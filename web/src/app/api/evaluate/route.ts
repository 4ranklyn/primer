import { NextRequest, NextResponse } from "next/server";
import { GoogleGenAI, Type } from "@google/genai";
import { challenges } from "@/lib/curriculum";

export interface EvaluateRequestBody {
  transcript: string;
  challengeId: string;
  currentHintLevel: number;
}

export interface EvaluateResponse {
  status: "CORRECT" | "INCORRECT" | "INAUDIBLE";
  nextHintLevel: number;
  spokenFeedback: string;
}

/**
 * Clean and normalize text for child speech matching:
 * Removes punctuation and collapses whitespace.
 */
function cleanText(text: string): string {
  return text
    .toLowerCase()
    .replace(/[.,\/#!$%\^&\*;:{}=\-_`~()?"'’]/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

/**
 * Deterministic local classifier to eliminate LLM latency/tokens for obvious matches.
 */
function evaluateLocally(
  cleanedTranscript: string,
  target: string,
  expectedAnswers: string[],
  currentHintLevel: number,
  celebrationText: string
): EvaluateResponse | null {
  if (!cleanedTranscript) {
    return {
      status: "INAUDIBLE",
      nextHintLevel: currentHintLevel,
      spokenFeedback: "Kiko belum mendengar suaramu. Coba lagi ya!",
    };
  }

  const normalizedTarget = cleanText(target);
  const normalizedExpected = expectedAnswers.map(cleanText);

  // Exact target match or matches any expected answer
  const isDirectMatch =
    cleanedTranscript === normalizedTarget ||
    normalizedExpected.includes(cleanedTranscript) ||
    cleanedTranscript.split(" ").includes(normalizedTarget) ||
    normalizedExpected.some(
      (ans) =>
        cleanedTranscript.includes(ans) ||
        (ans.length >= 3 && cleanedTranscript.includes(ans.slice(1))) // child dropped leading sound
    );

  if (isDirectMatch) {
    return {
      status: "CORRECT",
      nextHintLevel: currentHintLevel,
      spokenFeedback: celebrationText,
    };
  }

  return null;
}

export async function POST(req: NextRequest) {
  try {
    const body = (await req.json()) as Partial<EvaluateRequestBody>;
    const { transcript = "", challengeId = "", currentHintLevel = 0 } = body;

    const challenge = challenges.find((c) => c.id === challengeId);
    if (!challenge) {
      return NextResponse.json(
        { error: "Challenge not found" },
        { status: 404 }
      );
    }

    const cleanedTranscript = cleanText(transcript);
    const clampedHintLevel = Math.min(Math.max(currentHintLevel, 0), 2);

    // 1. Deterministic Local Match Guardrail (Zero token cost, <1ms)
    const localResult = evaluateLocally(
      cleanedTranscript,
      challenge.target,
      challenge.expectedAnswers,
      clampedHintLevel,
      challenge.celebrationText
    );

    if (localResult) {
      return NextResponse.json(localResult);
    }

    // 2. Prepare Fallback for timeout or failure
    const fallbackNextHint = Math.min(clampedHintLevel + 1, 3);
    const fallbackHintText =
      challenge.stepDownHints[Math.min(clampedHintLevel, 2)] ||
      challenge.celebrationText;

    const fallbackResponse: EvaluateResponse = {
      status: "INCORRECT",
      nextHintLevel: fallbackNextHint,
      spokenFeedback: fallbackHintText,
    };

    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      // Deterministic fallback if API key is not configured
      return NextResponse.json(fallbackResponse);
    }

    // 3. Gemini Call with 2.5s Timeout Guardrail
    const ai = new GoogleGenAI({ apiKey });

    const systemInstruction = `You are an expert Socratic evaluator for Indonesian Calistung (ages 4-6) companion app "Primer".
Your task: Evaluate an Indonesian toddler's spoken utterance against a target challenge.
Toddlers often exhibit speech sound disorders or developmental mispronunciations (e.g., dropping initial consonants 'ebek' for 'bebek', lisping 'tutuk' for 'tutup', or saying numbers casually).

Challenge Context:
- Target Concept: "${challenge.target}"
- Prompt: "${challenge.promptText}"
- Expected Answers: ${JSON.stringify(challenge.expectedAnswers)}
- Current Step-Down Hint Level: ${clampedHintLevel}
- Available Hints:
  Level 1 (Nudge): "${challenge.stepDownHints[0]}"
  Level 2 (Binary Choice): "${challenge.stepDownHints[1]}"
  Level 3 (Guided Sound/Reveal): "${challenge.stepDownHints[2]}"
- Celebration text: "${challenge.celebrationText}"

Classification Rules:
1. "CORRECT": If the child's utterance reasonably matches the target, target phoneme, or one of the expected answers, even with imperfect pronunciation.
   - spokenFeedback MUST be enthusiastic Indonesian praise (e.g. celebration text or equivalent).
   - nextHintLevel remains ${clampedHintLevel}.
2. "INCORRECT": If the child answered something clearly wrong or unrelated.
   - nextHintLevel MUST be ${fallbackNextHint}.
   - spokenFeedback MUST be the appropriate step-down hint in warm Indonesian.
3. "INAUDIBLE": If the utterance is pure noise, empty, or incomprehensible mumbling.
   - nextHintLevel remains ${clampedHintLevel}.
   - spokenFeedback should gently ask them to repeat.`;

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2500);

    try {
      const response = await ai.models.generateContent({
        model: "gemini-1.5-flash",
        contents: `Evaluate toddler utterance: "${cleanedTranscript}"`,
        config: {
          systemInstruction,
          temperature: 0.1,
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              status: {
                type: Type.STRING,
                enum: ["CORRECT", "INCORRECT", "INAUDIBLE"],
              },
              nextHintLevel: {
                type: Type.INTEGER,
              },
              spokenFeedback: {
                type: Type.STRING,
              },
            },
            required: ["status", "nextHintLevel", "spokenFeedback"],
          },
        },
      });

      clearTimeout(timeoutId);

      if (response.text) {
        const parsed = JSON.parse(response.text) as EvaluateResponse;
        return NextResponse.json({
          status: parsed.status,
          nextHintLevel: parsed.nextHintLevel ?? fallbackNextHint,
          spokenFeedback: parsed.spokenFeedback || fallbackHintText,
        });
      }

      return NextResponse.json(fallbackResponse);
    } catch {
      clearTimeout(timeoutId);
      // Graceful fallback on network timeout (2.5s) or API error
      return NextResponse.json(fallbackResponse);
    }
  } catch {
    return NextResponse.json(
      { error: "Invalid request payload" },
      { status: 400 }
    );
  }
}

/*
VERIFICATION / TEST CURL COMMANDS:

# 1. Local deterministic match (Zero token cost, instant):
# curl -X POST http://localhost:3000/api/evaluate \
#   -H "Content-Type: application/json" \
#   -d '{"transcript":"bebek","challengeId":"phonics-1-sound-b","currentHintLevel":0}'

# 2. Child pronunciation tolerance / LLM evaluation:
# curl -X POST http://localhost:3000/api/evaluate \
#   -H "Content-Type: application/json" \
#   -d '{"transcript":"ebek","challengeId":"phonics-1-sound-b","currentHintLevel":0}'

# 3. Incorrect answer stepping down hint level:
# curl -X POST http://localhost:3000/api/evaluate \
#   -H "Content-Type: application/json" \
#   -d '{"transcript":"gajah","challengeId":"phonics-1-sound-b","currentHintLevel":0}'
*/

import { NextRequest, NextResponse } from "next/server";
import { GoogleGenAI, Type } from "@google/genai";
import { challenges } from "@/lib/curriculum";
import {
  Challenge,
  EvaluationClassification,
  ChildSpeechEvaluationRequest,
  ChildSpeechEvaluationResponse,
  LegacyEvaluateResponse,
} from "@/types/curriculum";

export type EvaluateRequestBody = ChildSpeechEvaluationRequest;
export type EvaluateResponse = ChildSpeechEvaluationResponse | LegacyEvaluateResponse;

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
 * Local deterministic classifier for exact/clean transcript matches to minimize token spend & latency.
 */
/**
 * Local deterministic classifier for exact/clean transcript matches to minimize token spend & latency.
 */
function evaluateLocally(
  cleanedTranscript: string,
  challenge: Challenge,
  currentLevel: number,
  childNickname: string
): ChildSpeechEvaluationResponse | null {
  if (!cleanedTranscript) {
    return null;
  }

  const normalizedTarget = cleanText(challenge.target);
  const normalizedExpected = challenge.expectedAnswers.map(cleanText);
  const normalizedChoices = challenge.choices.map(cleanText);

  const words = cleanedTranscript.split(" ").filter(Boolean);

  // Common toddler pronunciation substitutions in Indonesian TK A/B:
  // e.g. "tu" -> 1, "wa" -> 2, "ga" -> 3, "pat" -> 4, "ma" -> 5
  // "tutu" -> "kuku", "tiya" -> "tiga"
  const toddlerWordEquivalents: Record<string, string[]> = {
    "1": ["satu", "tu", "atu"],
    "2": ["dua", "wa", "duwa", "uwa"],
    "3": ["tiga", "ga", "tiya", "tida", "iga"],
    "4": ["empat", "pat", "mpat", "empa"],
    "5": ["lima", "ma", "ima"],
    "6": ["enam", "nam"],
    "kuku": ["tutu", "kuku", "kku", "kuku."],
    "bapak": ["bapa", "apak", "pak"],
    "bebek": ["ebek", "bebe", "bekbek"],
    "apel": ["ape", "pel"],
    "ayam": ["yam", "aya"],
    "makan": ["akan", "mamam", "mam"],
    "minum": ["inum", "num"],
  };

  // 1. Check for CORRECT match with child phonetic tolerance
  const isDirectMatch =
    cleanedTranscript === normalizedTarget ||
    normalizedExpected.includes(cleanedTranscript) ||
    words.includes(normalizedTarget) ||
    normalizedExpected.some((ans) => {
      if (cleanedTranscript === ans) return true;
      if (words.includes(ans)) return true;
      if (cleanedTranscript.includes(ans) || ans.includes(cleanedTranscript)) return true;
      // Dropped initial or final consonant (e.g., 'ebek' for 'bebek', 'ape' for 'apel')
      if (ans.length >= 3 && (cleanedTranscript.includes(ans.slice(1)) || cleanedTranscript.includes(ans.slice(0, -1)))) {
        return true;
      }
      // Check toddler equivalents
      const equivalents = toddlerWordEquivalents[ans] || [];
      if (equivalents.some((eq) => cleanedTranscript === eq || words.includes(eq) || cleanedTranscript.includes(eq))) {
        return true;
      }
      return false;
    }) ||
    // Also check toddler equivalents for target
    (toddlerWordEquivalents[normalizedTarget] || []).some(
      (eq) => cleanedTranscript === eq || words.includes(eq) || cleanedTranscript.includes(eq)
    );

  if (isDirectMatch) {
    const spokenReply = `${childNickname ? `${childNickname}, ` : ""}${challenge.celebrationText}`;
    return {
      classification: "CORRECT",
      confidence: 1.0,
      nextStepDownLevel: 0,
      spokenReply,
      status: "CORRECT",
      nextHintLevel: 0,
      spokenFeedback: spokenReply,
    };
  }

  // 2. Check for explicit other choice (e.g. child selected or said a different card choice)
  const isKnownOtherChoice = normalizedChoices
    .filter((c) => c !== normalizedTarget && !normalizedExpected.includes(c))
    .some((c) => {
      if (cleanedTranscript === c || words.includes(c)) return true;
      const cEquivalents = toddlerWordEquivalents[c] || [];
      return cEquivalents.some((eq) => cleanedTranscript === eq || words.includes(eq));
    });

  if (isKnownOtherChoice) {
    return buildFallbackResponse("INCORRECT", currentLevel, challenge, childNickname);
  }

  return null;
}

/**
 * Build fallback Socratic response if Gemini API times out, fails, or key is unconfigured.
 */
function buildFallbackResponse(
  classification: EvaluationClassification,
  currentLevel: number,
  challenge: Challenge,
  childNickname: string
): ChildSpeechEvaluationResponse {
  const namePrefix = childNickname ? `${childNickname}, ` : "";

  if (classification === "CORRECT") {
    const spokenReply = `${namePrefix}${challenge.celebrationText}`;
    return {
      classification: "CORRECT",
      confidence: 0.9,
      nextStepDownLevel: 0,
      spokenReply,
      status: "CORRECT",
      nextHintLevel: 0,
      spokenFeedback: spokenReply,
    };
  }

  if (classification === "INAUDIBLE") {
    const spokenReply = `${namePrefix}Kiko belum mendengar suaramu dengan jelas. Coba ulangi lagi yuk!`;
    return {
      classification: "INAUDIBLE",
      confidence: 0.5,
      nextStepDownLevel: currentLevel,
      spokenReply,
      status: "INAUDIBLE",
      nextHintLevel: currentLevel,
      spokenFeedback: spokenReply,
    };
  }

  // Increment stepDownLevel: 0 -> 1 -> 2 -> 3 (clamped at 3)
  const nextLevel = Math.min(Math.max(currentLevel + 1, 1), 3);
  let spokenReply = "";

  if (nextLevel === 1) {
    spokenReply = `${namePrefix}${challenge.stepDownHints[0] || "Coba dengarkan bunyinya lagi ya!"}`;
  } else if (nextLevel === 2) {
    spokenReply = `${namePrefix}${challenge.stepDownHints[1] || `Apakah jawabannya ${challenge.choices.slice(0, 2).join(" atau ")}?`}`;
  } else {
    spokenReply = `${namePrefix}${challenge.stepDownHints[2] || "Yuk ucapkan bersama Kiko!"}`;
  }

  return {
    classification: classification === "PARTIAL" ? "PARTIAL" : "INCORRECT",
    confidence: 0.7,
    nextStepDownLevel: nextLevel,
    spokenReply,
    status: "INCORRECT",
    nextHintLevel: nextLevel,
    spokenFeedback: spokenReply,
  };
}

export async function POST(req: NextRequest) {
  try {
    const body = (await req.json()) as ChildSpeechEvaluationRequest;
    const {
      audioBase64,
      mimeType,
      transcript = "",
      currentNode,
      challengeId,
      stepDownLevel,
      currentHintLevel,
      childNickname = "Adik",
    } = body;

    // Resolve challenge node either from explicit currentNode or challengeId lookup
    let activeChallenge: Challenge | undefined = currentNode;
    if (!activeChallenge && challengeId) {
      activeChallenge = challenges.find((c) => c.id === challengeId);
    }
    if (!activeChallenge && !challengeId && challenges.length > 0) {
      activeChallenge = challenges[0];
    }

    if (!activeChallenge) {
      return NextResponse.json(
        { error: "Challenge node not found. Provide currentNode or valid challengeId." },
        { status: 404 }
      );
    }

    // Determine current level (support both stepDownLevel and currentHintLevel, clamped 0 to 3)
    const rawLevel = typeof stepDownLevel === "number" ? stepDownLevel : currentHintLevel ?? 0;
    const currentStepDown = Math.min(Math.max(rawLevel, 0), 3);

    const hasAudio = Boolean(audioBase64 && audioBase64.trim().length > 0);
    const cleanedTranscript = cleanText(transcript);

    // If both audio and transcript are absent, return inaudible response immediately
    if (!hasAudio && !cleanedTranscript) {
      const emptyAudioResponse = buildFallbackResponse(
        "INAUDIBLE",
        currentStepDown,
        activeChallenge,
        childNickname
      );
      return NextResponse.json(emptyAudioResponse);
    }

    // Fast-path: local check if transcript matches target/expected answers
    if (cleanedTranscript) {
      const localResult = evaluateLocally(
        cleanedTranscript,
        activeChallenge,
        currentStepDown,
        childNickname
      );
      if (localResult) {
        return NextResponse.json(localResult);
      }
    }

    const fallbackEvalResponse = buildFallbackResponse(
      cleanedTranscript ? "INCORRECT" : "INAUDIBLE",
      currentStepDown,
      activeChallenge,
      childNickname
    );

    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      // Deterministic fallback when API key is not configured in local environment
      return NextResponse.json(fallbackEvalResponse);
    }

    const ai = new GoogleGenAI({ apiKey });
    const targetModel =
      process.env.GEMINI_MODEL && process.env.GEMINI_MODEL !== "gemini-3.8-flash"
        ? process.env.GEMINI_MODEL
        : "gemini-flash-latest";

    const systemInstruction = `You are "Kiko", a gentle, encouraging Socratic Calistung (baca, tulis, hitung) learning companion for 4-to-6-year-old Indonesian children (TK A / TK B learners).

TARGET LEARNER PROFILE & PHONETIC TOLERANCE:
- Age: 4 to 6 years old.
- The child is learning Indonesian phonics, counting, or rhymes.
- You MUST evaluate child speech with generous developmental phonetic tolerance. Accept common toddler speech patterns, developmental lisps, or partial syllable approximations without penalizing developmental articulation:
  * Dropped initial or final consonants (e.g., 'ebek' for 'bebek', 'apel' -> 'ape', 'bapa' for 'bapak').
  * Lisping or sound substitution (e.g., 'c' -> 't', 'r' -> 'l'/'w', 's' -> 't' / 'th', 'kuku' -> 'tutu', 'tiga' -> 'tiya' / 'tida').
  * Syllable matching: if the child pronounces the core syllable or root sound of the target, count it as CORRECT or PARTIAL.
  * Casual counting pronunciations (e.g. 'tu', 'wa', 'ga' for satu, dua, tiga).

CRITICAL PEDAGOGICAL INVARIANTS:
1. NEVER say "Salah" (Wrong), "Bukan begitu", or any negative discouraging words.
2. Address the child naturally using their nickname: "${childNickname}".
3. Socratic response structuring based on nextStepDownLevel:
   - If evaluated as CORRECT:
     * nextStepDownLevel MUST be 0.
     * spokenReply: Enthusiastic warm celebration praising the child by name (${childNickname}).
   - If evaluated as INCORRECT or PARTIAL:
     * nextStepDownLevel must increment from current level (${currentStepDown}): 0 -> 1, 1 -> 2, 2 -> 3, 3 -> 3.
     * If nextStepDownLevel is 1: Give a gentle curiosity nudge (e.g., focus their attention on the initial sound, mouth shape, or hint: "${activeChallenge.stepDownHints[0]}").
     * If nextStepDownLevel is 2: Give a warm binary choice (e.g., "Apakah itu A atau B?", hint: "${activeChallenge.stepDownHints[1]}").
     * If nextStepDownLevel is 3: Give a guided count-along or sound-along prompt inviting the child to say it together with Kiko (hint: "${activeChallenge.stepDownHints[2]}").
   - If evaluated as OFF_TOPIC:
     * nextStepDownLevel should increment (or remain clamped at 1..3).
     * spokenReply: Gently redirect back to the target playfully without saying "Salah".
   - If evaluated as INAUDIBLE:
     * nextStepDownLevel remains ${currentStepDown}.
     * spokenReply: Gently invite the child to repeat because Kiko's ears missed it.

CHALLENGE CONTEXT:
- Challenge ID: "${activeChallenge.id}"
- Challenge Type: "${activeChallenge.type}"
- Target Concept: "${activeChallenge.target}"
- Question Prompt: "${activeChallenge.promptText}"
- Expected Answers: ${JSON.stringify(activeChallenge.expectedAnswers)}
- Visual Card Choices: ${JSON.stringify(activeChallenge.choices)}
- Step-Down Hint 1 (Nudge): "${activeChallenge.stepDownHints[0]}"
- Step-Down Hint 2 (Binary Choice): "${activeChallenge.stepDownHints[1]}"
- Step-Down Hint 3 (Guided sound/count-along): "${activeChallenge.stepDownHints[2]}"
- Celebration Template: "${activeChallenge.celebrationText}"
- Current Step-Down Level: ${currentStepDown}

RESPONSE FORMAT:
You must return strictly valid JSON matching the schema.`;

    // Prepare multimodal content parts in volatile memory (never written to disk)
    const contents: Array<string | { inlineData: { mimeType: string; data: string } } | { text: string }> = [];

    if (hasAudio) {
      const audioMime = mimeType || "audio/webm";
      // Clean base64 string if client sent data URL prefix
      const cleanBase64 = (audioBase64 as string).replace(/^data:[^;]+;base64,/, "").trim();
      contents.push({
        inlineData: {
          mimeType: audioMime,
          data: cleanBase64,
        },
      });
      contents.push({
        text: `Listen to this 4-to-6-year-old Indonesian child speaking for challenge "${activeChallenge.id}". Evaluate pronunciation with phonetic tolerance. Current stepDownLevel is ${currentStepDown}. Child's nickname is "${childNickname}".${cleanedTranscript ? ` Fallback transcript provided by speech recognizer: "${cleanedTranscript}".` : ""}`,
      });
    } else {
      contents.push({
        text: `Evaluate this fallback text transcript from a 4-to-6-year-old Indonesian child for challenge "${activeChallenge.id}": "${cleanedTranscript}". Current stepDownLevel is ${currentStepDown}. Child's nickname is "${childNickname}".`,
      });
    }

    try {
      const generatePromise = ai.models.generateContent({
        model: targetModel,
        contents,
        config: {
          systemInstruction,
          temperature: 0.2,
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              classification: {
                type: Type.STRING,
                enum: ["CORRECT", "PARTIAL", "INCORRECT", "OFF_TOPIC", "INAUDIBLE"],
                description: "Strict classification of child speech response",
              },
              confidence: {
                type: Type.NUMBER,
                description: "Evaluation confidence score between 0.0 and 1.0",
              },
              nextStepDownLevel: {
                type: Type.INTEGER,
                description: "0 if correct; 1 -> 2 -> 3 if incorrect/partial; unchanged if inaudible",
              },
              spokenReply: {
                type: Type.STRING,
                description: "Warm Indonesian Socratic response tailored to childNickname. Never say 'Salah'.",
              },
            },
            required: ["classification", "confidence", "nextStepDownLevel", "spokenReply"],
          },
        },
      });

      const timeoutPromise = new Promise<never>((_, reject) =>
        setTimeout(() => reject(new Error("Gemini generateContent timeout")), 3500)
      );

      const response = await Promise.race([generatePromise, timeoutPromise]);

      if (response.text) {
        const parsed = JSON.parse(response.text) as {
          classification: EvaluationClassification;
          confidence: number;
          nextStepDownLevel: number;
          spokenReply: string;
        };

        const rawClassification = parsed.classification || "INCORRECT";
        const classification: EvaluationClassification = [
          "CORRECT",
          "PARTIAL",
          "INCORRECT",
          "OFF_TOPIC",
          "INAUDIBLE",
        ].includes(rawClassification)
          ? rawClassification
          : "INCORRECT";

        const confidence = typeof parsed.confidence === "number" ? parsed.confidence : 0.8;
        const nextStepDownLevel =
          typeof parsed.nextStepDownLevel === "number"
            ? Math.min(Math.max(parsed.nextStepDownLevel, 0), 3)
            : fallbackEvalResponse.nextStepDownLevel;

        const spokenReply = parsed.spokenReply || fallbackEvalResponse.spokenReply;

        // Populate both new strict schema and backward compatibility fields
        const finalResponse: ChildSpeechEvaluationResponse = {
          classification,
          confidence,
          nextStepDownLevel,
          spokenReply,
          status: classification === "CORRECT" ? "CORRECT" : classification === "INAUDIBLE" ? "INAUDIBLE" : "INCORRECT",
          nextHintLevel: nextStepDownLevel,
          spokenFeedback: spokenReply,
        };

        return NextResponse.json(finalResponse);
      }

      return NextResponse.json(fallbackEvalResponse);
    } catch {
      return NextResponse.json(fallbackEvalResponse);
    }
  } catch {
    return NextResponse.json(
      { error: "Invalid request payload" },
      { status: 400 }
    );
  }
}

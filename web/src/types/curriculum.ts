export type ChallengeType = "phonics" | "counting" | "rhyme";

export interface Challenge {
  id: string;
  type: ChallengeType;
  target: string;
  promptText: string;
  expectedAnswers: string[];
  stepDownHints: [string, string, string]; // Level 1 (nudge), Level 2 (binary choice), Level 3 (guided sound)
  choices: string[]; // Visual tap card fallbacks
  celebrationText: string;
  audioPromptUrl?: string; // Pre-cached .opus audio prompt if mapped
}

export type EvaluationClassification =
  | "CORRECT"
  | "PARTIAL"
  | "INCORRECT"
  | "OFF_TOPIC"
  | "INAUDIBLE";

export interface ChildSpeechEvaluationRequest {
  audioBase64?: string;
  mimeType?: string;
  transcript?: string;
  currentNode?: Challenge;
  challengeId?: string;
  stepDownLevel?: number; // 0 to 3
  currentHintLevel?: number; // backward compatibility
  childNickname?: string;
}

export interface ChildSpeechEvaluationResponse {
  classification: EvaluationClassification;
  confidence: number;
  nextStepDownLevel: number; // 0 if correct, increment 1 -> 2 -> 3 if incorrect/partial
  spokenReply: string;
  // Legacy compatibility fields
  status: "CORRECT" | "INCORRECT" | "INAUDIBLE";
  nextHintLevel: number;
  spokenFeedback: string;
}

export interface LegacyEvaluateResponse {
  status: "CORRECT" | "INCORRECT" | "INAUDIBLE";
  nextHintLevel: number;
  spokenFeedback: string;
  classification?: EvaluationClassification;
  confidence?: number;
  nextStepDownLevel?: number;
  spokenReply?: string;
}

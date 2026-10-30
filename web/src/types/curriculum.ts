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
}

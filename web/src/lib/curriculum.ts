import { Challenge } from "@/types/curriculum";
import curriculumData from "@/data/curriculum.json";

export const challenges: Challenge[] = curriculumData as Challenge[];

export function getChallenge(index: number): Challenge {
  const safeIndex = ((index % challenges.length) + challenges.length) % challenges.length;
  return challenges[safeIndex];
}

export function getAllChallenges(): Challenge[] {
  return challenges;
}

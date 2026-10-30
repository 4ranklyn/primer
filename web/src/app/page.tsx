"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import Image from "next/image";
import { motion, AnimatePresence } from "framer-motion";
import confetti from "canvas-confetti";
import {
  Mic,
  Settings,
  Sparkles,
  Volume2,
  Zap,
  Play,
  CheckCircle,
  ChevronRight,
} from "lucide-react";
import { getChallenge, getAllChallenges } from "@/lib/curriculum";
import { Challenge, ChildSpeechEvaluationResponse } from "@/types/curriculum";
import ParentModal, { ParentDigestData } from "@/components/ParentModal";

type MascotState = "idle" | "listening" | "thinking" | "success" | "hint" | "speaking" | "paused";

/**
 * Child-friendly visual cues mapped to choices across curriculum challenges
 */
const CHOICE_VISUAL_CUES: Record<string, { icon: string; label: string }> = {
  // Letters (Phonics)
  B: { icon: "🦆", label: "Bebek & Bola" },
  D: { icon: "🐶", label: "Domba" },
  P: { icon: "🐧", label: "Penguin" },
  A: { icon: "🍎", label: "Apel & Ayam" },
  O: { icon: "🍊", label: "Oren" },
  U: { icon: "🐛", label: "Ulat" },
  M: { icon: "🍽️", label: "Makan & Minum" },
  N: { icon: "🍚", label: "Nasi" },
  W: { icon: "🥕", label: "Wortel" },
  PAK: { icon: "👨", label: "BA-PAK" },
  JU: { icon: "👕", label: "BA-JU" },
  LU: { icon: "🔨", label: "BA-LU" },
  KUKU: { icon: "💅", label: "Ku-Ku (Mirip Buku)" },
  TOPI: { icon: "🎩", label: "To-Pi" },
  BOLA: { icon: "⚽", label: "Bo-La" },
  // Numbers (Counting)
  "1": { icon: "🐱", label: "1 Kucing" },
  "2": { icon: "🍎🍎", label: "2 Buah" },
  "3": { icon: "🍎🍎🍎", label: "3 Apel" },
  "4": { icon: "🍬🍬🍬🍬", label: "4 Permen" },
  "5": { icon: "⭐⭐⭐⭐⭐", label: "5 Bintang" },
  "6": { icon: "🧁🧁🧁🧁🧁🧁", label: "6 Kue" },
};

/**
 * Robust Speech Synthesis controller with GC protection and timeout safety
 */
let speechTimeoutId: NodeJS.Timeout | null = null;

function stopCurrentSpeech() {
  if (typeof window !== "undefined") {
    if ("speechSynthesis" in window) {
      try {
        window.speechSynthesis.cancel();
      } catch {
        // ignore
      }
    }
    (window as unknown as { _kikoUtterance?: SpeechSynthesisUtterance | null })._kikoUtterance = null;
  }
  if (speechTimeoutId) {
    clearTimeout(speechTimeoutId);
    speechTimeoutId = null;
  }
}

function speakIndonesianText(text: string, onFinish: () => void) {
  stopCurrentSpeech();

  if (typeof window === "undefined" || !("speechSynthesis" in window)) {
    onFinish();
    return;
  }

  try {
    window.speechSynthesis.resume();
  } catch {
    // ignore
  }

  const utterance = new SpeechSynthesisUtterance(text);
  (window as unknown as { _kikoUtterance?: SpeechSynthesisUtterance | null })._kikoUtterance = utterance;
  utterance.lang = "id-ID";
  utterance.rate = 0.95;
  utterance.pitch = 1.12;

  let finished = false;
  const finishOnce = () => {
    if (!finished) {
      finished = true;
      if (speechTimeoutId) {
        clearTimeout(speechTimeoutId);
        speechTimeoutId = null;
      }
      (window as unknown as { _kikoUtterance?: SpeechSynthesisUtterance | null })._kikoUtterance = null;
      onFinish();
    }
  };

  utterance.onend = finishOnce;
  utterance.onerror = finishOnce;

  // Maximum speech duration fallback
  const maxWaitMs = Math.min(8000, Math.max(2000, text.length * 80));
  speechTimeoutId = setTimeout(finishOnce, maxWaitMs);

  try {
    window.speechSynthesis.speak(utterance);
  } catch {
    finishOnce();
  }
}

export default function CompanionGamePage() {
  const allChallenges = getAllChallenges();
  const [challengeIndex, setChallengeIndex] = useState(0);
  const currentChallenge: Challenge = getChallenge(challengeIndex);

  // Core Socratic & UI States
  const [mascotState, setMascotState] = useState<MascotState>("idle");
  const [stepDownLevel, setStepDownLevel] = useState<number>(0);
  const [childNickname] = useState<string>("Adik");
  const [spokenFeedback, setSpokenFeedback] = useState<string>("");
  const [lastTranscript, setLastTranscript] = useState<string>("");
  const [score, setScore] = useState(0);
  const [isSessionPaused, setIsSessionPaused] = useState(false);
  const [isWindDownCompleted, setIsWindDownCompleted] = useState(false);
  const [lowSnrCount, setLowSnrCount] = useState<number>(0);

  // Tactile Answer Cards Visual Feedback
  const [selectedChoice, setSelectedChoice] = useState<string | null>(null);
  const [choiceFeedback, setChoiceFeedback] = useState<"correct" | "incorrect" | null>(null);

  // Telemetry State Metrics
  const [telemetry, setTelemetry] = useState({
    unassisted_correct_count: 0,
    tasks_completed: 0,
    step_downs_triggered: 0,
    tcr_measurements: [] as number[],
    struggled_concepts: [] as string[],
  });

  // Session Duration Timer (seconds)
  const [sessionDurationSec, setSessionDurationSec] = useState<number>(0);

  // Audio & VAD RMS Volume State for Orb Dynamics
  const [rmsVolume, setRmsVolume] = useState<number>(0);

  // Parent Modal & Digest State
  const [isParentModalOpen, setIsParentModalOpen] = useState(false);
  const [digestData, setDigestData] = useState<ParentDigestData | null>(null);
  const [isSendingDigest, setIsSendingDigest] = useState(false);

  // Audio Context, Analyser & MediaStream Refs
  const audioContextRef = useRef<AudioContext | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const animationFrameRef = useRef<number | null>(null);

  // Speech Recognition & VAD Timing Refs
  const isSpeechDetectedRef = useRef<boolean>(false);
  const lastSpeechTimeRef = useRef<number>(0);
  const silenceTimer15sRef = useRef<NodeJS.Timeout | null>(null);
  const isListeningRef = useRef<boolean>(false);
  const isEvaluatingRef = useRef<boolean>(false);
  const isSpeakingRef = useRef<boolean>(false);
  const sessionWindDownTriggeredRef = useRef<boolean>(false);

  // TCR Timing Ref: recorded when companion's prompt finishes playing
  const promptEndTimeRef = useRef<number>(0);

  // Active audio element for pre-cached audio playback
  const activeAudioElRef = useRef<HTMLAudioElement | null>(null);

  // Speech recognition ref & captured transcript buffer
  const recognitionRef = useRef<{
    start: () => void;
    stop: () => void;
    abort?: () => void;
  } | null>(null);
  const capturedTranscriptRef = useRef<string>("");

  // Callback function refs for cyclic calls
  const playSocraticPromptRef = useRef<((text: string, audioUrl?: string) => Promise<void>) | null>(null);
  const startRecordingRef = useRef<(() => Promise<void>) | null>(null);
  const stopRecordingRef = useRef<(() => void) | null>(null);
  const evaluateAnswerRef = useRef<
    ((params: { blob?: Blob; transcript?: string; tcrMs: number }) => Promise<void>) | null
  >(null);

  // Ref to forward latest state into callbacks without stale closures
  const stateRef = useRef({
    stepDownLevel,
    currentChallenge,
    challengeIndex,
    childNickname,
    lowSnrCount,
    isSessionPaused,
    telemetry,
    sessionDurationSec,
  });

  useEffect(() => {
    stateRef.current = {
      stepDownLevel,
      currentChallenge,
      challengeIndex,
      childNickname,
      lowSnrCount,
      isSessionPaused,
      telemetry,
      sessionDurationSec,
    };
  }, [
    stepDownLevel,
    currentChallenge,
    challengeIndex,
    childNickname,
    lowSnrCount,
    isSessionPaused,
    telemetry,
    sessionDurationSec,
  ]);

  // Setup Web Speech Recognition for live Indonesian speech-to-text
  useEffect(() => {
    if (typeof window === "undefined") return;

    interface SpeechRecognitionInstance {
      lang: string;
      continuous: boolean;
      interimResults: boolean;
      maxAlternatives: number;
      onresult: (event: {
        resultIndex: number;
        results: ArrayLike<ArrayLike<{ transcript: string }> & { isFinal?: boolean }>;
      }) => void;
      onerror: (event: unknown) => void;
      onend: () => void;
      start: () => void;
      stop: () => void;
      abort?: () => void;
    }

    type SpeechRecognitionConstructor = new () => SpeechRecognitionInstance;

    const w = window as unknown as {
      SpeechRecognition?: SpeechRecognitionConstructor;
      webkitSpeechRecognition?: SpeechRecognitionConstructor;
    };
    const SpeechRecognition = w.SpeechRecognition || w.webkitSpeechRecognition;

    if (!SpeechRecognition) return;

    try {
      const recognition = new SpeechRecognition();
      recognition.lang = "id-ID";
      recognition.continuous = false;
      recognition.interimResults = true;
      recognition.maxAlternatives = 1;

      recognition.onresult = (event) => {
        let text = "";
        for (let i = 0; i < event.results.length; i++) {
          const item = event.results[i];
          if (item[0]?.transcript) {
            text += item[0].transcript;
          }
        }
        if (text) {
          capturedTranscriptRef.current = text;
          setLastTranscript(text);
          isSpeechDetectedRef.current = true;
          lastSpeechTimeRef.current = Date.now();
        }
      };

      recognition.onerror = () => {
        // Log speech recognition error without throwing
      };

      recognition.onend = () => {
        if (isListeningRef.current && !isEvaluatingRef.current) {
          if (capturedTranscriptRef.current) {
            if (stopRecordingRef.current) {
              stopRecordingRef.current();
            }
          }
        }
      };

      recognitionRef.current = recognition;
    } catch {
      // browser blocked microphone initialization
    }
  }, []);

  // Compute Median TCR in milliseconds
  const computeMedianTcrMs = useCallback((tcrList: number[]): number => {
    if (!tcrList || tcrList.length === 0) return 4200;
    const sorted = [...tcrList].sort((a, b) => a - b);
    const mid = Math.floor(sorted.length / 2);
    return sorted.length % 2 !== 0 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
  }, []);

  // Dispatch End-of-Session Parent WhatsApp ROI Pipeline to /api/notify-parent
  const triggerParentDigestPipeline = useCallback(
    async (customPhone?: string) => {
      setIsSendingDigest(true);
      const { telemetry: tel, sessionDurationSec: dur, childNickname: nick } = stateRef.current;
      const urrScore =
        tel.tasks_completed > 0 ? Math.round((tel.unassisted_correct_count / tel.tasks_completed) * 100) : 85;
      const medianTcr = computeMedianTcrMs(tel.tcr_measurements);
      const medianSec = +(medianTcr / 1000).toFixed(1);
      const storedPhone =
        typeof window !== "undefined" ? localStorage.getItem("primer_parent_wa_phone") || "" : "";
      const targetPhone = customPhone || storedPhone || "08123456789";

      try {
        const res = await fetch("/api/notify-parent", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            childNickname: nick,
            parentWaNumber: targetPhone,
            lessonTitle: "Calistung & Fonik Mandiri Bersama Kiko",
            metrics: {
              durationSec: dur,
              tasksCompleted: tel.tasks_completed,
              urrPercentage: urrScore,
              medianTcrSec: medianSec,
              stepDownsTriggered: tel.step_downs_triggered,
            },
            struggledConcepts:
              tel.struggled_concepts.length > 0
                ? tel.struggled_concepts
                : ["Pengenalan bunyi awal & perbandingan angka"],
          }),
        });

        if (res.ok) {
          const data = (await res.json()) as ParentDigestData;
          setDigestData(data);
        }
      } catch {
        const fallbackText = `*Laporan Sesi Primer Hari Ini* 🌟\nAnak: *${nick}*\nMateri: *Calistung & Fonik Mandiri Bersama Kiko*\n\n📊 *Capaian Belajar:*\n• Kecepatan Respons (TCR): *${medianSec} detik*\n• Tingkat Kemandirian (URR): *${urrScore}%*\n• Tantangan Selesai: *${tel.tasks_completed} konsep*\n• Bantuan Diberikan: *${tel.step_downs_triggered} kali*\n\n💡 *Ide Obrolan Meja Makan Malam Ini:*\n"Tadi di Primer belajar tentang Calistung & Fonik Mandiri Bersama Kiko. Coba tanya si kecil: tebak ada berapa benda bulat di meja makan malam ini?"\n\n_Primer — Pendamping Suara Sokratik Anak_`;
        const digits = targetPhone.replace(/\D/g, "");
        const cleanDigits = digits.startsWith("0") ? `62${digits.slice(1)}` : digits.startsWith("8") ? `62${digits}` : digits;
        const fallbackUrl = cleanDigits
          ? `https://wa.me/${cleanDigits}?text=${encodeURIComponent(fallbackText)}`
          : `https://wa.me/?text=${encodeURIComponent(fallbackText)}`;

        setDigestData({
          success: true,
          cleanPhone: cleanDigits,
          messageText: fallbackText,
          waUrl: fallbackUrl,
          whatsappDeepLink: fallbackUrl,
          webhookDelivered: false,
          dinnerPrompt: "Tadi di Primer belajar tentang Calistung & Fonik Mandiri Bersama Kiko. Coba tanya si kecil: tebak ada berapa benda bulat di meja makan malam ini?",
        });
      } finally {
        setIsSendingDigest(false);
      }
    },
    [computeMedianTcrMs]
  );

  // Clear silence detection timers
  const clearSilenceTimers = useCallback(() => {
    if (silenceTimer15sRef.current) {
      clearTimeout(silenceTimer15sRef.current);
      silenceTimer15sRef.current = null;
    }
  }, []);

  // Arm gentle nudge if child is silent for > 14s
  const armSilenceTimers = useCallback(() => {
    clearSilenceTimers();

    silenceTimer15sRef.current = setTimeout(() => {
      if (isListeningRef.current && !isEvaluatingRef.current && !stateRef.current.isSessionPaused) {
        if (!isSpeechDetectedRef.current) {
          if (stopRecordingRef.current) stopRecordingRef.current();
          const nudgeText = `${stateRef.current.childNickname}, Kiko siap mendengarkan suaramu. Boleh bicara atau ketuk kartu jawaban di layar ya!`;
          if (playSocraticPromptRef.current) {
            playSocraticPromptRef.current(nudgeText);
          }
        }
      }
    }, 14000);
  }, [clearSilenceTimers]);

  // Voice Output (TTS) with Indonesian window.speechSynthesis
  const playSocraticPrompt = useCallback(
    async (text: string, audioUrl?: string): Promise<void> => {
      return new Promise((resolve) => {
        clearSilenceTimers();
        isSpeakingRef.current = true;
        setMascotState("speaking");
        setSpokenFeedback(text);

        stopCurrentSpeech();

        if (activeAudioElRef.current) {
          activeAudioElRef.current.pause();
          activeAudioElRef.current = null;
        }

        const handlePlaybackFinished = () => {
          isSpeakingRef.current = false;
          promptEndTimeRef.current = Date.now();
          setMascotState("idle");
          resolve();
        };

        if (audioUrl) {
          const audio = new Audio(audioUrl);
          activeAudioElRef.current = audio;
          audio.onended = handlePlaybackFinished;
          audio.onerror = () => {
            speakIndonesianText(text, handlePlaybackFinished);
          };
          audio.play().catch(() => {
            speakIndonesianText(text, handlePlaybackFinished);
          });
          return;
        }

        speakIndonesianText(text, handlePlaybackFinished);
      });
    },
    [clearSilenceTimers]
  );

  // 14:00 Wind-Down Session Cutoff Trigger (PRD Scenario 3 & FR-03)
  const triggerWindDownRitual = useCallback(() => {
    if (stopRecordingRef.current) stopRecordingRef.current();
    clearSilenceTimers();
    stopCurrentSpeech();
    setIsSessionPaused(true);
    setIsWindDownCompleted(true);
    setMascotState("speaking");

    const windDownText = `Hore! Kamu sudah berusaha luar biasa hebat hari ini, ${childNickname}! Petualangan belajar kita selesai untuk sekarang. Waktunya mengistirahatkan telingamu!`;
    setSpokenFeedback(windDownText);

    speakIndonesianText(windDownText, () => {
      setIsParentModalOpen(true);
    });

    triggerParentDigestPipeline();
  }, [childNickname, clearSilenceTimers, triggerParentDigestPipeline]);

  // Advance to next curriculum node or complete curriculum lesson tree
  const advanceToNextChallenge = useCallback(
    (celebrationSpeech?: string) => {
      const currentIdx = stateRef.current.challengeIndex;
      const isLastNode = currentIdx + 1 >= allChallenges.length;

      if (isLastNode) {
        triggerWindDownRitual();
        return;
      }

      const nextIdx = currentIdx + 1;
      setSelectedChoice(null);
      setChoiceFeedback(null);
      setChallengeIndex(nextIdx);
      setStepDownLevel(0);
      setLowSnrCount(0);
      setLastTranscript("");
      capturedTranscriptRef.current = "";

      const nextChallenge = allChallenges[nextIdx];
      const speechToPlay = celebrationSpeech
        ? `${celebrationSpeech} Sekarang, ayo lihat yang ini: ${nextChallenge.promptText}`
        : nextChallenge.promptText;

      setTimeout(() => {
        if (playSocraticPromptRef.current) {
          playSocraticPromptRef.current(speechToPlay, nextChallenge.audioPromptUrl);
        }
      }, 700);
    },
    [allChallenges, triggerWindDownRitual]
  );

  // Unified Answer Evaluation: handles both voice audio, speech transcript, and tactile fallbacks
  const evaluateAnswer = useCallback(
    async ({
      blob,
      transcript,
      tcrMs,
    }: {
      blob?: Blob;
      transcript?: string;
      tcrMs: number;
    }) => {
      if (isEvaluatingRef.current) return;
      isEvaluatingRef.current = true;
      setMascotState("thinking");
      clearSilenceTimers();

      try {
        let audioBase64: string | undefined;
        let mimeType: string | undefined;

        if (blob && blob.size > 0) {
          const arrayBuffer = await blob.arrayBuffer();
          const bytes = new Uint8Array(arrayBuffer);
          let binary = "";
          for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
          }
          audioBase64 = btoa(binary);
          mimeType = blob.type || "audio/webm";
        }

        const currentLvl = stateRef.current.stepDownLevel;
        const currentTask = stateRef.current.currentChallenge;
        const nick = stateRef.current.childNickname;
        const effectiveTranscript = transcript || capturedTranscriptRef.current;

        const response = await fetch("/api/evaluate", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            audioBase64,
            mimeType,
            transcript: effectiveTranscript,
            currentNode: currentTask,
            challengeId: currentTask.id,
            stepDownLevel: currentLvl,
            childNickname: nick,
          }),
        });

        if (!response.ok) {
          throw new Error("Evaluation API error");
        }

        const data: ChildSpeechEvaluationResponse = await response.json();
        const { classification, confidence, nextStepDownLevel, spokenReply } = data;

        if (tcrMs > 0) {
          setTelemetry((prev) => ({
            ...prev,
            tcr_measurements: [...prev.tcr_measurements, tcrMs],
          }));
        }

        if (classification === "CORRECT") {
          setMascotState("success");
          setScore((s) => s + 10);
          setLowSnrCount(0);

          setTelemetry((prev) => ({
            ...prev,
            tasks_completed: prev.tasks_completed + 1,
            unassisted_correct_count:
              currentLvl === 0 ? prev.unassisted_correct_count + 1 : prev.unassisted_correct_count,
          }));

          confetti({
            particleCount: 65,
            spread: 70,
            origin: { y: 0.6 },
            colors: ["#38bdf8", "#fbbf24", "#34d399", "#f43f5e"],
          });

          isEvaluatingRef.current = false;
          advanceToNextChallenge(spokenReply);
          return;
        }

        if (
          (classification === "PARTIAL" ||
            classification === "INCORRECT" ||
            classification === "OFF_TOPIC") &&
          confidence >= 0.6
        ) {
          setLowSnrCount(0);
          setTelemetry((prev) => ({
            ...prev,
            step_downs_triggered: prev.step_downs_triggered + 1,
            struggled_concepts: prev.struggled_concepts.includes(currentTask.target)
              ? prev.struggled_concepts
              : [...prev.struggled_concepts, `${currentTask.type} (${currentTask.target})`],
          }));

          if (currentLvl >= 3) {
            setMascotState("hint");
            const guidedAnswerSpeech = `${nick}, kamu sudah berusaha sangat hebat! Jawabannya adalah ${currentTask.target}. Yuk kita coba tantangan seru berikutnya!`;
            isEvaluatingRef.current = false;
            advanceToNextChallenge(guidedAnswerSpeech);
            return;
          }

          const updatedLevel = Math.min(Math.max(nextStepDownLevel || currentLvl + 1, 1), 3);
          setStepDownLevel(updatedLevel);
          setMascotState("hint");

          let hintText = spokenReply;
          if (!hintText) {
            hintText = currentTask.stepDownHints[Math.min(updatedLevel - 1, 2)];
          }

          isEvaluatingRef.current = false;
          if (playSocraticPromptRef.current) {
            playSocraticPromptRef.current(hintText);
          }
          return;
        }

        // Low SNR / Inaudible handling
        const newLowSnr = stateRef.current.lowSnrCount + 1;
        setLowSnrCount(newLowSnr);
        setMascotState("hint");

        if (newLowSnr >= 2) {
          const fallbackAdvanceSpeech = `${nick}, Kiko kurang jelas mendengar suara di sekitarmu. Coba ketuk salah satu kartu di layar ya!`;
          isEvaluatingRef.current = false;
          if (playSocraticPromptRef.current) {
            playSocraticPromptRef.current(fallbackAdvanceSpeech);
          }
          return;
        }

        const repeatSpeech = `${nick}, Kiko kurang jelas mendengar suaramu. Boleh bicara lebih dekat ke mikrofon yuk!`;
        isEvaluatingRef.current = false;
        if (playSocraticPromptRef.current) {
          playSocraticPromptRef.current(repeatSpeech);
        }
      } catch {
        setMascotState("idle");
        isEvaluatingRef.current = false;
        if (playSocraticPromptRef.current) {
          playSocraticPromptRef.current(`Kiko siap mendengarkan lagi, ${stateRef.current.childNickname}!`);
        }
      }
    },
    [advanceToNextChallenge, clearSilenceTimers]
  );

  // Stop MediaRecorder and Web Speech Recognition, and finalize evaluation
  const stopRecording = useCallback(() => {
    isListeningRef.current = false;
    clearSilenceTimers();

    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
      animationFrameRef.current = null;
    }
    setRmsVolume(0);

    // Stop speech recognition
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch {
        // ignore
      }
    }

    // Stop media recorder
    const hadMediaRecorder = Boolean(
      mediaRecorderRef.current && mediaRecorderRef.current.state !== "inactive"
    );

    if (hadMediaRecorder && mediaRecorderRef.current) {
      try {
        mediaRecorderRef.current.stop();
      } catch {
        // ignore
      }
    } else {
      // If MediaRecorder was not active or already stopped, evaluate captured transcript directly
      const responseTime = Date.now();
      const tcrMs =
        promptEndTimeRef.current > 0 ? Math.max(0, responseTime - promptEndTimeRef.current) : 0;
      const transcript = capturedTranscriptRef.current || lastTranscript;
      if (evaluateAnswerRef.current) {
        evaluateAnswerRef.current({ transcript, tcrMs });
      }
    }
  }, [clearSilenceTimers, lastTranscript]);

  // Initialize or re-arm AudioContext, AnalyserNode and MediaRecorder
  const startRecording = useCallback(async () => {
    if (isEvaluatingRef.current || stateRef.current.isSessionPaused) {
      return;
    }

    // Stop any existing speech synthesis immediately
    stopCurrentSpeech();
    if (activeAudioElRef.current) {
      activeAudioElRef.current.pause();
      activeAudioElRef.current = null;
    }
    isSpeakingRef.current = false;

    clearSilenceTimers();
    isListeningRef.current = true;
    setMascotState("listening");
    audioChunksRef.current = [];
    capturedTranscriptRef.current = "";
    isSpeechDetectedRef.current = false;
    lastSpeechTimeRef.current = 0;

    armSilenceTimers();

    // 1. Start Web Speech Recognition
    if (recognitionRef.current) {
      try {
        recognitionRef.current.start();
      } catch {
        // already running or restarting
      }
    }

    // 2. Start MediaStream & VAD Analyser
    try {
      if (!mediaStreamRef.current || !mediaStreamRef.current.active) {
        mediaStreamRef.current = await navigator.mediaDevices.getUserMedia({
          audio: {
            echoCancellation: true,
            noiseSuppression: true,
            autoGainControl: true,
          },
        });
      }

      const stream = mediaStreamRef.current;

      const AudioCtx =
        window.AudioContext ||
        (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (!audioContextRef.current || audioContextRef.current.state === "closed") {
        audioContextRef.current = new AudioCtx();
      }
      if (audioContextRef.current.state === "suspended") {
        await audioContextRef.current.resume();
      }

      const ctx = audioContextRef.current;
      const source = ctx.createMediaStreamSource(stream);
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 512;
      analyser.smoothingTimeConstant = 0.4;
      source.connect(analyser);
      analyserRef.current = analyser;

      let mimeType = "audio/webm;codecs=opus";
      if (typeof MediaRecorder !== "undefined") {
        if (!MediaRecorder.isTypeSupported("audio/webm;codecs=opus")) {
          if (MediaRecorder.isTypeSupported("audio/webm")) {
            mimeType = "audio/webm";
          } else if (MediaRecorder.isTypeSupported("audio/mp4")) {
            mimeType = "audio/mp4";
          } else {
            mimeType = "";
          }
        }
      }

      const mediaRecorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        const responseTime = Date.now();
        const tcrMs =
          promptEndTimeRef.current > 0 ? Math.max(0, responseTime - promptEndTimeRef.current) : 0;
        const recordedBlob =
          audioChunksRef.current.length > 0
            ? new Blob(audioChunksRef.current, {
                type: mediaRecorder.mimeType || "audio/webm",
              })
            : undefined;

        if (evaluateAnswerRef.current) {
          evaluateAnswerRef.current({
            blob: recordedBlob,
            transcript: capturedTranscriptRef.current,
            tcrMs,
          });
        }
      };

      mediaRecorder.start(100);

      // Real-time VAD & RMS Loop
      const dataArray = new Uint8Array(analyser.frequencyBinCount);
      const SILENCE_THRESHOLD = 0.035;
      const SILENCE_DURATION_MS = 1300;

      const checkVad = () => {
        if (!isListeningRef.current || !analyserRef.current) return;

        analyserRef.current.getByteTimeDomainData(dataArray);

        let sumSquares = 0;
        for (let i = 0; i < dataArray.length; i++) {
          const normalized = (dataArray[i] - 128) / 128;
          sumSquares += normalized * normalized;
        }
        const rms = Math.sqrt(sumSquares / dataArray.length);
        setRmsVolume(rms);

        const now = Date.now();

        if (rms > SILENCE_THRESHOLD) {
          if (!isSpeechDetectedRef.current) {
            isSpeechDetectedRef.current = true;
          }
          lastSpeechTimeRef.current = now;
          armSilenceTimers();
        } else {
          if (isSpeechDetectedRef.current && lastSpeechTimeRef.current > 0) {
            if (now - lastSpeechTimeRef.current >= SILENCE_DURATION_MS) {
              clearSilenceTimers();
              stopRecording();
              return;
            }
          }
        }

        animationFrameRef.current = requestAnimationFrame(checkVad);
      };

      animationFrameRef.current = requestAnimationFrame(checkVad);
    } catch {
      // Even if MediaRecorder or getUserMedia is unavailable, speech recognition and tactile cards still work
    }
  }, [armSilenceTimers, clearSilenceTimers, stopRecording]);

  // Sync callback refs outside render in useEffect
  useEffect(() => {
    playSocraticPromptRef.current = playSocraticPrompt;
    startRecordingRef.current = startRecording;
    stopRecordingRef.current = stopRecording;
    evaluateAnswerRef.current = evaluateAnswer;
  }, [playSocraticPrompt, startRecording, stopRecording, evaluateAnswer]);

  // Session Duration Interval & 14:00 (840s) Wind-down Watcher
  useEffect(() => {
    const timer = setInterval(() => {
      setSessionDurationSec((prev) => {
        const next = prev + 1;
        if (next >= 840 && !sessionWindDownTriggeredRef.current) {
          sessionWindDownTriggeredRef.current = true;
          triggerWindDownRitual();
        }
        return next;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [triggerWindDownRitual]);

  // Initialize companion greeting on mount / first load
  useEffect(() => {
    const timer = setTimeout(() => {
      if (playSocraticPromptRef.current) {
        playSocraticPromptRef.current(currentChallenge.promptText, currentChallenge.audioPromptUrl);
      }
    }, 800);

    return () => {
      clearTimeout(timer);
      clearSilenceTimers();
      stopCurrentSpeech();
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
      if (mediaStreamRef.current) {
        mediaStreamRef.current.getTracks().forEach((t) => t.stop());
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Resume Session from Silence Pause
  const handleResumeSession = () => {
    setIsSessionPaused(false);
    setMascotState("idle");
    if (playSocraticPromptRef.current) {
      playSocraticPromptRef.current(
        `Selamat datang kembali ${childNickname}! Ayo lanjutkan: ${currentChallenge.promptText}`
      );
    }
  };

  // Toggle microphone listening when pressing the bottom voice button
  const handleVoiceButtonToggle = async () => {
    if (isSessionPaused) {
      handleResumeSession();
      return;
    }

    if (mascotState === "listening") {
      stopRecording();
    } else {
      stopCurrentSpeech();
      isSpeakingRef.current = false;
      await startRecording();
    }
  };

  // Tactile choice tap fallback handler with rich visual and auditory feedback
  const handleTactileChoice = (choice: string) => {
    stopRecording();
    clearSilenceTimers();
    stopCurrentSpeech();

    setSelectedChoice(choice);

    const cleanChoice = choice.trim().toLowerCase();
    const cleanTarget = currentChallenge.target.trim().toLowerCase();
    const cleanExpected = currentChallenge.expectedAnswers.map((a) => a.trim().toLowerCase());

    const isCorrect = cleanChoice === cleanTarget || cleanExpected.includes(cleanChoice);

    if (isCorrect) {
      setChoiceFeedback("correct");
      setMascotState("success");
      setSpokenFeedback(currentChallenge.celebrationText);
      setScore((s) => s + 10);
      setTelemetry((prev) => ({
        ...prev,
        tasks_completed: prev.tasks_completed + 1,
        unassisted_correct_count:
          stepDownLevel === 0 ? prev.unassisted_correct_count + 1 : prev.unassisted_correct_count,
      }));
      confetti({
        particleCount: 65,
        spread: 70,
        origin: { y: 0.6 },
        colors: ["#38bdf8", "#fbbf24", "#34d399", "#f43f5e"],
      });

      // Speak celebration and advance
      speakIndonesianText(currentChallenge.celebrationText, () => {
        setTimeout(() => {
          advanceToNextChallenge(`Hebat sekali ${childNickname}! Pilihanmu tepat!`);
        }, 300);
      });
    } else {
      setChoiceFeedback("incorrect");
      setMascotState("hint");
      const nextLvl = Math.min(stepDownLevel + 1, 3);
      setStepDownLevel(nextLvl);
      setTelemetry((prev) => ({
        ...prev,
        step_downs_triggered: prev.step_downs_triggered + 1,
        struggled_concepts: prev.struggled_concepts.includes(currentChallenge.target)
          ? prev.struggled_concepts
          : [...prev.struggled_concepts, `${currentChallenge.type} (${currentChallenge.target})`],
      }));

      if (nextLvl >= 3) {
        const guidedAnswerSpeech = `${childNickname}, kamu sudah berusaha sangat hebat! Jawabannya adalah ${currentChallenge.target}. Yuk kita coba tantangan seru berikutnya!`;
        setSpokenFeedback(guidedAnswerSpeech);
        speakIndonesianText(guidedAnswerSpeech, () => {
          setTimeout(() => {
            advanceToNextChallenge();
          }, 600);
        });
        return;
      }

      const hintText = currentChallenge.stepDownHints[Math.min(nextLvl - 1, 2)];
      setSpokenFeedback(hintText);
      speakIndonesianText(hintText, () => {
        // Reset selected choice after hint finishes so child can try again
        setTimeout(() => {
          setSelectedChoice(null);
          setChoiceFeedback(null);
          setMascotState("idle");
        }, 1200);
      });
    }
  };

  const dynamicOrbScale = mascotState === "listening" ? 1 + Math.min(rmsVolume * 3.5, 0.45) : 1;
  const currentUrrPercentage =
    telemetry.tasks_completed > 0
      ? Math.round((telemetry.unassisted_correct_count / telemetry.tasks_completed) * 100)
      : 100;
  const medianTcrInSeconds = (computeMedianTcrMs(telemetry.tcr_measurements) / 1000).toFixed(1);

  return (
    <main className="min-h-screen bg-gradient-to-b from-sky-100 via-amber-50 to-orange-100 flex flex-col items-center justify-between p-4 selection:bg-amber-200 select-none">
      {/* Top Header Bar */}
      <header className="w-full max-w-md flex items-center justify-between py-2 z-10">
        <button
          onClick={() => {
            setIsParentModalOpen(true);
            if (!digestData) {
              triggerParentDigestPipeline();
            }
          }}
          className="p-3 bg-white/90 hover:bg-white active:scale-95 transition rounded-2xl shadow-sm border border-slate-200 flex items-center gap-2 text-slate-700 font-bold text-sm cursor-pointer"
          aria-label="Parent Hub"
        >
          <Settings className="w-5 h-5 text-slate-500" />
          <span className="hidden sm:inline">Orang Tua</span>
        </button>

        {/* Progress Stars */}
        <div className="flex items-center gap-1.5 bg-white/90 px-4 py-2 rounded-2xl shadow-sm border border-amber-200">
          <Sparkles className="w-5 h-5 text-amber-500 fill-amber-400 animate-pulse" />
          <span className="font-extrabold text-amber-700 text-lg">
            {score} Bintang
          </span>
        </div>

        {/* Session Telemetry Pill */}
        <div className="flex items-center gap-1 text-[11px] font-bold text-slate-600 bg-white/80 px-3 py-1.5 rounded-2xl border border-slate-200 shadow-sm">
          <span>URR: {currentUrrPercentage}%</span>
        </div>
      </header>

      {/* Main Play Area */}
      <section className="w-full max-w-md flex flex-col items-center flex-1 justify-around my-2 text-center">
        {/* Challenge Prompt Banner */}
        <div className="w-full bg-white/95 backdrop-blur-sm p-4 rounded-3xl shadow-md border-2 border-amber-200 flex flex-col items-center gap-2">
          <div className="flex items-center justify-between w-full">
            <span className="text-xs uppercase font-bold tracking-wider px-2.5 py-1 rounded-full bg-amber-100 text-amber-800">
              Tantangan {challengeIndex + 1}/{allChallenges.length} • {currentChallenge.type}
            </span>
            <button
              onClick={() => {
                if (playSocraticPromptRef.current) {
                  playSocraticPromptRef.current(currentChallenge.promptText, currentChallenge.audioPromptUrl);
                }
              }}
              className="p-1.5 hover:bg-slate-100 rounded-full text-amber-600 active:scale-90 transition cursor-pointer"
              title="Dengarkan Ulang"
            >
              <Volume2 className="w-5 h-5" />
            </button>
          </div>

          <h1
            className="text-xl sm:text-2xl font-extrabold text-slate-800 leading-snug"
            style={{ fontFamily: "var(--font-fredoka), cursive" }}
          >
            {currentChallenge.promptText}
          </h1>

          {/* Socratic Step-down Hint Notice */}
          <AnimatePresence>
            {stepDownLevel > 0 && (
              <motion.div
                initial={{ opacity: 0, y: -6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                className="w-full mt-2 p-2.5 bg-orange-50 border border-orange-200 rounded-2xl text-xs text-orange-800 font-semibold flex items-center justify-center gap-1.5"
              >
                <Zap className="w-4 h-4 text-orange-500" />
                <span>
                  Petunjuk Kiko (Tingkat {stepDownLevel}):{" "}
                  {currentChallenge.stepDownHints[Math.min(stepDownLevel - 1, 2)]}
                </span>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Mascot (Kiko Orb) Driven by AudioContext RMS Volume */}
        <div className="relative my-3 flex flex-col items-center justify-center">
          <motion.div
            animate={{
              scale: dynamicOrbScale,
              boxShadow:
                mascotState === "listening"
                  ? `0 0 ${35 + rmsVolume * 90}px rgba(56, 189, 248, ${0.7 + rmsVolume * 0.3})`
                  : mascotState === "speaking"
                  ? "0 0 45px rgba(251, 191, 36, 0.7)"
                  : mascotState === "thinking"
                  ? "0 0 40px rgba(168, 85, 247, 0.7)"
                  : mascotState === "success"
                  ? "0 0 55px rgba(52, 211, 153, 0.9)"
                  : "0 0 25px rgba(251, 191, 36, 0.3)",
            }}
            transition={{
              type: "spring",
              stiffness: 300,
              damping: 20,
            }}
            className="w-44 h-44 sm:w-52 sm:h-52 rounded-full absolute -z-0 bg-gradient-to-tr from-amber-200 via-sky-200 to-rose-200"
          />

          <motion.div
            animate={
              mascotState === "thinking"
                ? { rotate: 360, scale: [1, 1.05, 1] }
                : mascotState === "success"
                ? { y: [0, -18, 0], scale: [1, 1.1, 1] }
                : mascotState === "speaking"
                ? { scale: [1, 1.04, 1] }
                : mascotState === "listening"
                ? { scale: dynamicOrbScale }
                : { scale: [1, 1.03, 1] }
            }
            transition={{
              duration: mascotState === "thinking" ? 1.5 : 2.5,
              repeat: mascotState === "thinking" || mascotState === "speaking" ? Infinity : 0,
              ease: "easeInOut",
            }}
            className="w-40 h-40 sm:w-48 sm:h-48 rounded-full overflow-hidden border-4 border-white shadow-xl relative z-10 bg-amber-50"
          >
            <Image
              src="/kiko.jpg"
              alt="Kiko Mascot"
              fill
              className="object-cover"
              priority
            />
          </motion.div>

          <AnimatePresence>
            {spokenFeedback && (
              <motion.div
                initial={{ opacity: 0, scale: 0.9, y: 10 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.9 }}
                className="mt-3 px-4 py-2 bg-slate-900/85 text-white text-xs sm:text-sm font-bold rounded-2xl shadow-lg max-w-xs leading-relaxed"
              >
                {spokenFeedback}
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Tactile Choice Cards Fallback */}
        <div className="w-full flex flex-col gap-2 my-2">
          <div className="flex items-center justify-between px-1">
            <p className="text-xs font-bold text-slate-600 uppercase tracking-wider flex items-center gap-1">
              <span>Pilih atau Ketuk Jawaban:</span>
            </p>
            {lowSnrCount >= 2 && (
              <span className="text-[10px] bg-sky-100 text-sky-800 font-bold px-2 py-0.5 rounded-full animate-pulse">
                Mode Ketuk Aktif
              </span>
            )}
          </div>

          <div className="grid grid-cols-3 gap-2 sm:gap-3 w-full">
            {currentChallenge.choices.map((choice) => {
              const isSelected = selectedChoice === choice;
              const cue = CHOICE_VISUAL_CUES[choice] || { icon: "✨", label: choice };

              let choiceStyle =
                "bg-white hover:bg-amber-50 text-slate-800 border-amber-200 hover:border-amber-400";
              if (isSelected) {
                if (choiceFeedback === "correct") {
                  choiceStyle =
                    "bg-emerald-500 text-white border-emerald-400 shadow-emerald-200 scale-105 ring-4 ring-emerald-200";
                } else if (choiceFeedback === "incorrect") {
                  choiceStyle =
                    "bg-rose-500 text-white border-rose-400 animate-pulse ring-4 ring-rose-200";
                }
              }

              return (
                <button
                  key={choice}
                  onClick={() => handleTactileChoice(choice)}
                  disabled={choiceFeedback === "correct"}
                  className={`py-3.5 px-2 active:scale-95 transition-all rounded-3xl shadow-md border-2 cursor-pointer flex flex-col items-center justify-center gap-1 ${choiceStyle}`}
                >
                  <span className="text-2xl sm:text-3xl">{cue.icon}</span>
                  <span
                    className="text-xl sm:text-2xl font-black leading-tight"
                    style={{ fontFamily: "var(--font-fredoka), cursive" }}
                  >
                    {choice}
                  </span>
                  <span className="text-[10px] sm:text-xs font-semibold opacity-90 truncate max-w-full px-1">
                    {cue.label}
                  </span>
                  {isSelected && choiceFeedback === "correct" && (
                    <CheckCircle className="w-5 h-5 text-white animate-bounce mt-0.5" />
                  )}
                </button>
              );
            })}
          </div>

          <div className="flex justify-between items-center mt-1 px-1">
            <span className="text-[11px] text-slate-500 font-medium">
              Ketuk kartu atau bicara langsung
            </span>
            <button
              onClick={() => advanceToNextChallenge()}
              className="text-xs text-slate-500 hover:text-slate-800 font-bold transition flex items-center gap-1 py-1 px-2 rounded-lg hover:bg-amber-100/60 cursor-pointer"
            >
              <span>Lewati tantangan ini</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </section>

      {/* Real Voice Status & Interactive Mic Button */}
      <footer className="w-full max-w-md flex flex-col items-center pb-4 pt-1 z-10">
        <div className="w-full flex flex-col items-center gap-2">
          <button
            onClick={handleVoiceButtonToggle}
            className={`w-full py-4.5 rounded-3xl flex items-center justify-center gap-3 shadow-xl border-4 active:scale-95 cursor-pointer transition-all ${
              mascotState === "listening"
                ? "bg-rose-500 hover:bg-rose-600 border-rose-300 text-white animate-pulse"
                : mascotState === "speaking"
                ? "bg-amber-500 hover:bg-amber-600 border-amber-300 text-white"
                : mascotState === "thinking"
                ? "bg-purple-500 hover:bg-purple-600 border-purple-300 text-white"
                : mascotState === "paused"
                ? "bg-slate-700 hover:bg-slate-800 border-slate-500 text-white"
                : "bg-sky-500 hover:bg-sky-600 border-sky-300 text-white"
            }`}
          >
            <Mic className={`w-7 h-7 ${mascotState === "listening" ? "animate-bounce" : ""}`} />
            <span
              className="text-xl sm:text-2xl font-extrabold tracking-wide"
              style={{ fontFamily: "var(--font-fredoka), cursive" }}
            >
              {mascotState === "listening"
                ? "Mendengarkan... (Ketuk Selesai)"
                : mascotState === "speaking"
                ? "Kiko Bicara... (Ketuk Jawab)"
                : mascotState === "thinking"
                ? "Kiko Berpikir..."
                : mascotState === "paused"
                ? "Sesi Dijeda (Ketuk Lanjut)"
                : "Tekan untuk Bicara (Suara)"}
            </span>
          </button>

          <div className="h-5 text-xs font-medium text-slate-600">
            {mascotState === "listening" ? (
              <span className="text-sky-700 font-bold">
                Bicaralah sekarang, Kiko siap mendengarkan jawabanmu
              </span>
            ) : lastTranscript ? (
              <span>Suara terdengar: &quot;{lastTranscript}&quot;</span>
            ) : (
              <span>Ucapkan jawaban atau ketuk salah satu kartu di atas</span>
            )}
          </div>
        </div>
      </footer>

      {/* 25-Second Silence Pause Overlay with Single Resume Button */}
      <AnimatePresence>
        {isSessionPaused && !isWindDownCompleted && (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-md z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              className="w-full max-w-sm bg-white p-6 rounded-3xl shadow-2xl text-center space-y-4 border border-amber-200"
            >
              <div className="w-16 h-16 mx-auto bg-amber-100 rounded-full flex items-center justify-center text-amber-600">
                <Play className="w-8 h-8 fill-amber-500 ml-1" />
              </div>
              <h2
                className="text-2xl font-extrabold text-slate-800"
                style={{ fontFamily: "var(--font-fredoka), cursive" }}
              >
                Istirahat Sebentar?
              </h2>
              <p className="text-xs text-slate-600 leading-relaxed font-medium">
                Kiko menjeda petualangan belajarmu. Tekan tombol di bawah untuk melanjutkan kapan saja!
              </p>
              <button
                onClick={handleResumeSession}
                className="w-full py-4 bg-sky-500 hover:bg-sky-600 active:scale-95 text-white font-extrabold text-lg rounded-2xl shadow-lg transition flex items-center justify-center gap-2 cursor-pointer"
              >
                <CheckCircle className="w-6 h-6" />
                <span>Lanjutkan Belajar</span>
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Parent Bottom Sheet Modal */}
      <ParentModal
        isOpen={isParentModalOpen}
        onClose={() => setIsParentModalOpen(false)}
        completedCount={telemetry.tasks_completed}
        score={score}
        urrPercentage={currentUrrPercentage}
        medianTcrSec={medianTcrInSeconds}
        stepDownsTriggered={telemetry.step_downs_triggered}
        waUrl={digestData?.waUrl}
        messageText={digestData?.messageText}
        cleanPhone={digestData?.cleanPhone}
        digestData={digestData}
        isSending={isSendingDigest}
        onSendWhatsApp={triggerParentDigestPipeline}
        onResetSession={() => {
          setScore(0);
          setChallengeIndex(0);
          setStepDownLevel(0);
          setSessionDurationSec(0);
          sessionWindDownTriggeredRef.current = false;
          setIsWindDownCompleted(false);
          setIsSessionPaused(false);
          setDigestData(null);
          setTelemetry({
            unassisted_correct_count: 0,
            tasks_completed: 0,
            step_downs_triggered: 0,
            tcr_measurements: [],
            struggled_concepts: [],
          });
          if (playSocraticPromptRef.current) {
            playSocraticPromptRef.current(allChallenges[0].promptText, allChallenges[0].audioPromptUrl);
          }
        }}
      />
    </main>
  );
}

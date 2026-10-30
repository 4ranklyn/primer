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
} from "lucide-react";
import { getChallenge, getAllChallenges } from "@/lib/curriculum";
import { Challenge, ChildSpeechEvaluationResponse } from "@/types/curriculum";
import ParentModal, { ParentDigestData } from "@/components/ParentModal";

type MascotState = "idle" | "listening" | "thinking" | "success" | "hint" | "speaking" | "paused";

/**
 * Speech synthesis helper extracted outside component render to avoid ref-during-render & hoisting issues
 */
function synthesizeSpeech(text: string, onFinish: () => void) {
  if (typeof window === "undefined" || !("speechSynthesis" in window)) {
    onFinish();
    return;
  }

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "id-ID";
  utterance.rate = 0.95;
  utterance.pitch = 1.1;

  let finished = false;
  const finishOnce = () => {
    if (!finished) {
      finished = true;
      onFinish();
    }
  };

  utterance.onend = finishOnce;
  utterance.onerror = finishOnce;

  setTimeout(() => {
    finishOnce();
  }, Math.max(2500, text.length * 90));

  window.speechSynthesis.speak(utterance);
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

  // VAD & Silence Detection Timing Refs
  const isSpeechDetectedRef = useRef<boolean>(false);
  const lastSpeechTimeRef = useRef<number>(0);
  const silenceTimer10sRef = useRef<NodeJS.Timeout | null>(null);
  const silenceTimer25sRef = useRef<NodeJS.Timeout | null>(null);
  const isListeningRef = useRef<boolean>(false);
  const isEvaluatingRef = useRef<boolean>(false);
  const isSpeakingRef = useRef<boolean>(false);
  const sessionWindDownTriggeredRef = useRef<boolean>(false);

  // TCR Timing Ref: recorded when companion's prompt finishes playing
  const promptEndTimeRef = useRef<number>(0);

  // Active audio element for pre-cached .opus playback
  const activeAudioElRef = useRef<HTMLAudioElement | null>(null);

  // Callback function refs for cyclic calls
  const playSocraticPromptRef = useRef<((text: string, audioUrl?: string) => Promise<void>) | null>(null);
  const startRecordingRef = useRef<(() => Promise<void>) | null>(null);
  const stopRecordingRef = useRef<(() => void) | null>(null);
  const evaluateAudioBlobRef = useRef<((blob: Blob, tcrMs: number) => Promise<void>) | null>(null);

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
            durationSec: dur,
            tasksCompleted: tel.tasks_completed,
            urrPercentage: urrScore,
            medianTcrMs: medianTcr,
            stepDownsTriggered: tel.step_downs_triggered,
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
        const fallbackText = `Halo Ayah & Bunda! ${nick} telah menyelesaikan belajar 14 menit bersama Kiko dengan URR ${urrScore}%.`;
        setDigestData({
          messageText: fallbackText,
          whatsappDeepLink: `https://wa.me/628123456789?text=${encodeURIComponent(fallbackText)}`,
          webhookDelivered: false,
        });
      } finally {
        setIsSendingDigest(false);
      }
    },
    [computeMedianTcrMs]
  );

  // Clear silence detection timers
  const clearSilenceTimers = useCallback(() => {
    if (silenceTimer10sRef.current) {
      clearTimeout(silenceTimer10sRef.current);
      silenceTimer10sRef.current = null;
    }
    if (silenceTimer25sRef.current) {
      clearTimeout(silenceTimer25sRef.current);
      silenceTimer25sRef.current = null;
    }
  }, []);

  // Reset silence timers and arm 10s and 25s timeouts
  const armSilenceTimers = useCallback(() => {
    clearSilenceTimers();

    silenceTimer10sRef.current = setTimeout(() => {
      if (isListeningRef.current && !isEvaluatingRef.current && !stateRef.current.isSessionPaused) {
        if (stopRecordingRef.current) stopRecordingRef.current();
        const nudgeText = `${stateRef.current.childNickname}, Kiko ada di sini menunggumu. Mau kita coba sama-sama?`;
        if (playSocraticPromptRef.current) {
          playSocraticPromptRef.current(nudgeText);
        }
      }
    }, 10000);

    silenceTimer25sRef.current = setTimeout(() => {
      if (!stateRef.current.isSessionPaused) {
        if (stopRecordingRef.current) stopRecordingRef.current();
        setIsSessionPaused(true);
        setMascotState("paused");
        if (typeof window !== "undefined" && "speechSynthesis" in window) {
          window.speechSynthesis.cancel();
        }
      }
    }, 25000);
  }, [clearSilenceTimers]);

  // Voice Output (TTS) with pre-cached .opus or Indonesian window.speechSynthesis
  const playSocraticPrompt = useCallback(
    async (text: string, audioUrl?: string): Promise<void> => {
      return new Promise((resolve) => {
        clearSilenceTimers();
        isSpeakingRef.current = true;
        setMascotState("speaking");
        setSpokenFeedback(text);

        if (typeof window !== "undefined" && "speechSynthesis" in window) {
          window.speechSynthesis.cancel();
        }
        if (activeAudioElRef.current) {
          activeAudioElRef.current.pause();
          activeAudioElRef.current = null;
        }

        const handlePlaybackFinished = () => {
          isSpeakingRef.current = false;
          promptEndTimeRef.current = Date.now();
          resolve();
          if (!stateRef.current.isSessionPaused && startRecordingRef.current) {
            startRecordingRef.current();
          }
        };

        if (audioUrl) {
          const audio = new Audio(audioUrl);
          activeAudioElRef.current = audio;
          audio.onended = handlePlaybackFinished;
          audio.onerror = () => {
            synthesizeSpeech(text, handlePlaybackFinished);
          };
          audio.play().catch(() => {
            synthesizeSpeech(text, handlePlaybackFinished);
          });
          return;
        }

        synthesizeSpeech(text, handlePlaybackFinished);
      });
    },
    [clearSilenceTimers]
  );

  // Stop MediaRecorder and teardown VAD monitoring loop
  const stopRecording = useCallback(() => {
    isListeningRef.current = false;
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
      animationFrameRef.current = null;
    }
    setRmsVolume(0);

    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== "inactive") {
      try {
        mediaRecorderRef.current.stop();
      } catch {
        // ignore state conflicts
      }
    }
  }, []);

  // Initialize or re-arm AudioContext, AnalyserNode and MediaRecorder
  const startRecording = useCallback(async () => {
    if (isSpeakingRef.current || isEvaluatingRef.current || stateRef.current.isSessionPaused) {
      return;
    }

    try {
      clearSilenceTimers();
      isListeningRef.current = true;
      setMascotState("listening");
      audioChunksRef.current = [];
      isSpeechDetectedRef.current = false;
      lastSpeechTimeRef.current = 0;

      armSilenceTimers();

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
        if (audioChunksRef.current.length > 0) {
          const recordedBlob = new Blob(audioChunksRef.current, {
            type: mediaRecorder.mimeType || "audio/webm",
          });
          const responseTime = Date.now();
          const tcrMs =
            promptEndTimeRef.current > 0 ? Math.max(0, responseTime - promptEndTimeRef.current) : 0;
          if (evaluateAudioBlobRef.current) {
            evaluateAudioBlobRef.current(recordedBlob, tcrMs);
          }
        }
      };

      mediaRecorder.start(100);

      // Real-time VAD & RMS Loop
      const dataArray = new Uint8Array(analyser.frequencyBinCount);
      const SILENCE_THRESHOLD = 0.035;
      const SILENCE_DURATION_MS = 1200;

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
      setMascotState("idle");
      isListeningRef.current = false;
    }
  }, [armSilenceTimers, clearSilenceTimers, stopRecording]);

  // 14:00 Wind-Down Session Cutoff Trigger (PRD Scenario 3 & FR-03)
  const triggerWindDownRitual = useCallback(() => {
    stopRecording();
    clearSilenceTimers();
    setIsSessionPaused(true);
    setIsWindDownCompleted(true);
    setMascotState("speaking");

    const windDownText = `Hore! Kamu sudah berusaha luar biasa hebat hari ini, ${childNickname}! Petualangan belajar kita selesai untuk sekarang. Waktunya mengistirahatkan telingamu!`;
    setSpokenFeedback(windDownText);

    if (typeof window !== "undefined" && "speechSynthesis" in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(windDownText);
      utterance.lang = "id-ID";
      utterance.rate = 0.95;
      utterance.pitch = 1.1;
      utterance.onend = () => {
        setIsParentModalOpen(true);
      };
      window.speechSynthesis.speak(utterance);
    } else {
      setIsParentModalOpen(true);
    }

    triggerParentDigestPipeline();
  }, [childNickname, clearSilenceTimers, stopRecording, triggerParentDigestPipeline]);

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
      setChallengeIndex(nextIdx);
      setStepDownLevel(0);
      setLowSnrCount(0);
      setLastTranscript("");

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

  // Evaluate Audio Blob via POST /api/evaluate
  const evaluateAudioBlob = useCallback(
    async (blob: Blob, tcrMs: number) => {
      if (isEvaluatingRef.current) return;
      isEvaluatingRef.current = true;
      setMascotState("thinking");
      clearSilenceTimers();

      try {
        const arrayBuffer = await blob.arrayBuffer();
        const bytes = new Uint8Array(arrayBuffer);
        let binary = "";
        for (let i = 0; i < bytes.byteLength; i++) {
          binary += String.fromCharCode(bytes[i]);
        }
        const audioBase64 = btoa(binary);

        const currentLvl = stateRef.current.stepDownLevel;
        const currentTask = stateRef.current.currentChallenge;
        const nick = stateRef.current.childNickname;

        const response = await fetch("/api/evaluate", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            audioBase64,
            mimeType: blob.type || "audio/webm",
            currentNode: currentTask,
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
          (classification === "PARTIAL" || classification === "INCORRECT" || classification === "OFF_TOPIC") &&
          confidence >= 0.65
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

        const newLowSnr = stateRef.current.lowSnrCount + 1;
        setLowSnrCount(newLowSnr);
        setMascotState("hint");

        if (newLowSnr >= 3) {
          const fallbackAdvanceSpeech = `Suara di sekitarmu agak ramai ya, ${nick}. Tidak apa-apa, ayo kita lanjut ke tantangan berikutnya bersama Kiko!`;
          isEvaluatingRef.current = false;
          advanceToNextChallenge(fallbackAdvanceSpeech);
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

  // Sync callback refs outside render in useEffect
  useEffect(() => {
    playSocraticPromptRef.current = playSocraticPrompt;
    startRecordingRef.current = startRecording;
    stopRecordingRef.current = stopRecording;
    evaluateAudioBlobRef.current = evaluateAudioBlob;
  }, [playSocraticPrompt, startRecording, stopRecording, evaluateAudioBlob]);

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
      if (typeof window !== "undefined" && "speechSynthesis" in window) {
        window.speechSynthesis.cancel();
      }
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

  // Tactile choice tap fallback handler
  const handleTactileChoice = (choice: string) => {
    stopRecording();
    clearSilenceTimers();

    const isCorrect =
      choice.toLowerCase() === currentChallenge.target.toLowerCase() ||
      currentChallenge.expectedAnswers.map((a) => a.toLowerCase()).includes(choice.toLowerCase());

    if (isCorrect) {
      setScore((s) => s + 10);
      setTelemetry((prev) => ({
        ...prev,
        tasks_completed: prev.tasks_completed + 1,
        unassisted_correct_count:
          stepDownLevel === 0 ? prev.unassisted_correct_count + 1 : prev.unassisted_correct_count,
      }));
      confetti({ particleCount: 50, spread: 60, origin: { y: 0.6 } });
      advanceToNextChallenge(`Hebat sekali ${childNickname}! Pilihanmu tepat!`);
    } else {
      const nextLvl = Math.min(stepDownLevel + 1, 3);
      setStepDownLevel(nextLvl);
      setTelemetry((prev) => ({
        ...prev,
        step_downs_triggered: prev.step_downs_triggered + 1,
        struggled_concepts: prev.struggled_concepts.includes(currentChallenge.target)
          ? prev.struggled_concepts
          : [...prev.struggled_concepts, `${currentChallenge.type} (${currentChallenge.target})`],
      }));
      if (playSocraticPromptRef.current) {
        playSocraticPromptRef.current(currentChallenge.stepDownHints[Math.min(nextLvl - 1, 2)]);
      }
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
          className="p-3 bg-white/80 hover:bg-white active:scale-95 transition rounded-2xl shadow-sm border border-slate-200 flex items-center gap-2 text-slate-700 font-bold text-sm"
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
        <div className="flex items-center gap-1 text-[11px] font-bold text-slate-500 bg-white/70 px-3 py-1.5 rounded-2xl border border-slate-200">
          <span>URR: {currentUrrPercentage}%</span>
        </div>
      </header>

      {/* Main Play Area */}
      <section className="w-full max-w-md flex flex-col items-center flex-1 justify-around my-2 text-center">
        {/* Challenge Prompt Banner */}
        <div className="w-full bg-white/90 backdrop-blur-sm p-4 rounded-3xl shadow-md border-2 border-amber-200 flex flex-col items-center gap-2">
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
              className="p-1.5 hover:bg-slate-100 rounded-full text-amber-600 active:scale-90 transition"
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
        <div className="relative my-4 flex flex-col items-center justify-center">
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
            className="w-48 h-48 sm:w-56 sm:h-56 rounded-full absolute -z-0 bg-gradient-to-tr from-amber-200 via-sky-200 to-rose-200"
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
            className="w-44 h-44 sm:w-52 sm:h-52 rounded-full overflow-hidden border-4 border-white shadow-xl relative z-10 bg-amber-50"
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
                className="mt-4 px-4 py-2.5 bg-slate-900/85 text-white text-sm font-bold rounded-2xl shadow-lg max-w-xs leading-relaxed"
              >
                {spokenFeedback}
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Tactile Choice Cards Fallback */}
        <div className="w-full flex flex-col gap-2 my-2">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
            Atau Ketuk Jawaban:
          </p>
          <div className="flex items-center justify-center gap-3">
            {currentChallenge.choices.map((choice) => (
              <button
                key={choice}
                onClick={() => handleTactileChoice(choice)}
                className="flex-1 py-4 px-3 bg-white hover:bg-amber-50 active:scale-95 transition-all text-xl sm:text-2xl font-black text-amber-700 rounded-3xl shadow-md border-2 border-amber-200 hover:border-amber-400"
                style={{ fontFamily: "var(--font-fredoka), cursive" }}
              >
                {choice}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Real Voice Status & Active Indicator */}
      <footer className="w-full max-w-md flex flex-col items-center pb-4 pt-1 z-10">
        <div className="w-full flex flex-col items-center gap-2">
          <div
            className={`w-full py-5 rounded-3xl flex items-center justify-center gap-3 shadow-xl border-4 transition-all ${
              mascotState === "listening"
                ? "bg-sky-500 border-sky-300 text-white animate-pulse"
                : mascotState === "speaking"
                ? "bg-amber-500 border-amber-300 text-white"
                : mascotState === "thinking"
                ? "bg-purple-500 border-purple-300 text-white"
                : mascotState === "paused"
                ? "bg-slate-700 border-slate-500 text-white"
                : "bg-sky-500 border-sky-300 text-white"
            }`}
          >
            <Mic className={`w-8 h-8 ${mascotState === "listening" ? "animate-bounce" : ""}`} />
            <span
              className="text-xl sm:text-2xl font-extrabold tracking-wide"
              style={{ fontFamily: "var(--font-fredoka), cursive" }}
            >
              {mascotState === "listening"
                ? "Kiko Mendengarkan..."
                : mascotState === "speaking"
                ? "Kiko Sedang Bicara..."
                : mascotState === "thinking"
                ? "Kiko Berpikir..."
                : mascotState === "paused"
                ? "Sesi Selesai / Dijeda"
                : "Bicara Langsung Yuk!"}
            </span>
          </div>

          <div className="h-5 text-xs font-medium text-slate-600">
            {mascotState === "listening" ? (
              <span className="text-sky-700 font-bold">
                Bicara sekarang, Kiko akan berhenti otomatis setelah selesai
              </span>
            ) : lastTranscript ? (
              <span>Suara terdengar: &quot;{lastTranscript}&quot;</span>
            ) : (
              <span>Cukup sebutkan jawabanmu lewat suara</span>
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
                className="w-full py-4 bg-sky-500 hover:bg-sky-600 active:scale-95 text-white font-extrabold text-lg rounded-2xl shadow-lg transition flex items-center justify-center gap-2"
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

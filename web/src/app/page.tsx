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
  RefreshCw,
  Award,
  ChevronRight,
  X,
  Zap,
} from "lucide-react";
import { getChallenge, getAllChallenges } from "@/lib/curriculum";
import { Challenge } from "@/types/curriculum";
import { EvaluateResponse } from "@/app/api/evaluate/route";
import ParentModal from "@/components/ParentModal";

type MascotState = "idle" | "listening" | "thinking" | "success" | "hint";

export default function CompanionGamePage() {
  const allChallenges = getAllChallenges();
  const [challengeIndex, setChallengeIndex] = useState(0);
  const currentChallenge: Challenge = getChallenge(challengeIndex);

  const [mascotState, setMascotState] = useState<MascotState>("idle");
  const [hintLevel, setHintLevel] = useState(0);
  const [spokenFeedback, setSpokenFeedback] = useState<string>("");
  const [lastTranscript, setLastTranscript] = useState<string>("");
  const [score, setScore] = useState(0);

  // Parent Modal
  const [isParentModalOpen, setIsParentModalOpen] = useState(false);

  // Demo Secret Override Panel
  const [isDemoOpen, setIsDemoOpen] = useState(false);
  const cornerClickCountRef = useRef(0);
  const cornerClickTimerRef = useRef<NodeJS.Timeout | null>(null);

  // Web Speech Recognition
  const [isRecording, setIsRecording] = useState(false);
  const [micSupported] = useState(() => {
    if (typeof window === "undefined") return true;
    const w = window as unknown as {
      SpeechRecognition?: unknown;
      webkitSpeechRecognition?: unknown;
    };
    return Boolean(w.SpeechRecognition || w.webkitSpeechRecognition);
  });
  const recognitionRef = useRef<{
    start: () => void;
    stop: () => void;
  } | null>(null);
  const evaluateUtteranceRef = useRef<((transcript: string) => Promise<void>) | null>(null);

  // Speak helper using Web Speech Synthesis
  const speakText = useCallback((text: string) => {
    if (typeof window === "undefined" || !("speechSynthesis" in window)) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "id-ID";
    utterance.rate = 0.95;
    utterance.pitch = 1.15;
    window.speechSynthesis.speak(utterance);
  }, []);

  const goToChallenge = useCallback(
    (nextIndex: number | ((prev: number) => number)) => {
      setHintLevel(0);
      setSpokenFeedback("");
      setLastTranscript("");
      setMascotState("idle");
      setChallengeIndex(nextIndex);
    },
    []
  );

  // Initialize Speech on Challenge Change
  useEffect(() => {
    const timer = setTimeout(() => {
      speakText(currentChallenge.promptText);
    }, 600);
    return () => clearTimeout(timer);
  }, [challengeIndex, currentChallenge.promptText, speakText]);

  // Setup Web Speech Recognition
  useEffect(() => {
    if (typeof window === "undefined") return;

    interface SpeechRecognitionInstance {
      lang: string;
      continuous: boolean;
      interimResults: boolean;
      maxAlternatives: number;
      onstart: () => void;
      onresult: (event: { results: ArrayLike<ArrayLike<{ transcript: string }>> }) => void;
      onerror: () => void;
      onend: () => void;
      start: () => void;
      stop: () => void;
    }

    type SpeechRecognitionConstructor = new () => SpeechRecognitionInstance;

    const w = window as unknown as {
      SpeechRecognition?: SpeechRecognitionConstructor;
      webkitSpeechRecognition?: SpeechRecognitionConstructor;
    };
    const SpeechRecognition = w.SpeechRecognition || w.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.lang = "id-ID";
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.maxAlternatives = 1;

      recognition.onstart = () => {
        setIsRecording(true);
        setMascotState("listening");
      };

      recognition.onresult = (event: { results: ArrayLike<ArrayLike<{ transcript: string }>> }) => {
        const transcript = event.results[0][0].transcript;
        setLastTranscript(transcript);
        evaluateUtteranceRef.current?.(transcript);
      };

      recognition.onerror = () => {
        setIsRecording(false);
        setMascotState("idle");
      };

      recognition.onend = () => {
        setIsRecording(false);
      };

      recognitionRef.current = recognition;
    } catch {
      // browser blocked microphone initialization
    }
  }, []);

  const handleEvaluationResult = useCallback(
    (result: EvaluateResponse) => {
      setSpokenFeedback(result.spokenFeedback);
      speakText(result.spokenFeedback);

      if (result.status === "CORRECT") {
        setMascotState("success");
        setScore((prev) => prev + 10);
        confetti({
          particleCount: 75,
          spread: 70,
          origin: { y: 0.6 },
          colors: ["#38bdf8", "#fbbf24", "#34d399", "#f43f5e"],
        });

        setTimeout(() => {
          goToChallenge((prev) => (prev + 1) % allChallenges.length);
        }, 2600);
      } else {
        setMascotState("hint");
        setHintLevel(result.nextHintLevel);
        setTimeout(() => {
          setMascotState("idle");
        }, 3500);
      }
    },
    [allChallenges.length, goToChallenge, speakText]
  );

  // Evaluate Utterance via Backend Route
  const evaluateUtterance = useCallback(
    async (transcript: string) => {
      setMascotState("thinking");
      try {
        const res = await fetch("/api/evaluate", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            transcript,
            challengeId: currentChallenge.id,
            currentHintLevel: hintLevel,
          }),
        });

        if (!res.ok) throw new Error("Evaluation failed");
        const data: EvaluateResponse = await res.json();
        handleEvaluationResult(data);
      } catch {
        const fallbackHint = Math.min(hintLevel + 1, 3);
        handleEvaluationResult({
          status: "INCORRECT",
          nextHintLevel: fallbackHint,
          spokenFeedback: currentChallenge.stepDownHints[Math.min(hintLevel, 2)],
        });
      }
    },
    [currentChallenge.id, currentChallenge.stepDownHints, handleEvaluationResult, hintLevel]
  );

  useEffect(() => {
    evaluateUtteranceRef.current = evaluateUtterance;
  });

  const startRecording = () => {
    if (!micSupported || !recognitionRef.current) return;
    try {
      recognitionRef.current.start();
    } catch {
      // recognition may already be running
    }
  };

  const stopRecording = () => {
    if (recognitionRef.current && isRecording) {
      try {
        recognitionRef.current.stop();
      } catch {
        // ignore
      }
    }
  };

  // Secret Demo Override Trigger
  const handleCornerClick = () => {
    cornerClickCountRef.current += 1;
    if (cornerClickTimerRef.current) clearTimeout(cornerClickTimerRef.current);

    if (cornerClickCountRef.current >= 3) {
      setIsDemoOpen(true);
      cornerClickCountRef.current = 0;
    } else {
      cornerClickTimerRef.current = setTimeout(() => {
        cornerClickCountRef.current = 0;
      }, 1200);
    }
  };

  return (
    <main className="min-h-screen bg-gradient-to-b from-sky-100 via-amber-50 to-orange-100 flex flex-col items-center justify-between p-4 selection:bg-amber-200 select-none">
      {/* Top Header Bar */}
      <header className="w-full max-w-md flex items-center justify-between py-2 z-10">
        <button
          onClick={() => setIsParentModalOpen(true)}
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

        {/* Secret Demo Corner Trigger */}
        <div
          onClick={handleCornerClick}
          className="w-12 h-12 flex items-center justify-center cursor-pointer active:opacity-60 rounded-2xl"
          title="Secret Demo Tap"
        >
          <div className="w-2 h-2 rounded-full bg-slate-300" />
        </div>
      </header>

      {/* Main Play Area */}
      <section className="w-full max-w-md flex flex-col items-center flex-1 justify-around my-2 text-center">
        {/* Challenge Prompt Banner */}
        <div className="w-full bg-white/90 backdrop-blur-sm p-4 rounded-3xl shadow-md border-2 border-amber-200 flex flex-col items-center gap-2">
          <div className="flex items-center justify-between w-full">
            <span className="text-xs uppercase font-bold tracking-wider px-2.5 py-1 rounded-full bg-amber-100 text-amber-800">
              Tantangan {challengeIndex + 1}/10 • {currentChallenge.type}
            </span>
            <button
              onClick={() => speakText(currentChallenge.promptText)}
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
            {hintLevel > 0 && (
              <motion.div
                initial={{ opacity: 0, y: -6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                className="w-full mt-2 p-2.5 bg-orange-50 border border-orange-200 rounded-2xl text-xs text-orange-800 font-semibold flex items-center justify-center gap-1.5"
              >
                <Zap className="w-4 h-4 text-orange-500" />
                <span>
                  Petunjuk Kiko:{" "}
                  {currentChallenge.stepDownHints[Math.min(hintLevel - 1, 2)]}
                </span>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Mascot (Kiko Orb) */}
        <div className="relative my-4 flex flex-col items-center justify-center">
          <motion.div
            animate={
              mascotState === "listening"
                ? {
                    scale: [1, 1.25, 1],
                    opacity: [0.6, 1, 0.6],
                    boxShadow: "0 0 45px rgba(56, 189, 248, 0.8)",
                  }
                : mascotState === "thinking"
                ? {
                    rotate: 360,
                    scale: [1, 1.08, 1],
                    boxShadow: "0 0 35px rgba(251, 191, 36, 0.8)",
                  }
                : mascotState === "success"
                ? {
                    scale: [1, 1.2, 1],
                    boxShadow: "0 0 50px rgba(52, 211, 153, 0.9)",
                  }
                : {
                    scale: [1, 1.05, 1],
                    boxShadow: "0 0 25px rgba(251, 191, 36, 0.3)",
                  }
            }
            transition={{
              repeat: Infinity,
              duration: mascotState === "thinking" ? 1.5 : 2.5,
              ease: "easeInOut",
            }}
            className="w-48 h-48 sm:w-56 sm:h-56 rounded-full absolute -z-0 bg-gradient-to-tr from-amber-200 via-sky-200 to-rose-200"
          />

          <motion.div
            animate={
              mascotState === "success"
                ? { y: [0, -18, 0], scale: [1, 1.1, 1] }
                : mascotState === "hint"
                ? { rotate: [-4, 4, -4, 0] }
                : { scale: [1, 1.04, 1] }
            }
            transition={{
              duration: mascotState === "success" ? 0.6 : 2.8,
              repeat: mascotState === "success" ? 2 : Infinity,
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
                className="mt-4 px-4 py-2 bg-slate-900/85 text-white text-sm font-bold rounded-2xl shadow-lg max-w-xs"
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
                onClick={() => evaluateUtterance(choice)}
                className="flex-1 py-4 px-3 bg-white hover:bg-amber-50 active:scale-95 transition-all text-xl sm:text-2xl font-black text-amber-700 rounded-3xl shadow-md border-2 border-amber-200 hover:border-amber-400"
                style={{ fontFamily: "var(--font-fredoka), cursive" }}
              >
                {choice}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Push-to-Talk Voice Microphone Footer */}
      <footer className="w-full max-w-md flex flex-col items-center pb-4 pt-1 z-10">
        <div className="w-full flex flex-col items-center gap-2">
          <button
            onMouseDown={startRecording}
            onMouseUp={stopRecording}
            onTouchStart={startRecording}
            onTouchEnd={stopRecording}
            className={`w-full py-5 rounded-3xl flex items-center justify-center gap-3 shadow-xl border-4 active:scale-95 transition-all ${
              isRecording
                ? "bg-rose-500 border-rose-300 text-white animate-pulse"
                : "bg-sky-500 hover:bg-sky-600 border-sky-300 text-white"
            }`}
          >
            <Mic className={`w-8 h-8 ${isRecording ? "animate-bounce" : ""}`} />
            <span
              className="text-xl sm:text-2xl font-extrabold tracking-wide"
              style={{ fontFamily: "var(--font-fredoka), cursive" }}
            >
              {isRecording ? "Mendengarkan..." : "Tahan untuk Bicara"}
            </span>
          </button>

          <div className="h-5 text-xs font-medium text-slate-600">
            {isRecording ? (
              <span className="text-rose-600 font-bold">
                Lepas tombol setelah selesai berbicara
              </span>
            ) : lastTranscript ? (
              <span>
                Suara terdengar: &quot;{lastTranscript}&quot;
              </span>
            ) : !micSupported ? (
              <span className="text-slate-400">
                Gunakan kartu pilihan di atas
              </span>
            ) : (
              <span>Tekan dan tahan tombol mikrofon untuk menjawab</span>
            )}
          </div>
        </div>
      </footer>

      {/* Secret Demo Injection Modal */}
      <AnimatePresence>
        {isDemoOpen && (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              className="w-full max-w-sm bg-slate-900 text-white p-5 rounded-3xl shadow-2xl border border-slate-700"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <div className="flex items-center gap-2">
                  <Zap className="w-5 h-5 text-amber-400" />
                  <h3 className="font-bold text-sm">Demo Override Panel</h3>
                </div>
                <button
                  onClick={() => setIsDemoOpen(false)}
                  className="p-1 hover:bg-slate-800 rounded-full text-slate-400"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="py-4 space-y-2.5">
                <button
                  onClick={() => {
                    setIsDemoOpen(false);
                    evaluateUtterance(currentChallenge.target);
                  }}
                  className="w-full py-2.5 px-3 bg-emerald-600 hover:bg-emerald-500 rounded-2xl text-xs font-bold transition flex items-center justify-between"
                >
                  <span>Simulate: Jawaban Benar</span>
                  <Award className="w-4 h-4" />
                </button>

                <button
                  onClick={() => {
                    setIsDemoOpen(false);
                    evaluateUtterance("jawaban_salah_uji_coba");
                  }}
                  className="w-full py-2.5 px-3 bg-amber-600 hover:bg-amber-500 rounded-2xl text-xs font-bold transition flex items-center justify-between"
                >
                  <span>Simulate: Nudge Step-down</span>
                  <RefreshCw className="w-4 h-4" />
                </button>

                <button
                  onClick={() => {
                    setIsDemoOpen(false);
                    handleEvaluationResult({
                      status: "INCORRECT",
                      nextHintLevel: Math.min(hintLevel + 1, 3),
                      spokenFeedback:
                        "Koneksi terhambat, Kiko bantu dengan petunjuk ya!",
                    });
                  }}
                  className="w-full py-2.5 px-3 bg-rose-600 hover:bg-rose-500 rounded-2xl text-xs font-bold transition flex items-center justify-between"
                >
                  <span>Simulate: Network Jitter Fallback</span>
                  <Zap className="w-4 h-4" />
                </button>

                <button
                  onClick={() => {
                    setIsDemoOpen(false);
                    goToChallenge(
                      (prev) => (prev + 1) % allChallenges.length
                    );
                  }}
                  className="w-full py-2.5 px-3 bg-slate-800 hover:bg-slate-700 rounded-2xl text-xs font-bold transition flex items-center justify-between"
                >
                  <span>Skip to Next Challenge</span>
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Parent Bottom Sheet Modal */}
      <ParentModal
        isOpen={isParentModalOpen}
        onClose={() => setIsParentModalOpen(false)}
        completedCount={challengeIndex}
        score={score}
        onResetSession={() => {
          setScore(0);
          goToChallenge(0);
        }}
      />
    </main>
  );
}

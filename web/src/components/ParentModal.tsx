"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  ShieldCheck,
  X,
  Clock,
  Target,
  Sparkles,
  Send,
  Lock,
  Copy,
  Check,
  ExternalLink,
  MessageSquare,
} from "lucide-react";
import { NotifyParentResponse } from "@/types/curriculum";

export type ParentDigestData = NotifyParentResponse;

interface ParentModalProps {
  isOpen: boolean;
  onClose: () => void;
  completedCount: number;
  score: number;
  urrPercentage: number;
  medianTcrSec: string | number;
  stepDownsTriggered?: number;
  waUrl?: string;
  messageText?: string;
  cleanPhone?: string;
  digestData?: ParentDigestData | null;
  onResetSession: () => void;
  onSendWhatsApp: (phone: string) => Promise<void>;
  isSending?: boolean;
}

const STORAGE_PHONE_KEY = "primer_parent_wa_phone";

export default function ParentModal({
  isOpen,
  onClose,
  completedCount,
  score,
  urrPercentage,
  medianTcrSec,
  stepDownsTriggered = 0,
  waUrl: propWaUrl,
  messageText: propMessageText,
  cleanPhone: propCleanPhone,
  digestData,
  onResetSession,
  onSendWhatsApp,
  isSending = false,
}: ParentModalProps) {
  const [isUnlocked, setIsUnlocked] = useState(false);
  const [pinAnswer, setPinAnswer] = useState("");
  const [pinError, setPinError] = useState(false);

  // Clipboard copy state
  const [copied, setCopied] = useState(false);

  // Phone number state
  const [phoneNumber, setPhoneNumber] = useState(() => {
    if (typeof window !== "undefined") {
      return localStorage.getItem(STORAGE_PHONE_KEY) || "";
    }
    return "";
  });

  const sessionMinutes = Math.max(1, Math.round(completedCount * 1.4));

  // Resolved values prioritizing props then digestData
  const effectiveMessageText =
    propMessageText ||
    digestData?.messageText ||
    `*Laporan Sesi Primer Hari Ini* 🌟\nAnak: *Adik*\nMateri: *Calistung Bersama Kiko*\n\n📊 *Capaian Belajar:*\n• Kecepatan Respons (TCR): *${medianTcrSec} detik*\n• Tingkat Kemandirian (URR): *${urrPercentage}%*\n• Tantangan Selesai: *${completedCount} konsep*\n• Bantuan Diberikan: *${stepDownsTriggered} kali*\n\n_Primer — Pendamping Suara Sokratik Anak_`;

  const effectiveCleanPhone =
    propCleanPhone || digestData?.cleanPhone || phoneNumber.replace(/\D/g, "");

  const effectiveWaUrl =
    propWaUrl ||
    digestData?.waUrl ||
    digestData?.whatsappDeepLink ||
    (effectiveCleanPhone
      ? `https://wa.me/${effectiveCleanPhone}?text=${encodeURIComponent(effectiveMessageText)}`
      : `https://wa.me/?text=${encodeURIComponent(effectiveMessageText)}`);

  const handleClose = () => {
    setIsUnlocked(false);
    setPinAnswer("");
    setPinError(false);
    setCopied(false);
    onClose();
  };

  const handleVerifyGate = (e: React.FormEvent) => {
    e.preventDefault();
    if (pinAnswer.trim() === "7") {
      setIsUnlocked(true);
      setPinError(false);
    } else {
      setPinError(true);
    }
  };

  const handleCopyMessage = async () => {
    if (!effectiveMessageText) return;
    try {
      if (typeof navigator !== "undefined" && navigator.clipboard) {
        await navigator.clipboard.writeText(effectiveMessageText);
        setCopied(true);
        setTimeout(() => setCopied(false), 2400);
      }
    } catch {
      // fallback
    }
  };

  const handleOpenWhatsApp = () => {
    if (typeof window !== "undefined" && effectiveWaUrl) {
      window.open(effectiveWaUrl, "_blank", "noopener,noreferrer");
    }
  };

  const handleSubmitPhone = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phoneNumber) return;
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_PHONE_KEY, phoneNumber);
    }
    await onSendWhatsApp(phoneNumber);
  };

  // Timestamp for WhatsApp bubble
  const [bubbleTime] = useState(() => {
    if (typeof window !== "undefined") {
      return new Date().toLocaleTimeString("id-ID", {
        hour: "2-digit",
        minute: "2-digit",
      });
    }
    return "12:00";
  });

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <motion.div
            initial={{ y: "100%", opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: "100%", opacity: 0 }}
            transition={{ type: "spring", damping: 25, stiffness: 220 }}
            className="w-full max-w-md bg-white text-slate-800 rounded-t-3xl sm:rounded-3xl shadow-2xl border border-slate-200 overflow-hidden max-h-[92vh] flex flex-col"
          >
            {/* Modal Header */}
            <div className="flex items-center justify-between p-5 border-b border-slate-100 bg-slate-50">
              <div className="flex items-center gap-2.5">
                <div className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-extrabold text-base text-slate-900 leading-tight">
                    Portal Orang Tua (Parent Digest)
                  </h3>
                  <p className="text-xs text-slate-500">
                    Laporan Sesi Belajar &amp; Ide Obrolan Meja Makan
                  </p>
                </div>
              </div>
              <button
                onClick={handleClose}
                className="p-1.5 hover:bg-slate-200/60 rounded-full text-slate-400 hover:text-slate-600 transition cursor-pointer"
                aria-label="Close"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6 overflow-y-auto space-y-4">
              {!isUnlocked ? (
                // Security Math Gate (4 + 3 = 7)
                <form onSubmit={handleVerifyGate} className="space-y-4 py-2">
                  <div className="text-center space-y-1">
                    <div className="w-12 h-12 mx-auto rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                      <Lock className="w-6 h-6" />
                    </div>
                    <h4 className="font-bold text-slate-800 text-lg">
                      Verifikasi Orang Tua
                    </h4>
                    <p className="text-xs text-slate-500">
                      Untuk memastikan keamanan anak, mohon jawab soal berikut:
                    </p>
                  </div>

                  <div className="p-4 bg-amber-50 rounded-2xl text-center border border-amber-200">
                    <span className="text-xs font-semibold text-amber-700 uppercase">
                      Pertanyaan Keamanan
                    </span>
                    <p className="font-black text-2xl text-amber-900 mt-0.5">
                      Berapa 4 + 3?
                    </p>
                  </div>

                  <input
                    type="number"
                    value={pinAnswer}
                    onChange={(e) => setPinAnswer(e.target.value)}
                    placeholder="Ketik angka hasil penjumlahan..."
                    className="w-full px-4 py-3.5 border-2 border-slate-200 rounded-2xl text-center text-xl font-bold focus:border-emerald-500 outline-none transition"
                    autoFocus
                  />

                  {pinError && (
                    <p className="text-xs text-rose-600 font-bold text-center">
                      Jawaban salah. Silakan coba kembali.
                    </p>
                  )}

                  <button
                    type="submit"
                    className="w-full py-3.5 bg-emerald-600 hover:bg-emerald-700 active:scale-98 text-white font-extrabold rounded-2xl transition shadow-md cursor-pointer"
                  >
                    Buka Laporan Sesi
                  </button>
                </form>
              ) : (
                // Unlocked Parent Digest
                <div className="space-y-4">
                  {/* 3 ROI Metrics Cards */}
                  <div className="grid grid-cols-3 gap-2">
                    <div className="p-3 bg-sky-50 rounded-2xl border border-sky-100 flex flex-col justify-between">
                      <div className="flex items-center gap-1 text-sky-600">
                        <Clock className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-bold uppercase">
                          Selesai
                        </span>
                      </div>
                      <p className="text-lg font-black text-sky-900 mt-1">
                        {completedCount} Bab
                      </p>
                      <span className="text-[9px] text-sky-600 font-medium">
                        {sessionMinutes} menit ({score}★)
                      </span>
                    </div>

                    <div className="p-3 bg-emerald-50 rounded-2xl border border-emerald-100 flex flex-col justify-between">
                      <div className="flex items-center gap-1 text-emerald-600">
                        <Target className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-bold uppercase">
                          URR Mandiri
                        </span>
                      </div>
                      <p className="text-lg font-black text-emerald-900 mt-1">
                        {urrPercentage}%
                      </p>
                      <span className="text-[9px] text-emerald-600 font-medium">
                        Tanpa petunjuk
                      </span>
                    </div>

                    <div className="p-3 bg-purple-50 rounded-2xl border border-purple-100 flex flex-col justify-between">
                      <div className="flex items-center gap-1 text-purple-600">
                        <Sparkles className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-bold uppercase">
                          TCR Kecepatan
                        </span>
                      </div>
                      <p className="text-lg font-black text-purple-900 mt-1">
                        {medianTcrSec}s
                      </p>
                      <span className="text-[9px] text-purple-600 font-medium">
                        Respon rata-rata
                      </span>
                    </div>
                  </div>

                  {/* WhatsApp Chat Bubble Card Preview (#DCF8C6) */}
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between text-xs font-bold text-slate-700 px-1">
                      <div className="flex items-center gap-1.5 text-emerald-700">
                        <MessageSquare className="w-4 h-4 fill-emerald-600 text-emerald-600" />
                        <span>Pratinjau Pesan WhatsApp</span>
                      </div>
                      <span className="text-[10px] text-slate-500 font-medium">
                        Deep Link (wa.me)
                      </span>
                    </div>

                    <div className="relative p-4 rounded-2xl rounded-tr-sm bg-[#DCF8C6] border border-[#BDE59E] shadow-sm text-slate-900">
                      {/* Chat text */}
                      <div className="text-xs sm:text-[13px] leading-relaxed whitespace-pre-wrap font-sans text-slate-800">
                        {effectiveMessageText}
                      </div>

                      {/* Bubble timestamp & double check status */}
                      <div className="flex items-center justify-end gap-1 mt-2 text-[10px] text-slate-500 font-medium">
                        <span>{bubbleTime || "Hari ini"}</span>
                        <span className="text-[#34B7F1] font-bold tracking-tighter">
                          ✓✓
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Primary & Secondary Action CTAs */}
                  <div className="space-y-2 pt-1">
                    {/* Primary CTA: Kirim Laporan ke WhatsApp */}
                    <button
                      onClick={handleOpenWhatsApp}
                      className="w-full py-3.5 px-4 bg-[#25D366] hover:bg-[#20ba5a] active:scale-98 text-white font-bold rounded-2xl transition shadow-md flex items-center justify-center gap-2 text-sm cursor-pointer"
                    >
                      <ExternalLink className="w-4 h-4" />
                      <span>Kirim Laporan ke WhatsApp</span>
                    </button>

                    {/* Secondary CTA: Salin Pesan */}
                    <button
                      onClick={handleCopyMessage}
                      className="w-full py-2.5 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-semibold rounded-2xl transition flex items-center justify-center gap-2 text-xs cursor-pointer border border-slate-200"
                    >
                      {copied ? (
                        <>
                          <Check className="w-4 h-4 text-emerald-600" />
                          <span className="text-emerald-700 font-bold">
                            Tersalin ke Clipboard!
                          </span>
                        </>
                      ) : (
                        <>
                          <Copy className="w-4 h-4 text-slate-500" />
                          <span>Salin Pesan</span>
                        </>
                      )}
                    </button>
                  </div>

                  {/* Inline Phone Input Field if phone is missing or parent wants to update */}
                  <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200 space-y-2">
                    <div className="flex items-center justify-between">
                      <p className="text-xs font-bold text-slate-800">
                        Nomor WhatsApp Orang Tua:
                      </p>
                      {effectiveCleanPhone && (
                        <span className="text-[10px] text-emerald-700 font-bold bg-emerald-100 px-2 py-0.5 rounded-full">
                          +{effectiveCleanPhone}
                        </span>
                      )}
                    </div>
                    <p className="text-[11px] text-slate-500 leading-normal">
                      {!effectiveCleanPhone
                        ? "Masukkan nomor HP agar tautan WhatsApp langsung membuka obrolan dengan nomor Ayah/Bunda."
                        : "Ingin mengirim ke nomor WhatsApp lain? Masukkan nomor baru di bawah:"}
                    </p>

                    <form onSubmit={handleSubmitPhone} className="flex gap-2">
                      <input
                        type="tel"
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        placeholder="Contoh: 08123456789 atau 62812..."
                        className="flex-1 px-3 py-2 bg-white border border-slate-300 rounded-xl text-xs font-medium focus:border-emerald-500 outline-none"
                      />
                      <button
                        type="submit"
                        disabled={isSending || !phoneNumber}
                        className="py-2 px-3 bg-slate-800 hover:bg-slate-700 disabled:opacity-40 text-white rounded-xl text-xs font-bold transition flex items-center gap-1 shadow-sm cursor-pointer"
                      >
                        <Send className="w-3.5 h-3.5" />
                        <span>{isSending ? "..." : "Simpan"}</span>
                      </button>
                    </form>
                  </div>

                  {/* Reset Button */}
                  <div className="pt-1">
                    <button
                      onClick={() => {
                        onResetSession();
                        handleClose();
                      }}
                      className="w-full py-2 text-center text-xs font-bold text-slate-400 hover:text-slate-600 transition cursor-pointer"
                    >
                      Reset Kemajuan Belajar Hari Ini
                    </button>
                  </div>
                </div>
              )}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}

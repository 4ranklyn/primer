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
  CheckCircle2,
  Lock,
  ExternalLink,
  MessageCircle,
} from "lucide-react";

export interface ParentDigestData {
  messageText: string;
  whatsappDeepLink: string;
  webhookDelivered: boolean;
}

interface ParentModalProps {
  isOpen: boolean;
  onClose: () => void;
  completedCount: number;
  score: number;
  urrPercentage: number;
  medianTcrSec: string;
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
  digestData,
  onResetSession,
  onSendWhatsApp,
  isSending = false,
}: ParentModalProps) {
  const [isUnlocked, setIsUnlocked] = useState(false);
  const [pinAnswer, setPinAnswer] = useState("");
  const [pinError, setPinError] = useState(false);

  // Phone & WhatsApp dispatch state
  const [phoneNumber, setPhoneNumber] = useState(() => {
    if (typeof window !== "undefined") {
      return localStorage.getItem(STORAGE_PHONE_KEY) || "";
    }
    return "";
  });

  const sessionMinutes = Math.max(1, Math.round(completedCount * 1.4));

  const handleClose = () => {
    setIsUnlocked(false);
    setPinAnswer("");
    setPinError(false);
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

  const handleSubmitPhone = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phoneNumber) return;
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_PHONE_KEY, phoneNumber);
    }
    await onSendWhatsApp(phoneNumber);
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <motion.div
            initial={{ y: "100%", opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: "100%", opacity: 0 }}
            transition={{ type: "spring", damping: 25, stiffness: 220 }}
            className="w-full max-w-md bg-white text-slate-800 rounded-t-3xl sm:rounded-3xl shadow-2xl border border-slate-200 overflow-hidden max-h-[90vh] flex flex-col"
          >
            {/* Modal Header */}
            <div className="flex items-center justify-between p-5 border-b border-slate-100 bg-slate-50">
              <div className="flex items-center gap-2.5">
                <div className="p-2 bg-sky-100 rounded-xl">
                  <ShieldCheck className="w-5 h-5 text-sky-600" />
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
                className="p-1.5 hover:bg-slate-200/60 rounded-full text-slate-400 hover:text-slate-600 transition"
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
                    className="w-full px-4 py-3.5 border-2 border-slate-200 rounded-2xl text-center text-xl font-bold focus:border-sky-500 outline-none transition"
                    autoFocus
                  />

                  {pinError && (
                    <p className="text-xs text-rose-600 font-bold text-center">
                      Jawaban salah. Silakan coba kembali.
                    </p>
                  )}

                  <button
                    type="submit"
                    className="w-full py-3.5 bg-sky-500 hover:bg-sky-600 active:scale-98 text-white font-extrabold rounded-2xl transition shadow-md"
                  >
                    Buka Laporan Sesi
                  </button>
                </form>
              ) : (
                // Unlocked Parent Digest
                <div className="space-y-4">
                  {/* 3 Clear ROI Metrics Cards */}
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

                  {/* Generated AI WhatsApp Digest Display */}
                  {digestData && digestData.messageText && (
                    <div className="p-4 bg-emerald-50 rounded-2xl border border-emerald-200 space-y-2">
                      <div className="flex items-center justify-between text-emerald-800 font-bold text-xs">
                        <div className="flex items-center gap-1.5">
                          <MessageCircle className="w-4 h-4 text-emerald-600" />
                          <span>Ringkasan WhatsApp Gemini 3.8 Flash</span>
                        </div>
                        {digestData.webhookDelivered && (
                          <span className="text-[10px] bg-emerald-200 text-emerald-900 px-2 py-0.5 rounded-full flex items-center gap-1">
                            <CheckCircle2 className="w-3 h-3" /> Terkirim Otomatis
                          </span>
                        )}
                      </div>
                      <div className="text-xs text-slate-700 font-medium whitespace-pre-wrap leading-relaxed max-h-48 overflow-y-auto bg-white/70 p-3 rounded-xl border border-emerald-100">
                        {digestData.messageText}
                      </div>

                      {/* Direct WhatsApp Deep Link Action Button */}
                      <a
                        href={digestData.whatsappDeepLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="w-full py-3 px-4 bg-emerald-600 hover:bg-emerald-500 active:scale-95 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 shadow-sm"
                      >
                        <ExternalLink className="w-4 h-4" />
                        <span>Buka Chat WhatsApp (Kirim Langsung)</span>
                      </a>
                    </div>
                  )}

                  {/* WhatsApp Custom Phone Update / Re-dispatch Form */}
                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200 space-y-2.5">
                    <div>
                      <p className="text-xs font-bold text-slate-800">
                        Kirim Ulang ke Nomor WhatsApp Lain
                      </p>
                      <p className="text-[11px] text-slate-500">
                        Nomor HP orang tua untuk menerima pesan ringkasan harian
                      </p>
                    </div>

                    <form onSubmit={handleSubmitPhone} className="space-y-2">
                      <input
                        type="tel"
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        placeholder="Contoh: 08123456789 atau 62812..."
                        className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-xl text-sm font-medium focus:border-emerald-500 outline-none"
                      />
                      <button
                        type="submit"
                        disabled={isSending || !phoneNumber}
                        className="w-full py-2.5 px-4 bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 shadow-sm"
                      >
                        <Send className="w-4 h-4" />
                        <span>{isSending ? "Memproses..." : "Perbarui & Kirim Ringkasan"}</span>
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
                      className="w-full py-2.5 text-center text-xs font-bold text-slate-400 hover:text-slate-600 transition"
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

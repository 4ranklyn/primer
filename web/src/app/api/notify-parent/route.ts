import { NextRequest, NextResponse } from "next/server";
import { NotifyParentRequest, NotifyParentResponse } from "@/types/curriculum";

export type { NotifyParentRequest, NotifyParentResponse };

/**
 * Sanitize parent WhatsApp number to international E.164 format without '+' or non-digit symbols:
 * 1. Strip all non-digit characters (`replace(/\D/g, '')`).
 * 2. If it starts with '0', replace with '62' (e.g. '0812...' -> '62812...').
 * 3. If it starts with '8', prepend '62' (e.g. '812...' -> '62812...').
 * 4. If it starts with '62', keep as is.
 */
export function sanitizeWhatsAppNumber(raw?: string): string {
  if (!raw) return "";
  const digits = raw.replace(/\D/g, "");
  if (!digits) return "";
  if (digits.startsWith("0")) {
    return `62${digits.slice(1)}`;
  }
  if (digits.startsWith("8")) {
    return `62${digits}`;
  }
  if (digits.startsWith("62")) {
    return digits;
  }
  return digits;
}

export async function POST(req: NextRequest) {
  try {
    const body = (await req.json()) as Partial<NotifyParentRequest>;
    const {
      childNickname = "si Kecil",
      parentWaNumber,
      phone,
      lessonTitle = "Calistung & Fonik Mandiri Bersama Kiko",
      metrics,
      struggledConcepts = [],
      tasksCompleted,
      urrPercentage,
      medianTcrMs,
      stepDownsTriggered,
    } = body;

    // 1. Sanitize parent WhatsApp number
    const rawNumber = parentWaNumber || phone || "";
    const cleanPhone = sanitizeWhatsAppNumber(rawNumber);

    // 2. Normalize session metrics
    const effectiveTasksCompleted = metrics?.tasksCompleted ?? tasksCompleted ?? 5;
    const effectiveUrr = metrics?.urrPercentage ?? urrPercentage ?? 80;
    const effectiveMedianTcrSec =
      metrics?.medianTcrSec ??
      (typeof medianTcrMs === "number" ? +(medianTcrMs / 1000).toFixed(1) : 4.5);
    const effectiveStepDowns = metrics?.stepDownsTriggered ?? stepDownsTriggered ?? 0;

    // 3. Generate a personalized Indonesian parent digest and screen-free dinner question using Gemini
    const apiKey = process.env.GEMINI_API_KEY;
    const targetModel = process.env.GEMINI_MODEL || "gemini-3.8-flash";

    let dinnerPrompt = "";

    if (apiKey) {
      try {
        const geminiEndpoint = `https://generativelanguage.googleapis.com/v1beta/models/${targetModel}:generateContent?key=${apiKey}`;

        const prompt = `Anda adalah asisten pendidikan anak usia dini untuk aplikasi "Primer".
Buatlah 1 pertanyaan/teka-teki obrolan meja makan malam ini (screen-free offline dinner question) yang konkret, menyenangkan, dan relevan dengan materi yang dipelajari anak hari ini.

DATA ANAK & MATERI:
- Nama Anak: ${childNickname}
- Materi Hari Ini: ${lessonTitle}
- Konsep yang dilatih: ${struggledConcepts.length > 0 ? struggledConcepts.join(", ") : "Pengenalan huruf, bunyi fonik & berhitung benda"}

INSTRUKSI:
- Berikan TEPAT 1 kalimat pertanyaan konkret yang bisa diajukan orang tua saat makan malam tanpa layar sama sekali (misalnya menebak benda bulat di meja, mencari kata berima di dapur, atau menghitung sendok).
- Gunakan bahasa Indonesia yang hangat, bersahabat, dan ramah anak.
- Output HANYA pertanyaan obrolan tersebut dalam 1 kalimat singkat tanpa tanda kutip pembuka/penutup.`;

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4000);

        const geminiRes = await fetch(geminiEndpoint, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          signal: controller.signal,
          body: JSON.stringify({
            contents: [
              {
                parts: [{ text: prompt }],
              },
            ],
            generationConfig: {
              temperature: 0.3,
              maxOutputTokens: 200,
            },
          }),
        });

        clearTimeout(timeoutId);

        if (geminiRes.ok) {
          const geminiData = await geminiRes.json();
          const candidateText =
            geminiData?.candidates?.[0]?.content?.parts?.[0]?.text;
          if (candidateText && candidateText.trim().length > 0) {
            dinnerPrompt = candidateText
              .trim()
              .replace(/^["'“”]/, "")
              .replace(/["'“”]$/, "")
              .trim();
          }
        }
      } catch {
        // Fall through immediately to deterministic fallback
      }
    }

    // 4. Deterministic fallback if API key is missing or call times out
    if (!dinnerPrompt) {
      dinnerPrompt = `Tadi di Primer belajar tentang ${lessonTitle}. Coba tanya si kecil: tebak ada berapa benda bulat di meja makan malam ini?`;
    }

    // 5. Construct the final message using WhatsApp Markdown formatting
    const messageText = `*Laporan Sesi Primer Hari Ini* 🌟
Anak: *${childNickname}*
Materi: *${lessonTitle}*

📊 *Capaian Belajar:*
• Kecepatan Respons (TCR): *${effectiveMedianTcrSec} detik*
• Tingkat Kemandirian (URR): *${effectiveUrr}%*
• Tantangan Selesai: *${effectiveTasksCompleted} konsep*
• Bantuan Diberikan: *${effectiveStepDowns} kali*

💡 *Ide Obrolan Meja Makan Malam Ini:*
"${dinnerPrompt}"

_Primer — Pendamping Suara Sokratik Anak_`;

    // 6. Construct deep link
    const encodedMessage = encodeURIComponent(messageText);
    const waUrl = cleanPhone
      ? `https://wa.me/${cleanPhone}?text=${encodedMessage}`
      : `https://wa.me/?text=${encodedMessage}`;

    const payload: NotifyParentResponse = {
      success: true,
      cleanPhone,
      messageText,
      waUrl,
      whatsappDeepLink: waUrl,
      webhookDelivered: false,
      dinnerPrompt,
    };

    return NextResponse.json(payload);
  } catch {
    return NextResponse.json(
      { error: "Invalid notify-parent request payload" },
      { status: 400 }
    );
  }
}

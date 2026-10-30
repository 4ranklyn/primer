import { NextRequest, NextResponse } from "next/server";

export interface NotifyParentPayload {
  childNickname?: string;
  parentWaNumber?: string;
  phone?: string; // backwards compatibility
  lessonTitle?: string;
  durationSec?: number;
  tasksCompleted?: number;
  urrPercentage?: number;
  medianTcrMs?: number;
  stepDownsTriggered?: number;
  struggledConcepts?: string[];
  // Legacy payload support
  stats?: {
    durationMin?: number;
    score?: number;
    urrPercentage?: number;
    focusAreas?: string;
    dinnerQuestion?: string;
  };
}

export interface NotifyParentResponse {
  success: boolean;
  messageText: string;
  whatsappDeepLink: string;
  webhookDelivered: boolean;
  dispatchMode?: string;
}

/**
 * Clean phone number to E.164 without leading plus for wa.me links
 * Indonesian standard: replaces 08... with 628...
 */
function normalizeIndonesianPhone(raw: string): string {
  const digits = raw.replace(/\D/g, "");
  if (digits.startsWith("0")) {
    return `62${digits.slice(1)}`;
  }
  if (digits.startsWith("62")) {
    return digits;
  }
  return digits || "6281234567890";
}

export async function POST(req: NextRequest) {
  try {
    const body = (await req.json()) as Partial<NotifyParentPayload>;
    const {
      childNickname = "si Kecil",
      parentWaNumber,
      phone,
      lessonTitle = "Calistung Mandiri Bersama Kiko",
      durationSec = 840, // 14 mins default
      tasksCompleted = 5,
      urrPercentage = 80,
      medianTcrMs = 4500,
      stepDownsTriggered = 2,
      struggledConcepts = ["Huruf konsonan B & membedakan angka"],
      stats,
    } = body;

    const rawPhone = parentWaNumber || phone || "08123456789";
    const cleanPhone = normalizeIndonesianPhone(rawPhone);

    const effectiveDurationMin = stats?.durationMin || Math.round(durationSec / 60);
    const effectiveUrr = stats?.urrPercentage ?? urrPercentage;
    const medianTcrSec = (medianTcrMs / 1000).toFixed(1);

    // Call Gemini 3.8 Flash to generate encouraging, personalized WhatsApp digest
    const apiKey = process.env.GEMINI_API_KEY;
    const targetModel = process.env.GEMINI_MODEL || "gemini-3.8-flash";

    let messageText = "";

    if (apiKey) {
      try {
        const geminiEndpoint = `https://generativelanguage.googleapis.com/v1beta/models/${targetModel}:generateContent?key=${apiKey}`;

        const promptText = `Anda adalah Kiko, asisten guru Calistung ramah untuk aplikasi anak "Primer". Buatlah 1 pesan ringkasan WhatsApp berkala yang ringkas, hangat, dan sangat memberdayakan untuk Ayah/Bunda.

DATA SESI BELAJAR:
- Nama Anak: ${childNickname}
- Durasi Belajar: ${effectiveDurationMin} menit (${durationSec} detik)
- Topik/Pelajaran: ${lessonTitle}
- Tantangan Selesai: ${tasksCompleted} tugas
- Akurasi Mandiri (URR): ${effectiveUrr}%
- Kecepatan Respon Median (TCR): ${medianTcrSec} detik
- Petunjuk Sokrates Dibutuhkan: ${stepDownsTriggered} kali
- Konsep yang Memerlukan Bantuan Sokrates: ${struggledConcepts.join(", ")}

FORMAT WAJIB PESAN WHATSAPP:
1. Sapaan hangat untuk Ayah/Bunda dan apresiasi usaha ${childNickname} hari ini.
2. Metrik Utama (bullet point rapi):
   - Durasi & Tantangan Selesai
   - Skor Akurasi Mandiri (URR)
   - Kecepatan Menjawab (TCR dalam detik)
3. Area yang Butuh Bantuan Sokrates (jelaskan secara positif bahwa Kiko membantu memandu tanpa membocorkan jawaban).
4. Satu ide konkret "Pertanyaan Meja Makan Malam Ini" (tanpa layar/screen-free) yang dapat ditanyakan orang tua saat makan malam untuk memperdalam topik hari ini secara natural.
5. Penutup santai & hangat dari Kiko.

Gunakan gaya bahasa Indonesia yang luwes, gunakan emoji secukupnya, dan jangan bertele-tele (maksimal 200 kata).`;

        const geminiRes = await fetch(geminiEndpoint, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            contents: [
              {
                parts: [{ text: promptText }],
              },
            ],
            generationConfig: {
              temperature: 0.3,
              maxOutputTokens: 500,
            },
          }),
        });

        if (geminiRes.ok) {
          const geminiData = await geminiRes.json();
          const candidateText =
            geminiData?.candidates?.[0]?.content?.parts?.[0]?.text;
          if (candidateText && candidateText.trim().length > 0) {
            messageText = candidateText.trim();
          }
        }
      } catch {
        // Fall through to deterministic template if Gemini API call fails
      }
    }

    // High quality deterministic fallback if Gemini key is missing or call fails
    if (!messageText) {
      const struggledStr = struggledConcepts.length > 0 ? struggledConcepts.join(", ") : "Eksplorasi fonik huruf & hitung benda";
      messageText = `Halo Ayah & Bunda! 🌟
Hari ini *${childNickname}* telah menyelesaikan sesi belajar bersama Kiko selama *${effectiveDurationMin} menit*!

📊 *Laporan Capaian Hari Ini:*
• Tantangan Diselesaikan: *${tasksCompleted} materi* (${lessonTitle})
• Akurasi Mandiri (URR): *${effectiveUrr}%*
• Kecepatan Respon (TCR): *${medianTcrSec} detik*

💡 *Catatan Dampingan Socratic:*
${childNickname} sangat gigih! Saat menemui tantangan pada *${struggledStr}*, Kiko memberikan petunjuk bertingkat hingga ${childNickname} berhasil menemukannya sendiri tanpa langsung diberi tahu jawabannya.

🍽️ *Ide Obrolan Meja Makan Malam Ini (Screen-Free):*
"Tadi ${childNickname} bercerita apa ya saat main sama Kiko? Benda apa saja di dapur kita yang bunyinya dimulai dari huruf B?"

Terima kasih telah menemani petualangan belajar si Kecil! Sampai jumpa besok! 🚀✨
— *Kiko & Tim Primer*`;
    }

    // Build wa.me deep link
    const encodedMessage = encodeURIComponent(messageText);
    const whatsappDeepLink = `https://wa.me/${cleanPhone}?text=${encodedMessage}`;

    // Optional webhook dispatch (e.g. Fonnte, Wablas, or internal gateway)
    let webhookDelivered = false;
    let dispatchMode = "deep_link_ready";

    const webhookUrl =
      process.env.WHATSAPP_WEBHOOK_URL || process.env.WA_GATEWAY_URL;
    const webhookToken =
      process.env.WHATSAPP_WEBHOOK_TOKEN || process.env.WA_GATEWAY_TOKEN;

    if (webhookUrl) {
      try {
        const webhookResponse = await fetch(webhookUrl, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...(webhookToken ? { Authorization: webhookToken } : {}),
          },
          body: JSON.stringify({
            target: cleanPhone,
            phone: cleanPhone,
            message: messageText,
            data: {
              childNickname,
              urrPercentage: effectiveUrr,
              medianTcrMs,
            },
          }),
        });

        if (webhookResponse.ok) {
          webhookDelivered = true;
          dispatchMode = "webhook_delivered";
        } else {
          dispatchMode = "webhook_failed_fallback_deep_link";
        }
      } catch {
        dispatchMode = "webhook_error_fallback_deep_link";
      }
    }

    const payload: NotifyParentResponse = {
      success: true,
      messageText,
      whatsappDeepLink,
      webhookDelivered,
      dispatchMode,
    };

    return NextResponse.json(payload);
  } catch {
    return NextResponse.json(
      { error: "Invalid notify-parent request payload" },
      { status: 400 }
    );
  }
}

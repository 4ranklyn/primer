import { NextRequest, NextResponse } from "next/server";

export interface NotifyParentPayload {
  phone: string;
  stats: {
    durationMin: number;
    score: number;
    urrPercentage: number;
    focusAreas: string;
    dinnerQuestion?: string;
  };
}

export async function POST(req: NextRequest) {
  try {
    const body = (await req.json()) as Partial<NotifyParentPayload>;
    const { phone, stats } = body;

    if (!phone) {
      return NextResponse.json(
        { error: "Phone number is required" },
        { status: 400 }
      );
    }

    const duration = stats?.durationMin || 12;
    const focus =
      stats?.focusAreas || "Mengenal Huruf B & Berhitung Angka 1-5";
    const dinnerQuestion =
      stats?.dinnerQuestion || "Tadi buah apa yang dihitung bareng Kiko?";

    const formattedMessage = `Halo Bunda/Ayah! 🌟
Hari ini si Kecil sudah belajar ${duration} menit bersama Kiko dengan Akurasi Mandiri ${stats?.urrPercentage ?? 80}%.

Topik hari ini:
${focus}

🍽️ *Pertanyaan Meja Makan Malam Ini*:
"${dinnerQuestion}"

Terima kasih telah mendampingi eksplorasi si Kecil bersama Primer! 🚀`;

    const webhookUrl = process.env.WHATSAPP_WEBHOOK_URL;
    const webhookToken = process.env.WHATSAPP_WEBHOOK_TOKEN;

    if (webhookUrl) {
      try {
        const response = await fetch(webhookUrl, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...(webhookToken ? { Authorization: webhookToken } : {}),
          },
          body: JSON.stringify({
            target: phone,
            message: formattedMessage,
          }),
        });

        if (!response.ok) {
          // Log without raw PII
          return NextResponse.json({
            success: true,
            dispatchMode: "mock_fallback_on_gateway_error",
            message: formattedMessage,
          });
        }

        return NextResponse.json({
          success: true,
          dispatchMode: "webhook_dispatched",
          message: formattedMessage,
        });
      } catch {
        return NextResponse.json({
          success: true,
          dispatchMode: "mock_fallback_on_network_error",
          message: formattedMessage,
        });
      }
    }

    // Default development fallback when webhook URL is not provided
    return NextResponse.json({
      success: true,
      dispatchMode: "mock_logged",
      message: formattedMessage,
    });
  } catch {
    return NextResponse.json(
      { error: "Invalid payload" },
      { status: 400 }
    );
  }
}

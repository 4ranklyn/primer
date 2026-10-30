"use client";

import React from "react";

export const dynamic = "force-dynamic";
export const fetchCache = "force-no-store";

export default function GlobalError({
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="en">
      <body className="flex min-h-screen flex-col items-center justify-center p-4 font-sans">
        <div className="max-w-md w-full bg-white rounded-2xl p-6 shadow-xl border border-gray-100 text-center">
          <h2 className="text-xl font-bold text-gray-800 mb-2">Terjadi Kendala Teknis</h2>
          <p className="text-sm text-gray-600 mb-6">
            Kiko sedang beristirahat sejenak. Silakan muat ulang halaman untuk melanjutkan.
          </p>
          <button
            onClick={() => reset()}
            className="w-full py-3 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-xl shadow-md transition-colors"
          >
            Muat Ulang
          </button>
        </div>
      </body>
    </html>
  );
}

import type { Metadata } from "next";
import localFont from "next/font/local";
import "./globals.css";

const fredoka = localFont({
  src: "./fonts/fredoka.ttf",
  variable: "--font-fredoka",
  display: "swap",
});

const nunito = localFont({
  src: "./fonts/nunito_sans.ttf",
  variable: "--font-nunito",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Kiko Calistung",
  description: "Voice-first Socratic companion for early childhood literacy and numeracy (Calistung) comprehension with 15-minute daily guided inquiry, step-down scaffolding, multimodal SNR fallback cards, and parental cognitive telemetry.",
  openGraph: {
    title: "Kiko Calistung",
    description: "Voice-first Socratic companion for early childhood literacy and numeracy (Calistung) comprehension with 15-minute daily guided inquiry, step-down scaffolding, multimodal SNR fallback cards, and parental cognitive telemetry.",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${fredoka.variable} ${nunito.variable} h-full antialiased`}>
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `
              (function() {
                try {
                  var rawFetch = typeof window !== 'undefined' ? window.fetch : null;
                  var boundFetch = rawFetch ? rawFetch.bind(window) : null;
                  var currentFetch = boundFetch;
                  var desc = {
                    configurable: true,
                    enumerable: true,
                    get: function() { return currentFetch; },
                    set: function(val) { currentFetch = val; }
                  };
                  try {
                    Object.defineProperty(window, 'fetch', desc);
                  } catch (e) {}
                  if (typeof Window !== 'undefined' && Window.prototype) {
                    try {
                      Object.defineProperty(Window.prototype, 'fetch', desc);
                    } catch (e) {}
                  }
                } catch (e) {}

                if (typeof window !== 'undefined') {
                  window.addEventListener('error', function(event) {
                    if (
                      event &&
                      ((event.filename && event.filename.indexOf('chrome-extension://') !== -1) ||
                       (event.message && event.message.indexOf('fetch of #<Window>') !== -1))
                    ) {
                      event.preventDefault();
                      event.stopImmediatePropagation();
                    }
                  }, true);
                }
              })();
            `,
          }}
        />
      </head>
      <body className="min-h-full flex flex-col font-sans">
        {children}
      </body>
    </html>
  );
}

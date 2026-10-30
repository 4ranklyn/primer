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

import ExtensionErrorGuard from "@/components/ExtensionErrorGuard";

const earlyExtensionGuardScript = `
(function() {
  try {
    var rawFetch = window.fetch;
    var currentFetch = rawFetch;
    var desc = {
      configurable: true,
      enumerable: true,
      get: function() { return currentFetch; },
      set: function(fn) { currentFetch = fn; }
    };
    try { Object.defineProperty(window, 'fetch', desc); } catch (_) {}
    try {
      if (typeof Window !== 'undefined' && Window.prototype) {
        Object.defineProperty(Window.prototype, 'fetch', desc);
      }
    } catch (_) {}
  } catch (_) {}

  var isIgnored = function(msg, file, stack) {
    var m = (msg || '').toLowerCase();
    var f = (file || '').toLowerCase();
    var s = (stack || '').toLowerCase();
    return f.indexOf('chrome-extension://') !== -1 ||
           s.indexOf('chrome-extension://') !== -1 ||
           m.indexOf('cannot set property fetch') !== -1 ||
           m.indexOf('only a getter') !== -1 ||
           m.indexOf('fetch of #<window>') !== -1;
  };

  var prevOnError = window.onerror;
  window.onerror = function(message, source, lineno, colno, error) {
    var msg = String(message || '');
    var src = String(source || '');
    var stk = (error && error.stack) ? String(error.stack) : '';
    if (isIgnored(msg, src, stk)) {
      return true;
    }
    if (typeof prevOnError === 'function') {
      return prevOnError.apply(this, arguments);
    }
    return false;
  };

  window.addEventListener('error', function(event) {
    var msg = event.message || (event.error && event.error.message) || '';
    var file = event.filename || '';
    var stack = (event.error && event.error.stack) || '';
    if (isIgnored(msg, file, stack)) {
      if (event.stopImmediatePropagation) event.stopImmediatePropagation();
      if (event.stopPropagation) event.stopPropagation();
      if (event.preventDefault) event.preventDefault();
    }
  }, true);

  window.addEventListener('unhandledrejection', function(event) {
    var reason = event.reason;
    var msg = (reason && reason.message) || String(reason || '');
    var stack = (reason && reason.stack) || '';
    if (isIgnored(msg, '', stack)) {
      if (event.stopImmediatePropagation) event.stopImmediatePropagation();
      if (event.stopPropagation) event.stopPropagation();
      if (event.preventDefault) event.preventDefault();
    }
  }, true);
})();
`;

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${fredoka.variable} ${nunito.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col font-sans">
        <script
          dangerouslySetInnerHTML={{
            __html: earlyExtensionGuardScript,
          }}
        />
        <ExtensionErrorGuard />
        {children}
      </body>
    </html>
  );
}

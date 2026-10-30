"use client";

import { useEffect } from "react";

export default function ExtensionErrorGuard() {
  useEffect(() => {
    try {
      const rawFetch = window.fetch;
      let currentFetch = rawFetch;
      const desc = {
        configurable: true,
        enumerable: true,
        get: () => currentFetch,
        set: (val: typeof window.fetch) => {
          currentFetch = val;
        },
      };
      try {
        Object.defineProperty(window, "fetch", desc);
      } catch {}
      if (typeof Window !== "undefined" && Window.prototype) {
        try {
          Object.defineProperty(Window.prototype, "fetch", desc);
        } catch {}
      }
    } catch {}

    const isIgnored = (msg?: string, file?: string, stack?: string) => {
      const m = (msg || "").toLowerCase();
      const f = (file || "").toLowerCase();
      const s = (stack || "").toLowerCase();
      return (
        f.includes("chrome-extension://") ||
        s.includes("chrome-extension://") ||
        m.includes("cannot set property fetch") ||
        m.includes("only a getter") ||
        m.includes("fetch of #<window>")
      );
    };

    const errorHandler = (event: ErrorEvent) => {
      const msg = event?.message || (event?.error && event.error.message) || "";
      const file = event?.filename || "";
      const stack = (event?.error && event.error.stack) || "";
      if (isIgnored(msg, file, stack)) {
        event.stopImmediatePropagation?.();
        event.stopPropagation?.();
        event.preventDefault?.();
      }
    };

    const rejectionHandler = (event: PromiseRejectionEvent) => {
      const reason = event?.reason;
      const msg = (reason && reason.message) || String(reason || "");
      const stack = (reason && reason.stack) || "";
      if (isIgnored(msg, "", stack)) {
        event.stopImmediatePropagation?.();
        event.stopPropagation?.();
        event.preventDefault?.();
      }
    };

    window.addEventListener("error", errorHandler, true);
    window.addEventListener("unhandledrejection", rejectionHandler, true);

    return () => {
      window.removeEventListener("error", errorHandler, true);
      window.removeEventListener("unhandledrejection", rejectionHandler, true);
    };
  }, []);

  return null;
}


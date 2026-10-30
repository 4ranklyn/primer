#!/bin/sh
set -e

# Support dynamic Cloud Run $PORT (defaults to 8080 if not set)
TARGET_PORT="${PORT:-8080}"
echo "[Production Container] Configuring NGINX on port ${TARGET_PORT}..."
sed -i "s/8080/${TARGET_PORT}/g" /etc/nginx/nginx.conf

# Force Next.js internal server to bind strictly to port 3000 on localhost
export PORT=3000
export HOSTNAME="127.0.0.1"

echo "[Production Container] Starting internal Next.js application on 127.0.0.1:3000..."
if [ -f /app/server.js ]; then
  node /app/server.js &
elif [ -f /app/web/server.js ]; then
  node /app/web/server.js &
else
  npx next start -H 127.0.0.1 -p 3000 &
fi

NEXT_PID=$!

# Trap signals for graceful shutdown of both services
trap "kill -TERM $NEXT_PID 2>/dev/null; nginx -s quit 2>/dev/null || true; exit 0" SIGTERM SIGINT

echo "[Production Container] Starting NGINX reverse proxy (0.0.0.0:${TARGET_PORT} -> 127.0.0.1:3000)..."
nginx -g "daemon off;" &
NGINX_PID=$!

# Health monitoring loop: keep container running while both services are alive
while kill -0 "$NEXT_PID" 2>/dev/null && kill -0 "$NGINX_PID" 2>/dev/null; do
  sleep 1
done

echo "[Production Container] One of the background processes terminated. Shutting down..."
kill -TERM "$NEXT_PID" 2>/dev/null || true
kill -TERM "$NGINX_PID" 2>/dev/null || true
exit 1

# Stage 1: Dependencies
FROM node:20-alpine AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

COPY web/package.json ./
RUN npm install

# Stage 2: Builder
FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY web ./

ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# Stage 3: Production Runner (Google Cloud Run optimized with NGINX reverse proxy)
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=8080

RUN apk add --no-cache nginx libc6-compat

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Set up NGINX run and cache directories with appropriate permissions
RUN mkdir -p /var/log/nginx /var/lib/nginx /tmp/client_temp /tmp/proxy_temp_path && \
    chown -R nextjs:nodejs /var/log/nginx /var/lib/nginx /tmp /etc/nginx

COPY nginx.conf /etc/nginx/nginx.conf
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

COPY --from=builder /app/public ./public
COPY --from=builder /app/public ./web/public

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./web/.next/static

USER nextjs

EXPOSE 8080
EXPOSE 3000

ENTRYPOINT ["/app/entrypoint.sh"]

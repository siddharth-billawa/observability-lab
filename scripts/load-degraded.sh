#!/usr/bin/env bash
set -euo pipefail
restore(){ PAYMENT_DELAY_MS=0 PAYMENT_FAIL=false docker compose up -d --force-recreate payment-service >/dev/null; }
trap restore EXIT
PAYMENT_DELAY_MS=${PAYMENT_DELAY_MS:-1200} PAYMENT_FAIL=false docker compose up -d --force-recreate payment-service
ready=false
for i in $(seq 1 40); do if curl -fsS http://localhost:${PAYMENT_PORT:-18081}/actuator/health >/dev/null 2>&1; then ready=true; break; fi; sleep 2; done
if [ "$ready" != true ]; then echo "Payment service did not become healthy within 80 seconds" >&2; exit 1; fi
docker compose --profile load run --rm -e BASE_URL=http://order-service:8080 -e VUS=${VUS:-3} -e DURATION=${DURATION:-90s} k6 run /scripts/degraded-dependency.js

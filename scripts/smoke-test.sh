#!/usr/bin/env bash
set -euo pipefail
base=${BASE_URL:-http://localhost:18080}
grafana_user=${GRAFANA_USER:-admin}; grafana_pass=${GRAFANA_PASSWORD:-admin}
wait_url(){ local url=$1; for i in $(seq 1 60); do if curl -fsS --max-time 3 "$url" >/dev/null 2>&1; then return 0; fi; sleep 2; done; echo "Timed out waiting for $url" >&2; exit 1; }
wait_match(){ local url=$1 pattern=$2; for i in $(seq 1 30); do local response; response=$(curl -fsS --max-time 3 "$url" 2>/dev/null || true); if printf '%s' "$response" | rg -q "$pattern"; then return 0; fi; sleep 2; done; echo "Expected '$pattern' in $url" >&2; exit 1; }
wait_url "$base/actuator/health"; wait_url http://localhost:${PAYMENT_PORT:-18081}/actuator/health
order=$(curl -fsS -H 'Content-Type: application/json' -d '{"item":"smoke-widget","amount":12.50}' "$base/api/orders")
echo "Created order: $order"
status=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/demo/failure"); test "$status" = 500 || { echo "Expected demo failure 500, received $status" >&2; exit 1; }
payment_status=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/demo/payment-failure?delayMs=10"); test "$payment_status" = 502 || { echo "Expected controlled payment failure 502, received $payment_status" >&2; exit 1; }
curl -fsS "$base/actuator/prometheus" | rg 'orders_accepted_total|orders_processing_seconds|payments_attempts_total' >/dev/null
curl -fsS http://localhost:${PAYMENT_PORT:-18081}/actuator/prometheus | rg 'payment_requests_total' >/dev/null
wait_match 'http://localhost:9090/api/v1/query?query=up%7Bjob%3D~%22order-service%7Cpayment-service%22%7D' 'order-service'
wait_match 'http://localhost:9090/api/v1/query?query=up%7Bjob%3D~%22order-service%7Cpayment-service%22%7D' 'payment-service'
wait_match 'http://localhost:9090/api/v1/rules' 'ElevatedServerErrorRate'
wait_match 'http://localhost:16686/api/services' 'order-service'
wait_match 'http://localhost:16686/api/traces?service=order-service&operation=http%20post%20%2Fapi%2Forders&limit=5&lookback=1h' 'payment-service'
wait_url http://localhost:3100/ready; wait_url http://localhost:3000/api/health; wait_url http://localhost:9093/api/v2/status
curl -fsS -u "$grafana_user:$grafana_pass" 'http://localhost:3000/api/datasources' | rg 'prometheus|loki|jaeger' >/dev/null
curl -fsS -u "$grafana_user:$grafana_pass" 'http://localhost:3000/api/search?type=dash-db' | rg 'application-health|business-dependencies' >/dev/null
# Allow Loki a bounded window to ingest the application stdout logs.
wait_match 'http://localhost:3100/loki/api/v1/labels' 'service_name'
now=$(python3 -c 'import time; print(int(time.time()*1_000_000_000))'); start=$((now-300000000000))
wait_match "http://localhost:3100/loki/api/v1/query_range?query=%7Bservice_name%3D%22order-service%22%7D%20%7C%3D%20%22Order%20processing%20completed%22&start=$start&end=$now&limit=20" 'Order processing completed'
printf 'Smoke checks passed for app health, metrics, Prometheus scrape targets, traces, Grafana provisioning, Loki readiness, and alerting. Query Loki in Grafana Explore to inspect the specific smoke request.\n'

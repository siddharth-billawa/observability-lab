# Experiment 04: Diagnosing latency

## Learning objectives

Attribute delay to the payment dependency.

## Concepts

Compare end-to-end timer to server and client spans. The payment service delay is bounded to five seconds.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
curl -s 'http://localhost:18080/api/demo/payment-failure?delayMs=1800'
curl -s -H 'Content-Type: application/json' -d '{"item":"slow","amount":20}' http://localhost:18080/api/orders

```

## Expected observations and where to look

Grafana p95 panels and Jaeger: find the longest child span. Filter traces by order-service.

## Questions

What share of total duration belongs to the downstream span? Did throughput fall?

## Explanation

The demo failure route controls a failing downstream call. Payment service environment values also support PAYMENT_DELAY_MS and PAYMENT_FAIL in a local run.

## Common mistakes and troubleshooting

Request timeouts are bounded by HTTP client defaults; very long injected delays may exhaust servlet threads in this small lab.

## Cleanup

Restore defaults with docker compose restart payment-service.

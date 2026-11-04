# Experiment 01: First telemetry

## Learning objectives

Trace one request through every signal.

## Concepts

POST /api/orders saves to PostgreSQL, calls payment over HTTP, and emits metrics, JSON stdout logs, and a trace. Prometheus scrapes /actuator/prometheus; Alloy tails Docker logs and sends to Loki; OTLP flows through Collector to Jaeger.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
curl -i -H 'Content-Type: application/json' -d '{"item":"notebook","amount":8.75}' http://localhost:18080/api/orders
curl -s http://localhost:18080/actuator/prometheus | grep orders_created

```

## Expected observations and where to look

Grafana Explore: Prometheus orders_accepted_total; Loki {service_name="order-service"} |= "order"; Jaeger search service order-service. Follow the trace to the payment-service span.

## Questions

Why does a trace have multiple spans? Which parts are cumulative versus sampled events?

## Explanation

Metric counters survive scrape intervals as process-lifetime totals; Prometheus derives rates. Trace sampling is 100% locally. Logs retain structured JSON fields and traceId/spanId when a span is active.

## Common mistakes and troubleshooting

Wait for services to be healthy (`docker compose ps`). A log in Docker does not prove Loki ingestion; query Loki separately.

## Cleanup

No cleanup required.

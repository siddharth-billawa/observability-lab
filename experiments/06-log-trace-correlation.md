# Experiment 06: Cross-signal correlation

## Learning objectives

Move between structured logs and traces by trace ID.

## Concepts

Spring structured JSON includes MDC traceId/spanId during active spans. Alloy parses level and trace identifiers but indexes only level plus Docker discovery labels.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
curl -s -H 'Content-Type: application/json' -d '{"item":"correlate","amount":21}' http://localhost:18080/api/orders

```

## Expected observations and where to look

Grafana Explore Loki: {service_name="order-service", level="INFO"}; open a log JSON and copy traceId into Jaeger Trace ID search.

## Questions

Are traceId values Loki labels or log content? Why?

## Explanation

Trace IDs are high-cardinality, so they remain structured log content rather than indexed Loki labels. Jaeger stores trace topology and timing; logs provide event detail.

## Common mistakes and troubleshooting

If traceId is missing, ensure logs occur inside an instrumented request and inspect JSON casing.

## Cleanup

No cleanup required.

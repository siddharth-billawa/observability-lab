# Experiment 10: Observability infrastructure failure

## Learning objectives

Observe business behavior during telemetry and dependency outages.

## Concepts

Metrics and log/trace export are telemetry paths; payment is a business dependency. A stopped telemetry backend should not normally fail an order request, while a payment outage changes its outcome. Telemetry buffers and exporter behavior have limits, so short outages can still lose spans or logs.

## Prerequisites and commands

Ensure `make up` completed and both applications are healthy. Run these one at a time:

```bash
# Stop the Collector, then create an order. Expect outcome PAID.
docker compose stop otel-collector
curl -s -H 'Content-Type: application/json' -d '{"item":"collector-down","amount":18}' http://localhost:18080/api/orders
docker compose start otel-collector

# Stop the payment dependency, then create an order. Expect PAYMENT_FAILED.
docker compose stop payment-service
curl -s -H 'Content-Type: application/json' -d '{"item":"payment-down","amount":19}' http://localhost:18080/api/orders
docker compose start payment-service
```

## Expected observations and where to look

The Collector outage should still return a successful order if payment is healthy; the trace sent during the outage may be lost. A payment outage should still persist and return the order with `PAYMENT_FAILED`. Check order-service logs, the database through `GET /api/orders/{id}`, Collector logs, and Jaeger after recovery.

## Questions

Which component is required to fulfill the request? What telemetry is lost during an outage? What happens if the telemetry queue fills during a long outage?

## Explanation

The order is persisted before its payment call. Collector export is asynchronous and best effort, but buffers are finite; this is not an absolute isolation guarantee. Stopping payment causes a persisted `PAYMENT_FAILED` result, while stopping the Collector should not affect payment or persistence.

## Common mistakes and troubleshooting

Warm both application health endpoints before starting. A just-started payment service can briefly reset the first request; retry after readiness. After restarting the Collector, allow it to reconnect before expecting new traces. Outage-time telemetry is not guaranteed to be backfilled.

## Cleanup

```bash
docker compose start otel-collector payment-service
```

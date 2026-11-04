# Experiment 03: Distributed tracing

## Learning objectives

Follow the HTTP parent-child relationship.

## Concepts

Spring Boot instruments inbound HTTP and the RestClient.Builder outbound HTTP call. W3C trace context is injected to payment service. The order save is represented by service code timing; JDBC spans may not be emitted by default.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
curl -s -H 'Content-Type: application/json' -d '{"item":"trace","amount":20}' http://localhost:18080/api/orders

```

## Expected observations and where to look

Jaeger at http://localhost:16686: choose order-service and find the latest POST /api/orders trace. Inspect the child HTTP client and payment server spans.

## Questions

Which span is the client? Does the payment server span share the same trace ID?

## Explanation

The OTLP exporter sends to Collector on the Compose network; Collector exports to Jaeger. No Java agent is installed, avoiding duplicate instrumentation.

## Common mistakes and troubleshooting

If no traces appear, check collector logs and the configured OTLP endpoint. Verify both services sampled and share context.

## Cleanup

No cleanup required.

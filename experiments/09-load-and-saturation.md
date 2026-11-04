# Experiment 09: Load and saturation

## Learning objectives

Compare normal, burst, and degraded traffic.

## Concepts

Load generator uses modest local VUs. A single laptop result is illustrative, not capacity planning.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
make load-normal
make load-burst
# Inject service delay for a local experiment:
docker compose stop payment-service
# Or use payment delay config in a local IDE run with DEMO_ENABLED=true
```

## Expected observations and where to look

Watch application health dashboard, JVM, DB pool, errors, throughput and traces.

## Questions

Does latency rise before errors? Does throughput remain linear? What recovers after burst?

## Explanation

The order persists before calling payment, and synchronous downstream latency occupies request threads and client time. A small local database and laptop are confounders.

## Common mistakes and troubleshooting

Check CPU/memory with Docker Desktop; reduce VUs if resource pressure makes observations noisy.

## Cleanup

docker compose start payment-service; stop load.

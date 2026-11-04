# Experiment 02: Counters, gauges, and histograms

## Learning objectives

Inspect metric semantics and histogram buckets.

## Concepts

Counters only increase; orders.pending is a gauge; timer histogram buckets support fleet aggregation. A computed quantile is an estimate.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
for i in {1..10}; do curl -s -o /dev/null -w "%{http_code}\n" -H 'Content-Type: application/json' -d '{"item":"metric","amount":15}' http://localhost:18080/api/orders; done
curl -s http://localhost:18080/actuator/prometheus | grep -E 'orders_(accepted|pending|processing)'

```

## Expected observations and where to look

Prometheus: rate(orders_accepted_total[5m]); histogram_quantile(0.95, sum by (le) (rate(orders_processing_seconds_bucket[5m]))). Compare rate with _count.

## Questions

What happens to the pending gauge after requests finish? How do histogram buckets combine across replicas?

## Explanation

Timers record count and sum; publishing histogram buckets enables aggregated quantiles. Never average per-instance p95 values: construct a quantile from summed buckets.

## Common mistakes and troubleshooting

If the _bucket series are absent, inspect management.metrics.distribution.percentiles-histogram and confirm scrape success.

## Cleanup

No cleanup required.

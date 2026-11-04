# Experiment 08: Metric cardinality

## Learning objectives

Observe how a label with a new value creates a separate Prometheus time series.

## Concepts

A label per order ID creates one series per order and multiplies memory and index costs. The demo endpoint emits synthetic `order_id` labels only when `DEMO_ENABLED=true`. It is capped at 500 series per process and 100 per call.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
curl -s 'http://localhost:18080/api/demo/cardinality?series=100'
curl -s http://localhost:18080/actuator/prometheus | grep demo_bad_orders_total
curl -sG http://localhost:9090/api/v1/query --data-urlencode 'query=count(demo_bad_orders_total)'
```

## Expected observations and where to look

The endpoint reports 100 new synthetic label values. Prometheus has up to 100 distinct `order_id` series. Run the endpoint again to increase the count to 200, then inspect Prometheus Graph.

## Questions

Why is `outcome` safe here while order ID is not? How would you investigate one order instead?

## Explanation

Metrics are aggregated service-level signals. Use logs, traces, or the database for individual identifiers. The synthetic label route is intentionally bad instrumentation in a capped local-only controller.

## Common mistakes and troubleshooting

The counter is capped at 500 series per service process. Further requests return 429; restart only the order service to reset the in-memory series. Do not use real order IDs as labels.

## Cleanup

No persistent data cleanup is needed. Prometheus will age out samples according to its local retention. The demonstration endpoint is absent unless the local demo flag is enabled.

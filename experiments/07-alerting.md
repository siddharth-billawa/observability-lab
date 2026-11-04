# Experiment 07: Alerting

## Learning objectives

Observe pending, firing, and resolved states.

## Concepts

Prometheus evaluates every 10s. ElevatedServerErrorRate needs >10% errors and a five-minute request rate above 0.2 requests/sec for 2m; this minimum volume avoids firing on a single isolated failure. ApplicationUnavailable fires after 1m down.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
for i in {1..1500}; do curl -s -o /dev/null http://localhost:18080/api/demo/failure; done
for i in {1..130}; do curl -s -o /dev/null http://localhost:18080/api/demo/failure; sleep 1; done
```

## Expected observations and where to look

Prometheus Alerts at http://localhost:9090/alerts; Alertmanager at http://localhost:9093; webhook payload in `docker compose logs alert-sink`.

## Questions

How does the pending period suppress one-off test errors? What does resolved mean?

## Explanation

Alert condition is a query; pending is condition true but for duration not elapsed; firing has elapsed; resolved is no longer true. Local receiver is an HTTP sink, no external endpoint.

## Common mistakes and troubleshooting

Ensure enough traffic exists for a rate denominator. Wait beyond pending period.

## Cleanup

Stop generating failures and allow the 5m rate window to clear. Recent successful load can dilute the ratio; the first burst above raises the numerator past the threshold, and the one-per-second loop holds it through the pending period.

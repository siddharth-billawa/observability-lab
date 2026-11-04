# Experiment 05: Diagnosing failures

## Learning objectives

Distinguish invalid input, payment rejection, and unexpected exceptions.

## Concepts

Validation returns 400; amount above 5000 is a business decline; payment technical errors are persisted as PAYMENT_FAILED; demo exception is 500.

## Prerequisites and commands

Ensure `make up` completed, then run:

```bash
curl -i -H 'Content-Type: application/json' -d '{"item":"","amount":-1}' http://localhost:18080/api/orders
curl -i -H 'Content-Type: application/json' -d '{"item":"large","amount":5001}' http://localhost:18080/api/orders
curl -i http://localhost:18080/api/demo/failure
curl -i http://localhost:18080/api/demo/payment-failure
```

## Expected observations and where to look

Compare HTTP status, saved order outcome, service logs, and Jaeger error tags.

## Questions

Which are expected business outcomes versus technical errors?

## Explanation

Business declines are returned with HTTP 201 and outcome DECLINED because order creation succeeded. The demo payment path returns 502. No credentials or request bodies are logged.

## Common mistakes and troubleshooting

Look up the stored order after a payment error; the initial insert commits before the downstream call.

## Cleanup

No cleanup required.

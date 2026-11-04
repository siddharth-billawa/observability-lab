package lab.orders;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);
    private final RestClient payment;
    private final MeterRegistry meters;
    private final Counter failedPayments;
    private final AtomicInteger generated = new AtomicInteger();

    public DemoController(RestClient payment, MeterRegistry meters) {
        this.payment = payment;
        this.meters = meters;
        this.failedPayments = Counter.builder("payments.attempts").description("Payment attempts").tag("outcome", "failure").register(meters);
    }

    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(name = "ms", defaultValue = "1200") long ms) throws InterruptedException {
        long safe = Math.max(0, Math.min(5000, ms));
        Thread.sleep(safe);
        return Map.of("sleptMs", safe);
    }

    @GetMapping("/failure")
    public Map<String, Object> failure() {
        throw new IllegalStateException("Demonstration failure");
    }

    @GetMapping("/payment-failure")
    public ResponseEntity<Map<String, String>> paymentFailure(@RequestParam(name = "delayMs", defaultValue = "800") long delayMs) {
        try {
            payment.post().uri(uri -> uri.path("/api/payments").queryParam("fail", "true").queryParam("delayMs", Math.max(0, Math.min(5000, delayMs))).build()).body(Map.of("orderId", 0, "amount", BigDecimal.ONE)).retrieve().toBodilessEntity();
            return ResponseEntity.ok(Map.of("outcome", "UNEXPECTED_SUCCESS"));
        } catch (Exception exception) {
            failedPayments.increment();
            log.atWarn().addKeyValue("event", "demo.payment-failure").addKeyValue("exception.type", exception.getClass().getSimpleName()).log("Controlled downstream failure returned as 502");
            return ResponseEntity.status(502).body(Map.of("outcome", "PAYMENT_FAILED"));
        }
    }

    @GetMapping("/cardinality")
    public Map<String, Object> cardinality(@RequestParam(name = "series", defaultValue = "25") int series) {
        int amount = Math.max(1, Math.min(100, series));
        int total = generated.updateAndGet(current -> current + amount <= 500 ? current + amount : -1);
        if (total < 0)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Demo capped at 500 synthetic series; restart to reset");
        int start = total - amount;
        for (int i = 1; i <= amount; i++) {
            Counter.builder("demo.bad.orders").description("Controlled cardinality demonstration; do not copy this label design").tag("order_id", "synthetic-" + (start + i)).register(meters).increment();
        }
        return Map.of("createdSeries", amount, "totalSyntheticSeries", total, "metric", "demo_bad_orders_total");
    }
}

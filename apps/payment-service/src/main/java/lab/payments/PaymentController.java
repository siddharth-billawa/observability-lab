package lab.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final long delay;
    private final boolean fail, demoEnabled;

    public PaymentController(@Value("${demo.payment.delay-ms:0}") long delay, @Value("${demo.payment.fail:false}") boolean fail, @Value("${demo.enabled:false}") boolean demoEnabled) {
        this.delay = delay;
        this.fail = fail;
        this.demoEnabled = demoEnabled;
    }

    public record Request(long orderId, BigDecimal amount) {
    }

    public record Result(boolean approved, String paymentId) {
    }

    @PostMapping
    @BusinessTelemetry("payment.process")
    public Result pay(@RequestBody Request request, @RequestParam(name = "fail", defaultValue = "false") boolean fail, @RequestParam(name = "delayMs", defaultValue = "0") long delayMs) throws InterruptedException {
        Thread.sleep(demoEnabled ? Math.max(0, Math.min(5000, delayMs > 0 ? delayMs : delay)) : 0);
        if (demoEnabled && (fail || this.fail)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Simulated payment failure");
        }
        if (request.amount().compareTo(new BigDecimal("5000")) > 0) {
            return new Result(false, null);
        }
        return new Result(true, UUID.randomUUID().toString());
    }
}

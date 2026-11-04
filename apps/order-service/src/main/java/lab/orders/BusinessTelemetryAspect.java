package lab.orders;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class BusinessTelemetryAspect {
    private static final Logger log = LoggerFactory.getLogger(BusinessTelemetryAspect.class);

    private final Counter accepted;
    private final Counter paymentSuccess;
    private final Counter paymentFailure;
    private final Counter paid;
    private final Counter declined;
    private final Counter failed;
    private final Timer processing;
    private final AtomicInteger pending = new AtomicInteger();
    private final MeterRegistry meters;
    private final ObservationRegistry observations;

    public BusinessTelemetryAspect(MeterRegistry meters, ObservationRegistry observations) {
        this.meters = meters;
        this.observations = observations;
        accepted = Counter.builder("orders.accepted").description("Persisted orders accepted by the order service").register(meters);
        paymentSuccess = Counter.builder("payments.attempts").description("Payment attempts").tag("outcome", "success").register(meters);
        paymentFailure = Counter.builder("payments.attempts").description("Payment attempts").tag("outcome", "failure").register(meters);
        paid = Counter.builder("orders.outcomes").description("Completed order outcomes").tag("outcome", "paid").register(meters);
        declined = Counter.builder("orders.outcomes").description("Completed order outcomes").tag("outcome", "declined").register(meters);
        failed = Counter.builder("orders.outcomes").description("Completed order outcomes").tag("outcome", "payment_failed").register(meters);
        processing = Timer.builder("orders.processing").description("End-to-end order processing duration").publishPercentileHistogram().register(meters);
        Gauge.builder("orders.pending", pending, AtomicInteger::get).description("Order requests currently being processed").register(meters);
    }

    @Around("@annotation(telemetry)")
    public Object instrument(ProceedingJoinPoint invocation, BusinessTelemetry telemetry) throws Throwable {
        if (!"order.create".equals(telemetry.value())) return invocation.proceed();

        pending.incrementAndGet();
        Timer.Sample sample = Timer.start(meters);
        Observation observation = Observation.createNotStarted("order.process", observations)
                .lowCardinalityKeyValue("item_category", "demo").start();
        try {
            Object result = invocation.proceed();
            if (result instanceof ResponseEntity<?> response && response.getBody() instanceof OrderController.OrderResponse order) {
                accepted.increment();
                recordOutcome(order);
            }
            return result;
        } finally {
            observation.stop();
            sample.stop(processing);
            pending.decrementAndGet();
        }
    }

    private void recordOutcome(OrderController.OrderResponse order) {
        switch (order.outcome()) {
            case "PAID" -> {
                paymentSuccess.increment();
                paid.increment();
            }
            case "DECLINED" -> {
                paymentFailure.increment();
                declined.increment();
            }
            default -> {
                paymentFailure.increment();
                failed.increment();
                log.atWarn().addKeyValue("event", "payment.failed").addKeyValue("orderId", order.id()).log("Payment request failed");
            }
        }
        log.atInfo().addKeyValue("event", "order.processed").addKeyValue("orderId", order.id())
                .addKeyValue("outcome", order.outcome()).log("Order processing completed");
    }
}

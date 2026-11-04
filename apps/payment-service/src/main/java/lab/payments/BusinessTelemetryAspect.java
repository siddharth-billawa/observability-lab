package lab.payments;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class BusinessTelemetryAspect {
    private static final Logger log = LoggerFactory.getLogger(BusinessTelemetryAspect.class);
    private final Counter success;
    private final Counter errors;

    public BusinessTelemetryAspect(MeterRegistry meters) {
        success = Counter.builder("payment.requests").description("Payment service requests").tag("outcome", "success").register(meters);
        errors = Counter.builder("payment.requests").description("Payment service requests").tag("outcome", "failure").register(meters);
    }

    @Around("@annotation(telemetry)")
    public Object instrument(ProceedingJoinPoint invocation, BusinessTelemetry telemetry) throws Throwable {
        if (!"payment.process".equals(telemetry.value())) return invocation.proceed();
        PaymentController.Request request = (PaymentController.Request) invocation.getArgs()[0];
        try {
            Object result = invocation.proceed();
            if (result instanceof PaymentController.Result payment) {
                if (payment.approved()) {
                    success.increment();
                    log.atInfo().addKeyValue("event", "payment.approved").addKeyValue("orderId", request.orderId()).log("Payment approved");
                } else {
                    errors.increment();
                    log.atInfo().addKeyValue("event", "payment.declined").addKeyValue("orderId", request.orderId()).log("Payment declined by demo limit");
                }
            }
            return result;
        } catch (ResponseStatusException exception) {
            errors.increment();
            log.atWarn().addKeyValue("event", "payment.failed").addKeyValue("orderId", request.orderId()).log("Simulated payment failure");
            throw exception;
        }
    }
}

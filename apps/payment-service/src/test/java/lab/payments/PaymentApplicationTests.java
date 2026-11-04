package lab.payments;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentApplicationTests {
    @Autowired
    MockMvc mvc;
    @Autowired
    MeterRegistry metrics;

    @Test
    void approvesPayment() throws Exception {
        mvc.perform(post("/api/payments").contentType("application/json").content("{\"orderId\":1,\"amount\":25}")).andExpect(status().isOk()).andExpect(jsonPath("$.approved").value(true));
        assertThat(metrics.counter("payment.requests", "outcome", "success").count()).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void healthIsAvailable() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}

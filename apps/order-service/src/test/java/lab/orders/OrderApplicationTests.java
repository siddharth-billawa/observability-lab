package lab.orders;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "demo.enabled=true"})
@AutoConfigureMockMvc
class OrderApplicationTests {
    static final AtomicBoolean failPayment = new AtomicBoolean();
    static final HttpServer payment;

    static {
        try {
            payment = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            payment.createContext("/api/payments", e -> {
                boolean fail = failPayment.get();
                byte[] body = (fail ? "simulated" : "{\"approved\":true,\"paymentId\":\"test\"}").getBytes();
                e.getResponseHeaders().add("Content-Type", "application/json");
                e.sendResponseHeaders(fail ? 503 : 200, body.length);
                e.getResponseBody().write(body);
                e.close();
            });
            payment.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("payment.base-url", () -> "http://127.0.0.1:" + payment.getAddress().getPort());
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    MeterRegistry metrics;

    @BeforeEach
    void reset() {
        failPayment.set(false);
    }

    @AfterAll
    static void stop() {
        payment.stop(0);
    }

    @Test
    void createsAndRetrievesOrder() throws Exception {
        var result = mvc.perform(post("/api/orders").contentType("application/json").content("{\"item\":\"book\",\"amount\":12.50}")).andExpect(status().isCreated()).andExpect(jsonPath("$.outcome").value("PAID")).andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
        mvc.perform(get("/api/orders/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.item").value("book"));
        assertThat(metrics.counter("orders.accepted").count()).isGreaterThanOrEqualTo(1.0);
        assertThat(metrics.counter("payments.attempts", "outcome", "success").count()).isGreaterThanOrEqualTo(1.0);
        assertThat(metrics.get("orders.pending").gauge().value()).isEqualTo(0.0);
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/orders").contentType("application/json").content("{\"item\":\"\",\"amount\":0}")).andExpect(status().isBadRequest());
    }

    @Test
    void recordsPaymentFailureWithoutLosingOrder() throws Exception {
        failPayment.set(true);
        mvc.perform(post("/api/orders").contentType("application/json").content("{\"item\":\"book\",\"amount\":12.50}")).andExpect(status().isCreated()).andExpect(jsonPath("$.outcome").value("PAYMENT_FAILED"));
    }

    @Test
    void exposesHealthAndControlledFailure() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/api/demo/failure")).andExpect(status().is5xxServerError());
    }
}

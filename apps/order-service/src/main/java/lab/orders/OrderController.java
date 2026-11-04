package lab.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderRepository repo;
    private final RestClient payment;

    public OrderController(OrderRepository repo, RestClient payment) {
        this.repo = repo;
        this.payment = payment;
    }

    public record CreateRequest(@NotBlank @Size(max = 80) String item,
                                @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {
    }

    public record PaymentResult(boolean approved, String paymentId) {
    }

    public record OrderResponse(Long id, String item, BigDecimal amount, String outcome, java.time.Instant createdAt) {
        static OrderResponse of(Order o) {
            return new OrderResponse(o.getId(), o.getItem(), o.getAmount(), o.getOutcome(), o.getCreatedAt());
        }
    }

    @PostMapping("/orders")
    @BusinessTelemetry("order.create")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateRequest request) {
        Order order = repo.saveAndFlush(new Order(request.item(), request.amount()));
        try {
            PaymentResult result = payment.post().uri("/api/payments").body(Map.of("orderId", order.getId(), "amount", request.amount())).retrieve().body(PaymentResult.class);
            order.setOutcome(result != null && result.approved() ? "PAID" : "DECLINED");
        } catch (Exception ex) {
            order.setOutcome("PAYMENT_FAILED");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.of(repo.save(order)));
    }

    @GetMapping("/orders/{id}")
    public OrderResponse get(@PathVariable("id") long id) {
        return repo.findById(id).map(OrderResponse::of).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @GetMapping("/orders")
    public List<OrderResponse> list() {
        return repo.findTop50ByOrderByCreatedAtDesc().stream().map(OrderResponse::of).toList();
    }

}

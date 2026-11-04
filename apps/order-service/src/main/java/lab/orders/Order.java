package lab.orders;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String item;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 24)
    private String outcome;
    @Column(nullable = false)
    private Instant createdAt;

    protected Order() {
    }

    public Order(String item, BigDecimal amount) {
        this.item = item;
        this.amount = amount;
        this.outcome = "PENDING";
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getOutcome() {
        return outcome;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}

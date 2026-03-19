package com.regalos.back_regalos.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotalAmount;

    @Column(name = "delivery_cost", nullable = false, precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal deliveryCost = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "total_cost_amount", nullable = false, precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal totalCostAmount = BigDecimal.ZERO;

    @Column(length = 10)
    @Builder.Default
    private String currency = "COP";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_city_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DeliveryCity deliveryCity;

    @Column(name = "delivery_neighborhood", length = 150)
    private String deliveryNeighborhood;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_time_range", length = 50)
    private String deliveryTimeRange;

    @Column(name = "gift_sender_name", length = 100)
    private String giftSenderName;

    @Column(name = "gift_receiver_name", length = 100)
    private String giftReceiverName;

    @Column(name = "gift_message", columnDefinition = "TEXT")
    private String giftMessage;

    @Column(name = "tracking_code", unique = true, nullable = false, length = 50)
    private String trackingCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_status_id")
    private OrderStatus orderStatus;

    @Column(name = "payment_status", length = 50)
    @Builder.Default
    private String paymentStatus = "PENDING";

    @Column(name = "bold_transaction_id", unique = true, length = 100)
    private String boldTransactionId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("order")
    private List<OrderItem> items;
}

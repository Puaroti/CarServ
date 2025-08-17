package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders_clients")
public class OrderClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private CarClientEntity car;

    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerServiceEntity worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private RegUserInfoEntity client;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(name = "problem_description", columnDefinition = "TEXT")
    private String problemDescription;

    @Column(length = 50)
    private String status;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

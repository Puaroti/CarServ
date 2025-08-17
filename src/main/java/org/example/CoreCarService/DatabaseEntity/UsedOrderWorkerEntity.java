package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.example.CoreCarService.DatabaseEntity.id.UsedOrderWorkerId;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "used_order_workers")
public class UsedOrderWorkerEntity {
    @EmbeddedId
    private UsedOrderWorkerId id;

    @ManyToOne(optional = false)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private OrderClientEntity order;

    @ManyToOne(optional = false)
    @MapsId("serviceId")
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceWorkEntity service;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}

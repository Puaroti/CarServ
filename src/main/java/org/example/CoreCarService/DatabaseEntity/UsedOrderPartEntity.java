package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.example.CoreCarService.DatabaseEntity.id.UsedOrderPartId;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "used_order_parts")
public class UsedOrderPartEntity {
    @EmbeddedId
    private UsedOrderPartId id;

    @ManyToOne(optional = false)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private OrderClientEntity order;

    @ManyToOne(optional = false)
    @MapsId("partId")
    @JoinColumn(name = "part_id", nullable = false)
    private PartEntity part;

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
}

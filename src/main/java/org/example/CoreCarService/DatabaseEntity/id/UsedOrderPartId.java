package org.example.CoreCarService.DatabaseEntity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UsedOrderPartId implements Serializable {
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "part_id")
    private Long partId;
}

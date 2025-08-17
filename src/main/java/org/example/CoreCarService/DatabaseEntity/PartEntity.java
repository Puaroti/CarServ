package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parts")
public class PartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parts_name", length = 255)
    private String partsName;

    @Column(name = "quantity_patrs", nullable = false)
    private Integer quantityPatrs; // keeping original column name typo

    @Column(name = "price_for_one", precision = 10, scale = 2)
    private BigDecimal priceForOne;

    @Column(name = "price_for_everything", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceForEverything;
}

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
@Table(name = "service_work")
public class ServiceWorkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerServiceEntity worker;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "type_of_service", nullable = false, length = 100)
    private String typeOfService;

    @Column(name = "service_focus", nullable = false, length = 100)
    private String serviceFocus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}

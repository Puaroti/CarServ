package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cars_clients")
public class CarClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private RegUserInfoEntity user;

    @Column(nullable = false, length = 50)
    private String make;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "car_year", nullable = false)
    private Integer carYear;

    @Column(nullable = false, length = 17, unique = true)
    private String vin;

    @Column(nullable = false)
    private Integer mileage;
}

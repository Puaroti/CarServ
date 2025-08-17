package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workers_service")
public class WorkerServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private RegUserInfoEntity user;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 100)
    private String speciality;
}

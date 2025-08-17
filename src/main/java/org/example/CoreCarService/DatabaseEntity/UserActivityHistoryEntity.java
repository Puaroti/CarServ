package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_activity_history")
public class UserActivityHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References reg_user_info.login (unique)
    @ManyToOne
    @JoinColumn(name = "user_login", referencedColumnName = "login", nullable = false)
    private RegUserInfoEntity user;

    @Column(name = "activity_at", nullable = false, length = 100)
    private String activityAt;
}

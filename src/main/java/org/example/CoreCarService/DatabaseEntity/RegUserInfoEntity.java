package org.example.CoreCarService.DatabaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "reg_user_info")
public class RegUserInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false, length = 50)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 255)
    private String phoneNumber;

    @Column(name = "user_type", nullable = false, length = 50)
    private String userType;

    @Column(name = "registration_user_status", nullable = false, length = 50)
    private String registrationUserStatus;

    @Column(name = "method_communicate", length = 50)
    private String methodCommunicate;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(length = 50)
    private String role;
}

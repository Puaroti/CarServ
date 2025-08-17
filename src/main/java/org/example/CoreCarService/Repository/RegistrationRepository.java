package org.example.CoreCarService.Repository;

import org.example.CoreCarService.DatabaseEntity.RegUserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<RegUserInfoEntity, Long> {
    Optional<RegUserInfoEntity> findByLogin(String login);
    Boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

}

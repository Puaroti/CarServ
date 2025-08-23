package org.example.CoreCarService.Repository;

import org.example.CoreCarService.DatabaseEntity.CarClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CarClientRepository extends JpaRepository<CarClientEntity, Long> {
    boolean existsByVin(String vin);
    List<CarClientEntity> findAllByUser_Id(Long userId);
    Optional<CarClientEntity> findByVin(String vin);
}

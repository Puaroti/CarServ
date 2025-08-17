package org.example.CoreCarService.Repository;


import jakarta.transaction.Transactional;
import org.example.CoreCarService.DatabaseEntity.UserActivityHistoryEntity;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<UserActivityHistoryEntity, Long> {

        List<UserActivityHistoryEntity> findAllByUser_Login (@Param("login") String login);

        Page<UserActivityHistoryEntity> findAllByUser_Login (@Param("login") String login, Pageable pageable);

        boolean existsByUser_Login (@Param("login") String login);


 }

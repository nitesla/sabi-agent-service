package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Supervisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * This interface is responsible for Supervisor crud operations
 */

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Long>, JpaSpecificationExecutor<Supervisor> {

    Page<Supervisor> findAll(Pageable pageable);

    Supervisor findByUserId(Long userId);

    List<Supervisor> findByIsActive(Boolean isActive);

    @Query("SELECT s FROM Supervisor s " +
            "INNER JOIN User supervisorUser ON s.userId = supervisorUser.id " +
            "WHERE (((:supervisorUserName IS NULL) OR (:supervisorUserName IS NOT NULL AND ( CONCAT(supervisorUser.firstName,' ',supervisorUser.lastName)  LIKE %:supervisorUserName% )))" +
            "AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND s.isActive =:isActive))" +
            "AND ((((:lowerDateTime IS NULL) AND (:upperDateTime IS  NULL))) OR (((:lowerDateTime IS NOT NULL) AND (:upperDateTime IS NOT NULL)) AND (s.createdDate >= :lowerDateTime AND s.createdDate < :upperDateTime)))" +
            ") ")
    Page<Supervisor> searchSupervisors(String supervisorUserName, Boolean isActive, LocalDateTime lowerDateTime, LocalDateTime upperDateTime, Pageable pageable);
}

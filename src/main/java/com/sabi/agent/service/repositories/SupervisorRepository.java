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

    Supervisor findByUserIdAndAgentId(Long userId, Long agentId);

    List<Supervisor> findByIsActive(Boolean isActive);

    @Query("SELECT s FROM Supervisor s " +
            "INNER JOIN Agent at ON s.agentId = at.id " +
            "INNER JOIN User supervisorUser ON s.userId = supervisorUser.id " +
            "INNER JOIN User agentUser ON agentUser.id = at.userId " +
            "WHERE (((:supervisorName IS NULL) OR (:supervisorName IS NOT NULL AND (supervisorUser.firstName LIKE %:supervisorName% OR supervisorUser.lastName LIKE %:supervisorName%)))" +
            "AND ((:agentName IS NULL) OR (:agentName IS NOT NULL AND (agentUser.firstName LIKE %:agentName% OR agentUser.lastName LIKE %:agentName%)))" +
            "AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND s.isActive =:isActive))" +
            "AND ((((:lowerDateTime IS NULL) AND (:upperDateTime IS  NULL))) OR (((:lowerDateTime IS NOT NULL) AND (:upperDateTime IS NOT NULL)) AND (s.createdDate >= :lowerDateTime AND s.createdDate < :upperDateTime)))" +
            ")")
    Page<Supervisor> searchSupervisors(String supervisorName, String agentName, Boolean isActive, LocalDateTime lowerDateTime, LocalDateTime upperDateTime, Pageable pageable);
}

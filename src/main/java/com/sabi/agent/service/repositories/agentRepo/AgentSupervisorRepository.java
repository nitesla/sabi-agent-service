package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.Supervisor;
import com.sabi.agent.core.models.agentModel.AgentSupervisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * This interface is responsible for Agent Supervisor crud operations
 */
@Repository
public interface AgentSupervisorRepository extends JpaRepository<AgentSupervisor, Long>, JpaSpecificationExecutor<AgentSupervisor> {
    List<AgentSupervisor> findByIsActive(Boolean isActive);

    AgentSupervisor findAgentSupervisorBySupervisorIdAndAgentId(Long agentId, Long supervisorId);


    @Query("SELECT asr FROM AgentSupervisor asr " +
            "INNER JOIN Agent at ON asr.agentId = at.id " +
            "INNER JOIN Supervisor s ON asr.supervisorId = s.id " +
            "INNER JOIN User supervisorUser ON s.userId = supervisorUser.id " +
            "INNER JOIN User agentUser ON agentUser.id = at.userId " +
            "WHERE (((:supervisorName IS NULL) OR (:supervisorName IS NOT NULL AND (CONCAT(supervisorUser.firstName, ' ', supervisorUser.lastName)  LIKE %:supervisorName%)))" +
            "AND ((:agentName IS NULL) OR (:agentName IS NOT NULL AND (CONCAT(agentUser.firstName, agentUser.lastName)  LIKE %:agentName%)))" +
            "AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND s.isActive =:isActive))" +
            "AND ((:agentId IS NULL) OR (:agentId IS NOT NULL AND asr.agentId =:agentId))" +
            "AND ((:supervisorId IS NULL) OR (:supervisorId IS NOT NULL AND asr.supervisorId =:supervisorId))" +
            "AND ((((:lowerDateTime IS NULL) AND (:upperDateTime IS  NULL))) OR (((:lowerDateTime IS NOT NULL) AND (:upperDateTime IS NOT NULL)) AND (s.createdDate >= :lowerDateTime AND s.createdDate < :upperDateTime)))" +
            ")")
    Page<AgentSupervisor> searchAgentSupervisors(String supervisorName, String agentName,
                                                 Long agentId, Boolean isActive, Long supervisorId, LocalDateTime lowerDateTime, LocalDateTime upperDateTime, Pageable pageable);

    AgentSupervisor findByAgentId(Long agentId);
}

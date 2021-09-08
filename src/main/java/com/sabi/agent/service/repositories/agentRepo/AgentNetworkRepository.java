package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentNetwork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * This interface is responsible for Agent Network crud operations
 */
public interface AgentNetworkRepository extends JpaRepository<AgentNetwork, Long> {
    AgentNetwork findByAgentId(Long agentId);

    @Query("SELECT a FROM AgentNetwork a WHERE ((:agentId IS NULL) OR (:agentId IS NOT NULL AND a.agentId = :agentId))" +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND a.isActive = :isActive))")
    Page<AgentNetwork> findAgentNetwork(Long agentId, Boolean isActive, Pageable pageable);
}

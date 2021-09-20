package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 *
 * This interface is responsible for Agent Network crud operations
 */
public interface AgentNetworkRepository extends JpaRepository<AgentNetwork, Long>, JpaSpecificationExecutor<AgentNetwork> {
    AgentNetwork findByAgentId(Long agentId);
    List<AgentNetwork> findByIsActive(Boolean isActive);
}

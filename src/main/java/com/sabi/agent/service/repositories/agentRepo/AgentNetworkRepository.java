package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentNetwork;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * This interface is responsible for Agent Network crud operations
 */
public interface AgentNetworkRepository extends JpaRepository<AgentNetwork, Long> {
}

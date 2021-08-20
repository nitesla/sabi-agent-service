package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.Agent;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 *
 * This interface is responsible for Agent crud operations
 */

public interface AgentRepository extends JpaRepository<Agent, Long> {
}

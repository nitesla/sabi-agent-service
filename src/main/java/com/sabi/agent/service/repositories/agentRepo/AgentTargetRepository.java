package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentTarget;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * This interface is responsible for Agent Target crud operations
 */
public interface AgentTargetRepository extends JpaRepository<AgentTarget, Long> {
}

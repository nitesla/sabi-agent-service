package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 *
 * This interface is responsible for Agent Category Target crud operations
 */

public interface AgentCategoryTargetRepository extends JpaRepository<AgentCategoryTarget, Long> {
}

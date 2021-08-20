package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * This interface is responsible for Agent Category Task crud operations
 */
public interface AgentCategoryTaskRepository extends JpaRepository<AgentCategoryTask, Long> {
}

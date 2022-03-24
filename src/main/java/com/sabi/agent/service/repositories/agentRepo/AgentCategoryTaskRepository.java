package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Agent Category Task crud operations
 */

@Repository
public interface AgentCategoryTaskRepository extends JpaRepository<AgentCategoryTask, Long>, JpaSpecificationExecutor<AgentCategoryTask> {
    AgentCategoryTask findByName (String name);

    AgentCategoryTask findByAgentCategoryIdAndTaskId (Long  agentCategoryId, Long TaskId);

    List<AgentCategoryTask> findByAgentCategoryId(Long agentCategoryId);

    List<AgentCategoryTask> findByIsActive(Boolean isActive);
}

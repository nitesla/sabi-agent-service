package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategory;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 *
 * This interface is responsible for Agent Category crud operations
 */

public interface AgentCategoryRepository extends JpaRepository<AgentCategory, Long> {
}

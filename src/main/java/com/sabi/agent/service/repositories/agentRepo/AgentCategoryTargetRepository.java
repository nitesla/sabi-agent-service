package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 *
 * This interface is responsible for Agent Category Target crud operations
 */

@Repository
public interface AgentCategoryTargetRepository extends JpaRepository<AgentCategoryTarget, Long>, JpaSpecificationExecutor<AgentCategoryTarget> {

    AgentCategoryTarget findByName (String name);

    AgentCategoryTarget findByAgentCategoryIdAndTargetTypeId (Long  agentCategoryId, Long targetTypeId);

    List<AgentCategoryTarget> findByIsActive(Boolean isActive);

}

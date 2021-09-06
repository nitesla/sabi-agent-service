package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 *
 * This interface is responsible for Agent Category Target crud operations
 */

public interface AgentCategoryTargetRepository extends JpaRepository<AgentCategoryTarget, Long> {

    AgentCategoryTarget findByName (String name);

    @Query("SELECT a FROM AgentCategoryTarget a WHERE ((:name IS NULL) OR (:name IS NOT NULL AND a.name = :name))")
    Page<AgentCategoryTarget> findAgentCategoryTargets(@Param("name")String name, Pageable pageable);


}

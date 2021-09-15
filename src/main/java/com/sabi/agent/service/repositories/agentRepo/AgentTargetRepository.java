package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 *
 * This interface is responsible for Agent Target crud operations
 */
public interface AgentTargetRepository extends JpaRepository<AgentTarget, Long> {

    List<AgentTarget> findByIsActive(Boolean isActive);

    @Query("SELECT i FROM AgentTarget i WHERE ((:name IS NULL) OR (:name IS NOT NULL AND i.name = :name))" +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND i.isActive = :isActive))")
    Page<AgentTarget> findAgentTarget(String name, Boolean isActive, Pageable pageable);
}

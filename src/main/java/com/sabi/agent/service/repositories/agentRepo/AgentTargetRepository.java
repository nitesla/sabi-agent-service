package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.Market;
import com.sabi.agent.core.models.agentModel.AgentTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 *
 * This interface is responsible for Agent Target crud operations
 */
public interface AgentTargetRepository extends JpaRepository<AgentTarget, Long>, JpaSpecificationExecutor<AgentTarget> {
    List<Market> findByIsActive(Boolean isActive);
    Optional<AgentTarget> findByName(String name);
}

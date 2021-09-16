package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 *
 * This interface is responsible for Agent Location crud operations
 */
public interface AgentLocationRepository extends JpaRepository<AgentLocation, Long>, JpaSpecificationExecutor<AgentLocation> {
    List<AgentLocation> findByIsActive(Boolean isActive);
    Boolean existsByAgentId(Long agentId);
}

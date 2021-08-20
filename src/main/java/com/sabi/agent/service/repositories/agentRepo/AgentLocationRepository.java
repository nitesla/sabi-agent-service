package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentLocation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * This interface is responsible for Agent Location crud operations
 */
public interface AgentLocationRepository extends JpaRepository<AgentLocation, Long> {
}

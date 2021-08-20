package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * This interface is responsible for Agent Supervisor crud operations
 */
public interface AgentSupervisorRepository extends JpaRepository<AgentSupervisor, Long> {
}

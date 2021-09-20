package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Agent Supervisor crud operations
 */
@Repository
public interface AgentSupervisorRepository extends JpaRepository<AgentSupervisor, Long>, JpaSpecificationExecutor<AgentSupervisor> {
    List<AgentSupervisor> findByIsActive(Boolean isActive);
}

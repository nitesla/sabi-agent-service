package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.dto.agentDto.requestDto.AgentSupervisorDto;
import com.sabi.agent.core.models.agentModel.AgentSupervisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Agent Supervisor crud operations
 */
@Repository
public interface AgentSupervisorRepository extends JpaRepository<AgentSupervisor, Long> {
    AgentSupervisor findByAgentSupervisorDto(AgentSupervisorDto agentSupervisorDto);

}

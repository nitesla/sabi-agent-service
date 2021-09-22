package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 *
 * This interface is responsible for Agent Bank crud operations
 */

@Repository
public interface AgentBankRepository extends JpaRepository<AgentBank, Long>, JpaSpecificationExecutor<AgentBank> {

    AgentBank findByAccountNumber (String accountnumber);

    AgentBank findByAgentIdAndBankId (Long  agentId, Long bankId);

    List<AgentBank> findByIsActive(Boolean isActive);

}

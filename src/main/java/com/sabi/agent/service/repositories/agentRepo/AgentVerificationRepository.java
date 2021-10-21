package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 *
 * This interface is responsible for Agent Verification crud operations
 */
public interface AgentVerificationRepository extends JpaRepository<AgentVerification, Long> {

    AgentVerification findByAgentId(Long agentId);
    AgentVerification findByAgentIdAndComponent(Long agentId ,String component);


    @Query("SELECT a FROM AgentVerification a WHERE  ((:name IS NULL) OR (:name IS NOT NULL AND a.name = :name))"+
            " AND ((:agentId IS NULL) OR (:agentId IS NOT NULL AND a.agentId = :agentId))")
    Page<AgentVerification> agentsDetailsForVerification(@Param("name")String name,
                                                         @Param("agentId")Long agentId,
                                                         Pageable pageable);
}

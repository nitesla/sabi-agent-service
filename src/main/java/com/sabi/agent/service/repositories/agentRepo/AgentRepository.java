package com.sabi.agent.service.repositories.agentRepo;



import com.sabi.agent.core.models.agentModel.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Agent crud operations
 */

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Agent findByUserId (Long userId);

    Agent findByRegistrationToken (String registrationToken);

}

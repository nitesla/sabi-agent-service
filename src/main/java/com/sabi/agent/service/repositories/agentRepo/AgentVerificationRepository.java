package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentVerification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * This interface is responsible for Agent Verification crud operations
 */
public interface AgentVerificationRepository extends JpaRepository<AgentVerification, Long> {
}

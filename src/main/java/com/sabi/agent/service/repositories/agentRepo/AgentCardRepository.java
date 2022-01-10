package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentCardRepository extends JpaRepository<AgentCard, Long> {
    List<AgentCard> findByAgentId(Long agentId);
    AgentCard findByIdAndAgentId(Long cardId, Long agentId);
}

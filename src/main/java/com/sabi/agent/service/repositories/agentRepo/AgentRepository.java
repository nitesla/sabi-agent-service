package com.sabi.agent.service.repositories.agentRepo;


import com.sabi.agent.core.models.agentModel.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Agent crud operations
 */

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Agent findByUserId (Long userId);

    Agent findByRegistrationToken (String registrationToken);


    @Query("SELECT t FROM Agent t WHERE ((:userId IS NULL) OR (:userId IS NOT NULL AND t.userId = :userId)) " +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND t.isActive = :isActive))")
    Page<Agent> findAgents(@Param("userId")Long userId,
                           @Param("isActive")Boolean isActive,Pageable pageable);

}

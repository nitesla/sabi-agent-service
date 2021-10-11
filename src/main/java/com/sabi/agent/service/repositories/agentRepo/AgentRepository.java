package com.sabi.agent.service.repositories.agentRepo;


import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.framework.models.User;
import org.aspectj.weaver.ast.And;
import org.hibernate.sql.Select;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Agent crud operations
 */

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Agent findByUserId (Long userId);


    Agent findByRegistrationToken (String registrationToken);

    List<Agent> findByIsActive(Boolean isActive);


    @Query("SELECT t FROM Agent t  WHERE  ((:userId IS NULL) OR (:userId IS NOT NULL AND t.userId = :userId)) " +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND t.isActive = :isActive))"+
    " AND ((:referrer IS NULL) OR (:referrer IS NOT NULL AND t.referrer = :referrer))"
)
    Page<Agent> findAgents(@Param("userId")Long userId,
                           @Param("isActive")Boolean isActive,
                           @Param("referrer")String referrer,
                           Pageable pageable);

}

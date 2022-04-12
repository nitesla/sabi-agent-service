package com.sabi.agent.service.repositories.agentRepo;


import com.sabi.agent.core.models.agentModel.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
            " AND ((:referralCode IS NULL) OR (:referralCode IS NOT NULL AND t.referralCode = :referralCode))"+
    " AND ((:referrer IS NULL) OR (:referrer IS NOT NULL AND t.referrer = :referrer))")
    Page<Agent> findAgents(@Param("userId")Long userId,
                           @Param("isActive")Boolean isActive,
                           @Param("referralCode") String referralCode,
                           @Param("referrer")String referrer,
                           Pageable pageable);

    @Query(value = "SELECT AgentCategory.name as agentCategory, User.firstName as agentFirstName, User.lastName as agentLastName, Agent.* FROM AgentCategory, User, Agent WHERE " +
            "Agent.agentCategoryId=AgentCategory.id AND Agent.userId = User.id " +
            "AND ((:verificationStatus IS NULL) OR (:verificationStatus IS NOT NULL AND Agent.verificationStatus = :verificationStatus)) " +
            "AND ((:status IS NULL) OR (:status IS NOT NULL AND Agent.status = :status)) " +
            "AND ((:agentName IS NULL) OR (CONCAT(User.firstName, \" \" ,User.lastName) LIKE %:agentName%) " +
            "OR (CONCAT(User.lastName, \" \" ,User.firstName) LIKE %:agentName%)) " +
            "AND ((:agentCategory IS NULL) OR (:agentCategory IS NOT NULL AND AgentCategory.name LIKE %:agentCategory%)) " +
            "AND ((:startDate IS NULL) AND (:endDate IS NULL) OR (Agent.createdDate BETWEEN :startDate AND :endDate))", nativeQuery = true)
    Page<Map> filterAgent(@Param("agentName") String agentName,
                          @Param("agentCategory") String agentCategory,
                          @Param("verificationStatus") String verificationStatus,
                          @Param("status") Integer status,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate,
                          Pageable pageable);

}

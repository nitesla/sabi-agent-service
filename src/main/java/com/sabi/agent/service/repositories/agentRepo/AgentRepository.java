package com.sabi.agent.service.repositories.agentRepo;


import com.sabi.agent.core.models.agentModel.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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



    @Query("SELECT t FROM Agent t  WHERE ((:userId IS NULL) OR (:userId IS NOT NULL AND t.userId = :userId)) " +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND t.isActive = :isActive))"+
    " AND ((:referrer IS NULL) OR (:referrer IS NOT NULL AND t.referrer = :referrer))")

//    @Query(value ="SELECT \n" +
//            "a.id,a.createdBy,a.createdDate,a.isActive,a.updatedBy,a.updatedDate,a.accountNonLocked,a.address,a.agentCategoryId,\n" +
//            "a.agentType,a.balance,a.bankId,a.bvn,a.cardToken,a.comment,a.countryId,a.creditLevelId,\n" +
//            "a.creditLimit,a.hasCustomizedTarget,a.idCard,a.idTypeId,a.isEmailVerified,\n" +
//            "a.payBackDuration,a.picture,a.referralCode,a.referrer,a.registrationToken,a.registrationTokenExpiration,a.scope,\n" +
//            "a.stateId,a.status,a.supervisorId,a.userId,\n" +
//            "u.firstName AS firstName,\n" +
//            "u.lastName AS lastName,\n" +
//            "a.verificationDate,a.verificationStatus,a.walletId\n" +
//            " FROM agent a\n" +
//            " INNER JOIN user u  ON  a.userId = u.id", nativeQuery=true)
    Page<Agent> findAgents(Long userId,
                           Boolean isActive,
                           String referrer,
//                           String firstName,
//                           String lastName,
                           Pageable pageable);

}

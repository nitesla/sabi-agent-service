package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.RegisteredMerchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface MerchantRepository extends JpaRepository<RegisteredMerchant, Long>, JpaSpecificationExecutor<RegisteredMerchant> {

    @Query(value = "SELECT  * from RegisteredMerchant where (CONCAT(firstName, \" \" ,lastName)  " +
            "LIKE %:searchTerm% OR CONCAT(lastName, \" \" ,firstName) LIKE %:searchTerm%) AND agentId = :agentId", nativeQuery = true)
    Page<RegisteredMerchant> searchMerchants(@Param("searchTerm") String searchTerm, @Param("agentId") Long agentId, Pageable pageable);

//    @Query(value = "SELECT  * from RegisteredMerchant where (CONCAT(firstName, \" \" ,lastName)  " +
//            "LIKE %:searchTerm% OR CONCAT(lastName, \" \" ,firstName) LIKE %:searchTerm%) " +
//            "AND ((:agentId IS NULL) OR (:agentId IS NOT NULL AND agentId = :agentId)) " +
//            "AND ((:startDate IS NULL AND :endDate IS NULL) OR (:startDate IS NOT NULL AND :endDate IS NOT NULL " +
//            "AND createdDate BETWEEN :startDate AND :endDate))", nativeQuery = true)
//    Page<RegisteredMerchant> searchMerchants(@Param("searchTerm") String searchTerm,
//                                             @Param("agentId") Long agentId,
//                                             @Param("startDate") Date startDate,
//                                             @Param("endDate") Date endDate,
//                                             Pageable pageable);
}

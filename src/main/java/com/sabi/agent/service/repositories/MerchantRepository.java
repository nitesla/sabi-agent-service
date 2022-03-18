package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.RegisteredMerchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MerchantRepository extends JpaRepository<RegisteredMerchant, Long>, JpaSpecificationExecutor<RegisteredMerchant> {

    @Query(value = "SELECT  * from RegisteredMerchant where ((CONCAT(firstName, \" \" ,lastName)  " +
            "LIKE %:searchTerm% OR CONCAT(lastName, \" \" ,firstName) LIKE %:searchTerm%) " +
            "OR phoneNumber LIKE %:phoneNumber% )" +
            "AND agentId = :agentId", nativeQuery = true)
    Page<RegisteredMerchant> searchMerchants(@Param("searchTerm") String searchTerm, @Param("agentId") Long agentId, @Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query(value = "SELECT  * from RegisteredMerchant where (CONCAT(firstName, \" \" ,lastName)  " +
            "LIKE %:searchTerm% OR CONCAT(lastName, \" \" ,firstName) LIKE %:searchTerm%) AND ((:agentId IS NULL) OR (:agentId IS NOT NULL AND agentId = :agentId))" +
            "AND (((:fromDate IS NULL ) OR (:fromDate IS NOT NULL AND createdDate >= :fromDate)) AND ((:toDate IS NULL) OR (:toDate IS NOT NULL AND createdDate <= :toDate) )) ", nativeQuery = true)
    Page<RegisteredMerchant> searchMerchants(@Param("searchTerm") String searchTerm, @Param("agentId") Long agentId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);


    @Query(value = "SELECT  * from RegisteredMerchant where (CONCAT(firstName, \" \" ,lastName)  " +
            "LIKE %:searchTerm% OR CONCAT(lastName, \" \" ,firstName) LIKE %:searchTerm%)"+
            "OR phoneNumber LIKE %:phoneNumber% ", nativeQuery = true)
    Page<RegisteredMerchant> searchMerchantsWithoutAgentId(@Param("searchTerm") String searchTerm, @Param("phoneNumber") String phoneNumber, Pageable pageable);
}

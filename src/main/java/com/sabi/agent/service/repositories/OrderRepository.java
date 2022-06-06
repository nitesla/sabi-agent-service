package com.sabi.agent.service.repositories;

import com.sabi.agent.core.dto.responseDto.OrderSearchResponse;
import com.sabi.agent.core.models.AgentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<AgentOrder, Long> {

    Page<AgentOrder> findByMerchantId(Long merchantId, Pageable pageable);

    //RegisteredMerchant
    //Search for a Customer should be across board, Searc by "Customer Order No", "Customer Name", "Phone No"
    @Query("SELECT t FROM AgentOrder t  WHERE ((:orderId IS NULL) OR (:orderId IS NOT NULL AND t.orderId = :orderId) OR (:orderId IS NOT NULL AND t.orderNumber = :orderId)) " +
            " AND ((:status IS NULL) OR (:status IS NOT NULL AND t.status = :status))" +
            " AND ((:createdDate IS NULL) OR (:createdDate IS NOT NULL AND t.createdDate = :createdDate))" +
            " AND ((:userName IS NULL) OR (:userName IS NOT NULL AND t.userName = :userName))" +
            " AND ((:agentId IS NULL) OR (:agentId IS NOT NULL AND t.agentId = :agentId))")
    Page<AgentOrder> findOrders(@Param("orderId") Long orderId,
                                @Param("status") Boolean status,
                                @Param("createdDate") Date createdDate,
                                @Param("agentId") Long agentId,
                                @Param("userName") String userName,
                                Pageable pageable);

//    @Query(value = "SELECT RegisteredMerchant.firstName, RegisteredMerchant.lastName, RegisteredMerchant.phoneNumber, " +
//            "AgentOrder.* from RegisteredMerchant, AgentOrder  WHERE (firstName LIKE %:searchTerm% OR lastName LIKE %:searchTerm% " +
//            "or phoneNumber like %:searchTerm% OR AgentOrder.orderId LIKE %:searchTerm%) " +
//            "AND AgentOrder.createdDate BETWEEN :startDate AND :endDate", nativeQuery = true)

//    @Query(value = "SELECT CONCAT(firstName, \", \" ,lastName) from RegisteredMerchant where firstName LIKE %:searchTerm% OR lastName LIKE %:searchTerm% " +
//            "UNION " +
//            "SELECT phoneNumber FROM RegisteredMerchant WHERE phoneNumber LIKE %:searchTerm% " +
//            "UNION " +
//            "SELECT orderId FROM AgentOrder WHERE orderId LIKE %:searchTerm%", nativeQuery = true)

    @Query(value = "SELECT RegisteredMerchant.firstName, RegisteredMerchant.lastName, RegisteredMerchant.phoneNumber," +
            " AgentOrder.* FROM RegisteredMerchant, AgentOrder WHERE ((:agentId IS NULL) OR (:agentId IS NOT NULL AND AgentOrder.agentId = :agentId)) " +
            " AND (( CONCAT(RegisteredMerchant.firstName, \" \" ,RegisteredMerchant.lastName)  " +
            "LIKE %:searchTerm% OR (CONCAT (RegisteredMerchant.lastName, \" \" ,RegisteredMerchant.firstName) LIKE %:searchTerm%) " +
            "OR phoneNumber LIKE %:searchTerm% OR (AgentOrder.orderId LIKE %:searchTerm% ) OR (AgentOrder.orderNumber LIKE %:searchTerm% ))and AgentOrder.merchantId=RegisteredMerchant.id)"+
            "AND AgentOrder.createdDate BETWEEN :startDate AND :endDate", nativeQuery = true)
    Page<Map> singleSearch(@Param("searchTerm") String searchTerm,
                           @Param("agentId") Long agentId,
                           @Param("startDate") String startDate,
                           @Param("endDate") String endDate,
                           Pageable pageable);

    //    @Query(value = "SELECT RegisteredMerchant.firstName, RegisteredMerchant.lastName, RegisteredMerchant.phoneNumber," +
//            " AgentOrder.* FROM RegisteredMerchant,AgentOrder WHERE(firstName LIKE %:searchTerm% OR lastName LIKE %:searchTerm% " +
//            "OR phoneNumber LIKE %:searchTerm% OR AgentOrder.orderId LIKE  %:searchTerm%)", nativeQuery = true)
    @Query(value = "SELECT RegisteredMerchant.firstName, RegisteredMerchant.lastName, RegisteredMerchant.phoneNumber," +
            " AgentOrder.* FROM RegisteredMerchant, AgentOrder WHERE ((:agentId IS NULL) OR (:agentId IS NOT NULL AND AgentOrder.agentId = :agentId)) " +
            " AND (( CONCAT(RegisteredMerchant.firstName, \" \" ,RegisteredMerchant.lastName)  " +
            "LIKE %:searchTerm% OR (CONCAT (RegisteredMerchant.lastName, \" \" ,RegisteredMerchant.firstName) LIKE %:searchTerm%) " +
            "OR phoneNumber LIKE %:searchTerm% OR (AgentOrder.orderId LIKE %:searchTerm% ))and AgentOrder.merchantId=RegisteredMerchant.id)", nativeQuery = true)
    Page<Map> singleSearch(@Param("searchTerm") String searchTerm,
                           @Param("agentId") Long agentId,
                           Pageable pageable);

    AgentOrder findByOrderId(long orderId);

    List<AgentOrder> findByIsSentToThirdPartyAndOrderStatus(Boolean sent, String orderStatus);

    @Query(value = "SELECT RegisteredMerchant.firstName, RegisteredMerchant.lastName, AgentOrder.* " +
            "FROM RegisteredMerchant, AgentOrder WHERE " +
            "((:agentId IS NULL) OR (:agentId IS NOT NULL AND AgentOrder.agentId = :agentId)) " +
            "AND AgentOrder.merchantId=RegisteredMerchant.id " +
            "AND ((:status IS NULL) OR (:status IS NOT NULL AND AgentOrder.status = :status)) " +
            "AND ((:merchantName IS NULL) OR (CONCAT(RegisteredMerchant.firstName, \" \" ,RegisteredMerchant.lastName) LIKE %:merchantName%) " +
            "OR (CONCAT(RegisteredMerchant.lastName, \" \" ,RegisteredMerchant.firstName) LIKE %:merchantName%))" +
            "AND ((:agentName IS NULL) OR (:agentName IS NOT NULL AND AgentOrder.userName LIKE %:agentName%)) " +
            "AND ((:orderId IS NULL) OR (:orderId IS NOT NULL AND AgentOrder.orderId = :orderId)) " +
            "AND ((:startDate IS NULL) AND (:endDate IS NULL) OR (AgentOrder.createdDate BETWEEN :startDate AND :endDate))", nativeQuery = true)
    Page<Map> findForAdmin(@Param("status") Integer status,
                           @Param("agentId") Long agentId,
                           @Param("merchantName") String merchantName,
                           @Param("agentName") String agentName,
                           @Param("startDate") String startDate,
                           @Param("orderId") Long orderId,
                           @Param("endDate") String endDate,
                           Pageable pageable);


    Page<AgentOrder> findByAgentId(Long agentId, Pageable of);
}


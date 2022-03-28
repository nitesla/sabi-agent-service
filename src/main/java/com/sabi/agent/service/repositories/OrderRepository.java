package com.sabi.agent.service.repositories;

import com.sabi.agent.core.dto.responseDto.OrderSearchResponse;
import com.sabi.agent.core.models.AgentOrder;
import org.springframework.data.domain.Page;
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


    //RegisteredMerchant
    //Search for a Customer should be across board, Searc by "Customer Order No", "Customer Name", "Phone No"
    @Query("SELECT t FROM AgentOrder t  WHERE ((:orderId IS NULL) OR (:orderId IS NOT NULL AND t.orderId = :orderId)) " +
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
            "OR phoneNumber LIKE %:searchTerm% OR (AgentOrder.orderId LIKE %:searchTerm% ))and AgentOrder.merchantId=RegisteredMerchant.id)"+
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
    //Status, Date Range, name of merchant, agent name quantity, unit price and Total price.

//    @Query(value = "Select USER.firstName as agentFirstName, USER.lastName as agentLastName," +
//            "RegisteredMerchant.firstName as merchantFirstName, RegisteredMerchant.lastName as merchantLastName, AgentOrder.* FROM USER, AgentOrder WHERE " +
//            "CONCAT (USER.lastName, \" \" ,USER.firstName) LIKE %:agentName% " +
//            "OR CONCAT (USER.firstName, \" \" ,USER.lastName) LIKE %:agentName%) " +
//            "AND ((:status IS NULL) OR (:status IS NOT NULL AND AgentOrder.orderStatus=:status)) " +
//            "AND ((:quantity IS NULL) OR (:quantity IS NOT NULL AgentOrder.quantity=:quantity)) " +
//            "AND ((:amount IS NULL) OR (:amount IS NOT NULL AgentOrder.totalAmount=:amount)) " +
//            "AND (( CONCAT(RegisteredMerchant.firstName, \" \" ,RegisteredMerchant.lastName)  " +
//            " LIKE %:merchName% OR (CONCAT (RegisteredMerchant.lastName, \" \" ,RegisteredMerchant.firstName) LIKE %:merchName%) " +
//            "AND AgentOrder.merchantId=RegisteredMerchant.id AND AgentOrder.agentId=User.agentId")
//    Page<Map> adminSearch(@Param("agentName") String agentName,
//                          @Param("orderStatus") String orderStatus,
//                           @Param("quantity") Long quantity,
//                          @Param("totalAmount") String totalAmount,
//                          @Param("merchName") String merchName,
//                           Pageable pageable);

//    @Query(value = "SELECT u.firstName AS agentFirstName, u.lastName AS agentLastName, " +
//            "r.firstName AS merchantFirstName,r.lastName AS merchantLastName,a.*,Agent.id " +
//            "FROM USER u,AgentOrder a,RegisteredMerchant r, Agent WHERE a.merchantId=r.id " +
//            "AND ((:agentName IS NULL) OR (:agentName IS NOT NULL AND CONCAT(u.lastName,\" \",u.firstName) LIKE %:agentName% " +
//            "OR CONCAT(u.firstName,\" \",u.lastName) LIKE %:agentName%)) " +
//            "AND ((:orderStatus IS NULL) OR (:orderStatus IS NOT NULL AND a.orderStatus=:orderStatus) " +
////            "AND ((:quantity IS NULL) OR (:quantity IS NOT NULL AgentOrder.quantity=:quantity)) " +
//            "AND ((:totalAmount IS NULL) OR (:totalAmount IS NOT NULL AND a.totalAmount=:totalAmount )) " +
//            "AND ((:merchName IS NULL) OR (:merchName IS NOT NULL AND CONCAT(r.lastName,\" \",r.firstName) LIKE %:agentName% " +
//            "OR CONCAT(r.firstName,\" \",r.lastName) LIKE %:merchName%)) " +
//            "AND r.agentId=(SELECT id FROM Agent WHERE u.id=id) ", nativeQuery = true)

//    @Query(value = "SELECT u.firstName AS agentFirstName,u.lastName AS agentLastName,r.firstName AS merchantFirstName, " +
//            "r.lastName AS merchantLastName,a.*,Agent.id as aId FROM User u,AgentOrder a,RegisteredMerchant r,Agent " +
//            "WHERE a.merchantId=r.id AND((:agentName IS NULL) OR (:agentName IS NOT NULL AND (CONCAT(u.lastName,\" \",u.firstName) " +
//            "LIKE %:agentName% OR CONCAT(u.firstName,\" \",u.lastName)LIKE %:agentName%))) " +
//            "AND ((:orderStatus IS NULL) OR (:orderStatus IS NOT NULL AND " +
//            "a.orderStatus = :orderStatus)) AND ((:totalAmount IS NULL) OR (:totalAmount IS NOT NULL AND a.totalAmount=:totalAmount)) AND " +
//            "((:merchName IS NULL) OR (:merchName IS NOT NULL AND (CONCAT(r.lastName,\" \",r.firstName)LIKE %:merchName% OR " +
//            "CONCAT(r.firstName,\" \",r.lastName)LIKE %:merchName%)))AND r.agentId=(SELECT id FROM Agent WHERE u.id=id)", nativeQuery = true)
//    @Query(value = "SELECT u.firstName AS agentFirstName,u.lastName AS agentLastName,r.firstName AS merchantFirstName,r.lastName AS merchantLastName,a.*,Agent.id as aId FROM USER u,AgentOrder a,RegisteredMerchant r,Agent WHERE a.merchantId=r.id AND((NULL IS NULL)OR(NULL IS NOT NULL AND (CONCAT(u.lastName,\" \",u.firstName)LIKE NULL OR CONCAT(u.firstName,\" \",u.lastName)LIKE NULL)))AND((NULL IS NULL)OR(NULL IS NOT NULL AND a.orderStatus=NULL))AND((NULL IS NULL)OR(NULL IS NOT NULL AND a.totalAmount=NULL))AND((NULL IS NULL)OR(NULL IS NOT NULL AND (CONCAT(r.lastName,\" \",r.firstName)LIKE NULL OR CONCAT(r.firstName,\" \",r.lastName)LIKE NULL)))AND r.agentId=(SELECT id FROM Agent WHERE u.id=id)", nativeQuery = true)
//    List<Map> adminSearch(@Param("agentName") String agentName,
//                          @Param("orderStatus") String orderStatus,
////                          @Param("quantity") Long quantity,
//                          @Param("totalAmount") String totalAmount,
//                          @Param("merchName") String merchName
//                          );
}


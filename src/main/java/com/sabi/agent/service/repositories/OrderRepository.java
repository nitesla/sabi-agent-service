package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.AgentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface OrderRepository extends JpaRepository<AgentOrder, Long> {





    @Query("SELECT t FROM AgentOrder t  WHERE ((:orderId IS NULL) OR (:orderId IS NOT NULL AND t.orderId = :orderId)) " +
            " AND ((:status IS NULL) OR (:status IS NOT NULL AND t.status = :status))"+
            " AND ((:createdDate IS NULL) OR (:createdDate IS NOT NULL AND t.createdDate = :createdDate))"+
            " AND ((:userName IS NULL) OR (:userName IS NOT NULL AND t.userName = :userName))"+
            " AND ((:agentId IS NULL) OR (:agentId IS NOT NULL AND t.agentId = :agentId))")
    Page<AgentOrder> findOrders(@Param("orderId")Long orderId,
                                @Param("status")Boolean status,
                                @Param("createdDate")Date createdDate,
                                @Param("agentId")Long agentId,
                           @Param("userName") String userName,
                           Pageable pageable);
}

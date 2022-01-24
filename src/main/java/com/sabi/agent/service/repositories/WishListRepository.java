package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    WishList findWishListById(Long id);
    WishList findWishListByProductNameAndAgentId(String productName, String agentId);

    @Query("SELECT t FROM WishList t WHERE ((:agentId IS NULL) OR (:agentId IS NOT NULL AND t.agentId = :agentId)) " +
            "AND ((:productId IS NULL) OR (:productId IS NOT NULL AND t.productId = :productId))" +
            " AND ((:productName IS NULL) OR (:productName IS NOT NULL AND t.productName LIKE %:productName%))" +
            "AND ((:picture IS NULL) OR (:picture IS NOT NULL AND t.picture LIKE %:picture%))")
    Page<WishList> findWishList(@Param("agentId")String agentId,
                            @Param("productId")String productId,
                                @Param("productName")String productName,
                            @Param("picture")String picture, Pageable pageable);
}

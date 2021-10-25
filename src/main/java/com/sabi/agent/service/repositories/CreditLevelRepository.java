package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.CreditLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * This interface is responsible for Credit level crud operations
 */

@Repository
public interface CreditLevelRepository extends JpaRepository<CreditLevel, Long> {


    List<CreditLevel> findByIsActive(Boolean isActive);

    CreditLevel findCreditLevelByLimits(BigDecimal creditLimit);

    @Query("SELECT c FROM CreditLevel c WHERE ((:limits IS NULL) OR (:limits IS NOT NULL AND c.limits = :limits))" +
//        " AND ((:repaymentPeriod IS NULL) OR (:repaymentPeriod IS NOT NULL AND c.repaymentPeriod = :repaymentPeriod))" +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND c.isActive = :isActive))")
    Page<CreditLevel> findCreditLevel(@Param("limits") BigDecimal limit,
//                                      @Param("repaymentPeriod") int repaymentPeriod,
                                      @Param("isActive")Boolean isActive,
                                      Pageable pageable);
}

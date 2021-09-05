package com.sabi.agent.service.repositories;





import com.sabi.agent.core.models.CreditLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Credit level crud operations
 */

@Repository
public interface CreditLevelRepository extends JpaRepository<CreditLevel, Long> {
@Query("SELECT c FROM CreditLevel c WHERE ((:limit IS NULL) OR (:limit IS NOT NULL AND c.limit = :limit))" +
        " AND ((:repaymentPeriod IS NULL) OR (:repaymentPeriod IS NOT NULL AND c.repaymentPeriod = :repaymentPeriod))")
    Page<CreditLevel> findcreditLevel(@Param("limit") Long limit,
                                      @Param("repaymentPeriod") Long repaymentPeriod,
                                      PageRequest pageRequest);
}

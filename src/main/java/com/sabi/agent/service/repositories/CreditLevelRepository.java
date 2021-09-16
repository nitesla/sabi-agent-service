package com.sabi.agent.service.repositories;





import com.sabi.agent.core.models.CreditLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Credit level crud operations
 */

@Repository
public interface CreditLevelRepository extends JpaRepository<CreditLevel, Long> , JpaSpecificationExecutor<CreditLevel> {
    List<CreditLevel> findByIsActive(Boolean isActive);
}

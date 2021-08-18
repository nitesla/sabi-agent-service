package com.sabi.agent.service.repositories;





import com.sabi.agent.core.models.CreditLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Credit level crud operations
 */

@Repository
public interface CreditLevelRepository extends JpaRepository<CreditLevel, Long> {

}

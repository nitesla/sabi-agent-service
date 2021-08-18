package com.sabi.agent.service.repositories;





import com.sabi.agent.core.models.LGA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for LGA crud operations
 */

@Repository
public interface LGARepository extends JpaRepository<LGA, Long> {
}

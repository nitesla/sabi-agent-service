package com.sabi.agent.service.repositories;





import com.sabi.agent.core.models.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Ward crud operations
 */

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
}

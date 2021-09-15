package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Supervisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Supervisor crud operations
 */

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Long>, JpaSpecificationExecutor<Supervisor> {

    Page<Supervisor> findAll(Pageable pageable);

    Supervisor findByUserId(Long userId);

    List<Supervisor> findByIsActive(Boolean isActive);
}

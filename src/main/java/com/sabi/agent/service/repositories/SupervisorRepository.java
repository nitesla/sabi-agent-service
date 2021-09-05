package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Supervisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Supervisor crud operations
 */

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {

    Page<Supervisor> findAll(Pageable pageable);
}

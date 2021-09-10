package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.UserTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for User Task crud operations
 */

@Repository
public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    UserTask findByTaskId(Long taskId);

   Page<UserTask> findAll(Pageable pageable);
}

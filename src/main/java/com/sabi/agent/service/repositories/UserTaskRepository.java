package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for User Task crud operations
 */

@Repository
public interface    UserTaskRepository extends JpaRepository<UserTask, Long>, JpaSpecificationExecutor<UserTask> {

    UserTask findByTaskId(Long taskId);

    UserTask findByUserId(Long userId);

    List<UserTask> findByIsActive(Boolean isActive);
}

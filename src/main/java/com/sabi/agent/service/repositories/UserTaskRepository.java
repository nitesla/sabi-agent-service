package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.UserTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 *
 * This interface is responsible for User Task crud operations
 */

@Repository
public interface    UserTaskRepository extends JpaRepository<UserTask, Long>, JpaSpecificationExecutor<UserTask> {

    UserTask findByTaskId(Long taskId);

    UserTask findByUserId(Long userId);

    List<UserTask> findByIsActive(Boolean isActive);

    @Query(value = "SELECT * from UserTask WHERE " +
            "((:taskName IS NULL) OR (:taskName IS NOT NULL AND UserTask.taskName LIKE %:taskName%)) " +
            "AND ((:userType IS NULL) OR (:userType IS NOT NULL AND UserTask.userType = :userType)) " +
            "AND ((:taskType IS NULL) OR (:taskType IS NOT NULL AND UserTask.taskType LIKE %:taskType%)) " +
            "AND ((:startDate IS NULL) AND (:endDate IS NULL) OR (UserTask.createdDate BETWEEN :startDate AND :endDate))", nativeQuery = true)
    Page<UserTask> filterUserTask(@Param("taskName") String taskName,
                             @Param("userType") String userType,
                             @Param("taskType") String taskType,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate,
                             Pageable pageable);
}

package com.sabi.agent.service.repositories;









import com.sabi.agent.core.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Task crud operations
 */

@SuppressWarnings("ALL")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Task findByNameAndTaskType(String name, String taskType);

    @Query("SELECT t FROM Task t WHERE ((:name IS NULL) OR (:name IS NOT NULL AND t.name = :name)) " +
            "AND ((:taskType IS NULL) OR (:taskType IS NOT NULL AND t.taskType = :taskType))" +
            " AND ((:priority IS NULL) OR (:priority IS NOT NULL AND t.priority = :priority))")
    Page<Task> findTask(@Param("name")String name,
                        @Param("taskType")String taskType,
                        @Param("priority")String priority, Pageable pageable);
}

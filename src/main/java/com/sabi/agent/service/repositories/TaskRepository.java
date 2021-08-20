package com.sabi.agent.service.repositories;








import com.sabi.agent.core.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Task crud operations
 */

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Task findByNameAndTaskType(String name, String taskType);
}

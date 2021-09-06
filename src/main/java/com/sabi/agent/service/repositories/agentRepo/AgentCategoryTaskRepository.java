package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * This interface is responsible for Agent Category Task crud operations
 */
public interface AgentCategoryTaskRepository extends JpaRepository<AgentCategoryTask, Long> {
    AgentCategoryTask findByName (String name);

    @Query("SELECT a FROM AgentCategoryTask a WHERE ((:name IS NULL) OR (:name IS NOT NULL AND a.name = :name))" +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND a.isActive = :isActive))")
    Page<AgentCategoryTask> findAgentCategoryTasks(@Param("name")String name,
                                                   @Param("isActive")Boolean isActive,
                                                   Pageable pageable);

}

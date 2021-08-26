package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 *
 * This interface is responsible for Agent Category crud operations
 */

@Repository
public interface AgentCategoryRepository extends JpaRepository<AgentCategory, Long> {

    AgentCategory findByName(String name);


    @Query("SELECT a FROM AgentCategory a WHERE ((:name IS NULL) OR (:name IS NOT NULL AND a.name = :name))")
    Page<AgentCategory> findAgentCategories(@Param("name")String name, Pageable pageable);
}

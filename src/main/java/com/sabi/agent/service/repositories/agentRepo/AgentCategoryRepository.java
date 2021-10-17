package com.sabi.agent.service.repositories.agentRepo;

import com.sabi.agent.core.models.agentModel.AgentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 *
 * This interface is responsible for Agent Category crud operations
 */

@Repository
public interface AgentCategoryRepository extends JpaRepository<AgentCategory, Long> , JpaSpecificationExecutor<AgentCategory> {

    AgentCategory findByName(String name);

    AgentCategory findAgentCategoriesByIsDefault(boolean isDefault);

    AgentCategory findAgentCategoriesById(Long id);


    @Query("SELECT a FROM AgentCategory a WHERE ((:name IS NULL) OR (:name IS NOT NULL AND a.name = :name))" +
            " AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND a.isActive = :isActive))"
    )
    Page<AgentCategory> findAgentCategories(@Param("name")String name, @Param("isActive")Boolean isActive,Pageable pageable);

    List<AgentCategory> findByIsActive(Boolean isActive);

    @Modifying
    @Query(value = "UPDATE agentcategory SET isDefault = 0", nativeQuery = true)
    void updateIsDefault();

    List<AgentCategory> findByIsDefault(Boolean isDefault);
}

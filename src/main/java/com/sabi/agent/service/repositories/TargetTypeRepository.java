package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for TargetType crud operations
 */

@Repository
public interface TargetTypeRepository extends JpaRepository<TargetType, Long> {
    TargetType findByName (String name);


    @Query("SELECT t FROM TargetType t WHERE ((:name IS NULL) OR (:name IS NOT NULL AND t.name = :name))")
    Page<TargetType> findTargetTypes(@Param("name")String name, Pageable pageable);


}

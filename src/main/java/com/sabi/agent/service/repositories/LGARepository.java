package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.LGA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for LGA crud operations
 */

@Repository
public interface LGARepository extends JpaRepository<LGA, Long> {

       LGA findByName (String name);

       @Query("SELECT l FROM LGA l WHERE ((:isActive IS NULL) OR (:isActive IS NOT NULL AND l.isActive = :isActive))" +
               " AND ((:stateId IS NULL) OR (:stateId IS NOT NULL AND l.stateId = :stateId))")
       List<LGA> findLgaWithStateId(@Param("isActive")Boolean isActive,
                                          @Param("stateId")Long stateId);

       @Query("SELECT l FROM LGA l WHERE ((:name IS NULL) OR (:name IS NOT NULL AND l.name LIKE %:name%))")
       Page<LGA> findLgas(@Param("name")String name, Pageable pageable);
       List<LGA> findByStateId(Long stateId);
}

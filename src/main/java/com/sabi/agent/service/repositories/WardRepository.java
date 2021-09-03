package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Ward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Ward crud operations
 */

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    Ward findByName (String name);

    @Query("SELECT w FROM Ward w WHERE ((:name IS NULL) OR (:name IS NOT NULL AND w.name = :name))")
    Page<Ward> findWards(@Param("name")String name, Pageable pageable);
}

package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for State crud operations
 */

@Repository
public interface StateRepository extends JpaRepository<State, Long>, JpaSpecificationExecutor<State> {

    State findByName(String name);

    @Query("SELECT s FROM State s WHERE ((:isActive IS NULL) OR (:isActive IS NOT NULL AND s.isActive = :isActive))" +
            " AND ((:countryId IS NULL) OR (:countryId IS NOT NULL AND s.countryId = :countryId))")
    List<State> findByStateWithCountryId(@Param("isActive")Boolean isActive,
                                           @Param("countryId")Long countryId);

    @Query("SELECT s FROM State s WHERE ((:name IS NULL) OR (:name IS NOT NULL AND s.name = :name))")
    Page<State> findStates(@Param("name")String name, Pageable pageable);

}

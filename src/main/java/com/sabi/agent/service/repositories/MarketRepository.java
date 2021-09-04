package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Market crud operations
 */

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    Market findByName (String name);
    @Query("SELECT i FROM Market i WHERE ((:name IS NULL) OR (:name IS NOT NULL AND i.name = :name))")
    Page<Market> findMarkets(@Param("name")String name, Pageable pageable);
}

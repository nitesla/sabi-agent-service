package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Market crud operations
 */

@Repository
public interface MarketRepository extends JpaRepository<Market, Long>, JpaSpecificationExecutor<Market> {
    Market findByName (String name);
    List<Market> findByIsActive(Boolean isActive);

    @Query("SELECT m FROM  Market m INNER JOIN Ward  w ON m.wardId = w.id INNER JOIN LGA l " +
            "ON w.lgaId = l.id INNER JOIN State s ON l.stateId = s.id INNER JOIN Country  c ON s.countryId = c.id WHERE (m.id=:marketId AND m.wardId=:wardId)")
    Market findMarketAndLocationInfo(Long marketId,Long wardId);
}

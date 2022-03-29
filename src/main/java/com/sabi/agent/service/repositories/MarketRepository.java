package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT m FROM  Market m " +
            "INNER JOIN Ward  w ON m.wardId = w.id INNER JOIN LGA l " +
            "ON w.lgaId = l.id INNER JOIN State s ON l.stateId = s.id " +
            "INNER JOIN Country  c ON s.countryId = c.id " +
            "WHERE (((:marketName IS NULL)  OR (:marketName IS NOT NULL AND m.name LIKE %:marketName%)) " +
            "AND ((:wardName IS NULL) OR (:wardName IS NOT NULL AND w.name=:wardName)) " +
            "AND ((:lgaName IS NULL) OR (:lgaName IS NOT NULL AND l.name=:lgaName))" +
            "AND ((:stateName IS NULL ) OR (:stateName IS NOT NULL AND s.name=:stateName))" +
            "AND ((:countryName IS NULL )OR (:countryName IS NOT NULL AND c.name=:countryName))" +
            "AND ((:isActive IS NULL) OR (:isActive IS NOT NULL AND m.isActive=:isActive)))")
    Page<Market> searchMarketsByWardStateLgaCountry(String marketName, String wardName, String lgaName, String stateName, String countryName, Boolean isActive, Pageable pageable);
}

package com.sabi.agent.service.repositories;






import com.sabi.agent.core.models.Bank;
import com.sabi.agent.core.models.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Market crud operations
 */

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    Market findByName (String name);
}

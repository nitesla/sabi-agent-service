package com.sabi.agent.service.repositories.billPayments;


import com.sabi.agent.core.models.billPayments.Airtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Airtime crud operations
 */

@Repository
public interface AirtimeRepository extends JpaRepository<Airtime, Long>, JpaSpecificationExecutor<Airtime> {


}

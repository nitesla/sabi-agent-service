package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 *
 * This interface is responsible for Sms crud operations
 */

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long>, JpaSpecificationExecutor<Sms> {


}

package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.RegisteredMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<RegisteredMerchant, Long>, JpaSpecificationExecutor<RegisteredMerchant> {
}

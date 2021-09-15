package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for TargetType crud operations
 */

@Repository
public interface TargetTypeRepository extends JpaRepository<TargetType, Long>, JpaSpecificationExecutor<TargetType> {
    TargetType findByName (String name);

    List<TargetType> findByIsActive(Boolean isActive);


}

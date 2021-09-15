package com.sabi.agent.service.repositories;


import com.sabi.agent.core.models.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * This interface is responsible for Ward crud operations
 */

@Repository
public interface WardRepository extends JpaRepository<Ward, Long>, JpaSpecificationExecutor<Ward> {

    Ward findByName (String name);

    List<Ward> findByIsActive(Boolean isActive);

}

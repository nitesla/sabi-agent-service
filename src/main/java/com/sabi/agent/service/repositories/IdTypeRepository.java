package com.sabi.agent.service.repositories;



import com.sabi.agent.core.models.IdType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdTypeRepository extends JpaRepository<IdType, Long> {

    IdType findByName(String name);

    List<IdType> findAllByName (String name);

    @Query("SELECT i FROM IdType i WHERE ((:name IS NULL) OR (:name IS NOT NULL AND i.name = :name))")
    Page<IdType> findIdTypes(@Param("name")String name, Pageable pageable);
}

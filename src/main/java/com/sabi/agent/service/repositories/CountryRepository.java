package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long>, JpaSpecificationExecutor<Country> {

    Country findByName(String name);

    @Query("SELECT c FROM Country c WHERE ((:isActive IS NULL) OR (:isActive IS NOT NULL AND c.isActive = :isActive))")
    List<Country> findByIsActive(@Param("isActive")Boolean isActive);

    Country findByCode(String code);



    @Query("SELECT c FROM Country c WHERE ((:name IS NULL) OR (:name IS NOT NULL AND c.name LIKE %:name%))" +
            " AND ((:code IS NULL) OR (:code IS NOT NULL AND c.code LIKE %:code%))")
    Page<Country> findCountries(@Param("name")String name,
                                @Param("code")String code,
                                Pageable pageable);

}

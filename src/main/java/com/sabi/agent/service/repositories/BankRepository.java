package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.Bank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long>, JpaSpecificationExecutor<Bank
        > {

    Bank findByName (String name);
    Bank findByBankCode(String bankCode);


    @Query("SELECT b FROM Bank b WHERE ((:isActive IS NULL) OR (:isActive IS NOT NULL AND b.isActive = :isActive))")
    List<Bank> findByIsActive(@Param("isActive")Boolean isActive);

    @Query("SELECT b FROM Bank b WHERE ((:name IS NULL) OR (:name IS NOT NULL AND b.name LIKE %:name%))" +
            " AND ((:bankCode IS NULL) OR (:bankCode IS NOT NULL AND b.bankCode LIKE %:bankCode%))")
    Page<Bank> findBanks(@Param("name")String name,
                                @Param("bankCode")String bankCode,
                                Pageable pageable);
}

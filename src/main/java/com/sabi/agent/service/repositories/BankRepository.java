package com.sabi.agent.service.repositories;

import com.sabi.agent.core.models.Bank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    Bank findByName (String name);
    Bank findByBankCode(String bankCode);

    @Query("SELECT b FROM Bank b WHERE ((:name IS NULL) OR (:name IS NOT NULL AND b.name = :name))" +
            " AND ((:bankCode IS NULL) OR (:bankCode IS NOT NULL AND b.bankCode = :bankCode))")
    Page<Bank> findBanks(@Param("name")String name,
                                @Param("bankCode")String bankCode,
                                Pageable pageable);
}

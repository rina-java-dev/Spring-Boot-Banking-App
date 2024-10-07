package com.banking.banking.app.repository;

import com.banking.banking.app.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByIdOrderByTimestampDesc(Long accountId);
}

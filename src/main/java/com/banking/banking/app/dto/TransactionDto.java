package com.banking.banking.app.dto;


public record TransactionDto(Long id, Long accountId, String transactionType, double amount, String timestamp) {
}

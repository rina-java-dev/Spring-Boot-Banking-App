package com.banking.banking.app.dto;

public record TransferFundDto(Long fromAccountId, Long toAccountId, double amount) {
}

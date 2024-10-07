package com.banking.banking.app.service;


import com.banking.banking.app.dto.AccountDto;
import com.banking.banking.app.dto.TransactionDto;
import com.banking.banking.app.dto.TransferFundDto;
import com.banking.banking.app.entity.Account;
import com.banking.banking.app.exception.AccountException;
import com.banking.banking.app.repository.AccountRepository;

import java.util.List;

public interface AccountService {
    AccountDto createAccount(AccountDto account);

    AccountDto getAccountById(Long id);

    AccountDto deposit(long id, double amount);

    AccountDto withdraw(long id, double amount);

    List<AccountDto> getAllAccounts();

    void deleteAccount(Long id);

    void transferFunds(TransferFundDto transferFundDto);


    List<TransactionDto> getAccountTransactions(Long accountId);


    // Default method to check if an account exists by ID
    default Account findAccountById(AccountRepository accountRepository, Long id) {
        return accountRepository
                .findById(id)
                .orElseThrow(() -> new AccountException("Account does not exist."));
    }



}

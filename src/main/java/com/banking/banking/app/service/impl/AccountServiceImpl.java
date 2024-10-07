package com.banking.banking.app.service.impl;

import com.banking.banking.app.dto.AccountDto;
import com.banking.banking.app.dto.TransactionDto;
import com.banking.banking.app.dto.TransferFundDto;
import com.banking.banking.app.entity.Account;
import com.banking.banking.app.entity.Transaction;
import com.banking.banking.app.exception.AccountException;
import com.banking.banking.app.mapper.AccountMapper;
import com.banking.banking.app.repository.AccountRepository;
import com.banking.banking.app.repository.TransactionRepository;
import com.banking.banking.app.service.AccountService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private TransactionRepository transactionRepository;


    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AccountDto createAccount(AccountDto account) {
        Account account1 = AccountMapper.mapToaccount(account);
        Account savedAccount = accountRepository.save(account1);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account account = findAccountById(accountRepository, id); // Reuse default method
        return AccountMapper.mapToAccountDto(account);
    }


    @Override
    public AccountDto deposit(long id, double amount) {
        Account account = findAccountById(accountRepository, id); // Reuse default method
        double total = account.getBalance() + amount;
        account.setBalance(total);

        // log deposit transaction

        Transaction transaction = new Transaction();
        transaction.setAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setTimestamp(LocalDateTime.now());


        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto withdraw(long id, double amount) {
        Account account = findAccountById(accountRepository, id); // Reuse default method


       if (account.getBalance() < amount) {
           throw new AccountException("Insufficient amount");
       }

       double total = account.getBalance() - amount;
       account.setBalance(total);


        // log withdraw transaction


        Transaction transaction = new Transaction();
        transaction.setAccountId(id);
        transaction.setTransactionType("WITHDRAW");
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());


        transactionRepository.save(transaction);


       Account savedAccount = accountRepository.save(account);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
       List<Account> accounts = accountRepository.findAll();
       return accounts.stream().map(AccountMapper::mapToAccountDto)
               .collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = findAccountById(accountRepository, id); // Reuse default method

        accountRepository.deleteById(id);
    }


    @Override
    public void transferFunds(TransferFundDto transferFundDto) {
        Account fromAccount = accountRepository.findById(transferFundDto.fromAccountId())
                .orElseThrow(() -> new AccountException("Account does not exist."));

        Account toAccount = accountRepository.findById(transferFundDto.toAccountId())
                .orElseThrow(() -> new AccountException("Account does not exist."));

        fromAccount.setBalance(fromAccount.getBalance() - transferFundDto.amount());
        toAccount.setBalance(toAccount.getBalance() + transferFundDto.amount());


        // log transfer transaction


        Transaction transaction = new Transaction();
        transaction.setAccountId(fromAccount.getId());
        transaction.setTransactionType("TRANSFER");
        transaction.setAmount(transferFundDto.amount());
        transaction.setTimestamp(LocalDateTime.now());


        transactionRepository.save(transaction);


        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Override
    public List<TransactionDto> getAccountTransactions(Long accountId) {

        List<Transaction> transactions = transactionRepository
                .findByIdOrderByTimestampDesc(accountId);


        return transactions.stream()
                .map((transaction) -> convertEntityToTransactionDto(transaction))
                        .collect(Collectors.toList());

    }

    private TransactionDto convertEntityToTransactionDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getTimestamp().toString()
        );
    }
}


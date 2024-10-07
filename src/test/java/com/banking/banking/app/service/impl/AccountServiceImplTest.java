package com.banking.banking.app.service.impl;

import com.banking.banking.app.dto.AccountDto;
import com.banking.banking.app.dto.TransferFundDto;
import com.banking.banking.app.entity.Account;
import com.banking.banking.app.exception.AccountException;
import com.banking.banking.app.exception.InsufficientFundsException;
import com.banking.banking.app.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountServiceImpl;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAccount() {
        // Given
        AccountDto accountDto = new AccountDto(1L, "John Doe", 500.0);
        Account account = new Account();
        account.setId(1L);
        account.setAccountHolderName("John Doe");
        account.setBalance(500.0);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        AccountDto createdAccount = accountServiceImpl.createAccount(accountDto);

        // Then
        assertNotNull(createdAccount);
        assertEquals(1L, createdAccount.getId());
        assertEquals("John Doe", createdAccount.getAccountHolderName());
        assertEquals(500.0, createdAccount.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testGetAccountById_AccountExists() {
        // Given
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);
        account.setAccountHolderName("John Doe");
        account.setBalance(500.0);

        AccountDto expectedAccountDto = new AccountDto(accountId, "John Doe", 500.0);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When
        AccountDto accountDto = accountServiceImpl.getAccountById(accountId);

        // Then
        assertNotNull(accountDto);
        assertEquals(1L, accountDto.getId());
        assertEquals("John Doe", accountDto.getAccountHolderName());
        assertEquals(500.0, accountDto.getBalance());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    public void testDeposit_AccountExists() {
        // Given
        Long accountId = 1L;
        double depositAmount = 100.0;
        Account account = new Account();
        account.setId(accountId);
        account.setAccountHolderName("John Doe");
        account.setBalance(500.0);

        Account updatedAccount = new Account();
        updatedAccount.setId(accountId);
        updatedAccount.setAccountHolderName("John Doe");
        updatedAccount.setBalance(600.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(updatedAccount);

        // When
        AccountDto updatedAccountDto = accountServiceImpl.deposit(accountId, depositAmount);

        // Then
        assertNotNull(updatedAccountDto);
        assertEquals(600.0, updatedAccountDto.getBalance());
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testWithdraw_AccountExists() {
        // Given
        Long accountId = 1L;
        double withdrawAmount = 50.0;
        Account account = new Account();
        account.setId(accountId);
        account.setAccountHolderName("John Doe");
        account.setBalance(500.0);

        Account updatedAccount = new Account();
        updatedAccount.setId(accountId);
        updatedAccount.setAccountHolderName("John Doe");
        updatedAccount.setBalance(450.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(updatedAccount);

        // When
        AccountDto updatedAccountDto = accountServiceImpl.withdraw(accountId, withdrawAmount);

        // Then
        assertNotNull(updatedAccountDto);
        assertEquals(450.0, updatedAccountDto.getBalance());
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testTransferFunds_ValidTransfer() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        double transferAmount = 100.0;

        Account fromAccount = new Account();
        fromAccount.setId(fromAccountId);
        fromAccount.setBalance(500.0);

        Account toAccount = new Account();
        toAccount.setId(toAccountId);
        toAccount.setBalance(200.0);

        TransferFundDto transferFundDto = new TransferFundDto(fromAccountId, toAccountId, transferAmount);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        // When
        accountServiceImpl.transferFunds(transferFundDto);

        // Then
        assertEquals(400.0, fromAccount.getBalance());
        assertEquals(300.0, toAccount.getBalance());
        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
    }

    @Test
    public void testTransferFunds_FromAccountNotFound() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        double transferAmount = 100.0;

        TransferFundDto transferFundDto = new TransferFundDto(fromAccountId, toAccountId, transferAmount);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountException.class, () -> accountServiceImpl.transferFunds(transferFundDto));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testTransferFunds_ToAccountNotFound() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        double transferAmount = 100.0;

        Account fromAccount = new Account();
        fromAccount.setId(fromAccountId);
        fromAccount.setBalance(500.0);

        TransferFundDto transferFundDto = new TransferFundDto(fromAccountId, toAccountId, transferAmount);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountException.class, () -> accountServiceImpl.transferFunds(transferFundDto));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testTransferFunds_InsufficientFunds() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        double transferAmount = 600.0;

        Account fromAccount = new Account();
        fromAccount.setId(fromAccountId);
        fromAccount.setBalance(500.0);

        Account toAccount = new Account();
        toAccount.setId(toAccountId);
        toAccount.setBalance(200.0);

        TransferFundDto transferFundDto = new TransferFundDto(fromAccountId, toAccountId, transferAmount);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        // When & Then
//        assertThrows(InsufficientFundsException.class, () -> {
//            accountServiceImpl.transferFunds(transferFundDto);
//        });
//        assertEquals(500.0, fromAccount.getBalance());
//        assertEquals(200.0, toAccount.getBalance());
//        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testTransferFunds_ZeroAmount() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        double transferAmount = 0.0;

        Account fromAccount = new Account();
        fromAccount.setId(fromAccountId);
        fromAccount.setBalance(500.0);

        Account toAccount = new Account();
        toAccount.setId(toAccountId);
        toAccount.setBalance(200.0);

        TransferFundDto transferFundDto = new TransferFundDto(fromAccountId, toAccountId, transferAmount);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        // When
        accountServiceImpl.transferFunds(transferFundDto);

        // Then
        assertEquals(500.0, fromAccount.getBalance());
        assertEquals(200.0, toAccount.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
    }
}

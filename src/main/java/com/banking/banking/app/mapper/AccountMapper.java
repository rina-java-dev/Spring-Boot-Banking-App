package com.banking.banking.app.mapper;

import com.banking.banking.app.dto.AccountDto;
import com.banking.banking.app.entity.Account;

public class AccountMapper {
    public static Account mapToaccount(AccountDto accountDto){
        Account account = new Account(
                accountDto.getId(),
                accountDto.getAccountHolderName(),
                accountDto.getBalance()
        );
        return account;

    }

    public static AccountDto mapToAccountDto(Account account) {
        AccountDto accountDto = new AccountDto(
                account.getId(),
                account.getAccountHolderName(),
                account.getBalance()
        );
        return accountDto;
    }
}

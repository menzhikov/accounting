package com.menzhikov.accounting.services;

import com.menzhikov.accounting.entities.Account;
import com.menzhikov.accounting.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAmount(Long id, int amount) {
        log.info("incrementAmount: started for {} by {}", id, amount);
        Account account = accountRepository.getAccountAndObtainPessimisticWriteLockingOnItById(id);
        account.setAmount(account.getAmount() + amount);
        log.info("incrementAmount: finished for {} by {}", id, amount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrementAmount(Long id, int amount) {
        log.info("decrementAmount: started for {} by {}", id, amount);
        Account account = accountRepository.getAccountAndObtainPessimisticWriteLockingOnItById(id);
        if (account.getAmount() - amount < 0) {
            log.error("decrementAmount: account {} have not enough amount on balance", id);
            throw new IllegalStateException("not enough amount on balance");
        }
        account.setAmount(account.getAmount() - amount);
        log.info("decrementAmount: finished for {} by {}", id, amount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transferAmount(Long from, Long to, int amount) {
        log.info("transferAmount: started for {} and {} by {}", from, to, amount);
        Account accountFrom = accountRepository.getAccountAndObtainPessimisticWriteLockingOnItById(from);
        if (accountFrom.getAmount() - amount < 0) {
            log.error("transferAmount: account {} have not enough amount on balance", from);
            throw new IllegalStateException("not enough amount on balance");
        }
        Account accountTo = accountRepository.getAccountAndObtainPessimisticWriteLockingOnItById(to);
        accountFrom.setAmount(accountFrom.getAmount() - amount);
        accountTo.setAmount(accountTo.getAmount() + amount);
        log.info("transferAmount: finished for {} and {} by {}", from, to, amount);
    }
}

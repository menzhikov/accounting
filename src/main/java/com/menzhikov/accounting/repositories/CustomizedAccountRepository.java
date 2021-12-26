package com.menzhikov.accounting.repositories;

import com.menzhikov.accounting.entities.Account;

public interface CustomizedAccountRepository {

    void setLockTimeout(long timeoutDurationInMs);

    long getLockTimeout();

    Account getAccountAndObtainPessimisticWriteLockingOnItById(Long id);
}

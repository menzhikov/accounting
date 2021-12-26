package com.menzhikov.accounting.repositories;

import com.menzhikov.accounting.entities.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class CustomizedAccountRepositoryImpl implements CustomizedAccountRepository {

    @Getter
    @Value("${concurrency.pessimisticLocking.requiredToSetLockTimeoutForTestsAtStartup: true}")
    private boolean requiredToSetLockTimeoutForTestsAtStartup;

    @Getter
    @Value("${concurrency.pessimisticLocking.minimalPossibleLockTimeOutInMs: 0}")
    private long minimalPossibleLockTimeOutInMs;

    @Getter
    @Value("${concurrency.pessimisticLocking.lockTimeOutInMsForQueryGetAccount: 5000}")
    private long lockTimeOutInMsForQueryGetAccount;

    private final EntityManager em;

    @Override
    public void setLockTimeout(long timeoutDurationInMs) {
        log.debug("setLockTimeout: Started setting lock timout {} ms", timeoutDurationInMs);
        long timeoutDurationInSec = TimeUnit.MILLISECONDS.toSeconds(timeoutDurationInMs);
        Query query = em.createNativeQuery(String.format("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.locks.waitTimeout',  '%d')",
                timeoutDurationInSec));
        query.executeUpdate();
        log.debug("setLockTimeout: Finished setting lock timout {} ms", timeoutDurationInMs);
    }

    @Override
    public long getLockTimeout() {
        log.debug("getLockTimeout: Started getting lock timout in ms");
        Query query = em.createNativeQuery("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('derby.locks.waitTimeout')");
        long timeoutDurationInSec = Long.valueOf((String) query.getSingleResult());
        log.debug("getLockTimeout: Finished getting lock timout in ms");
        return TimeUnit.SECONDS.toMillis(timeoutDurationInSec);
    }

    @Override
    public Account getAccountAndObtainPessimisticWriteLockingOnItById(Long id) {
        log.debug("getAccountAndObtainPessimisticWriteLockingOnItById: Started trying to obtain pessimistic lock for {}", id);

        Query query = em.createQuery("select Account from Account Account where Account.id = :id");
        query.setParameter("id", id);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        Account account = (Account) query.getSingleResult();

        log.debug("getAccountAndObtainPessimisticWriteLockingOnItById: pessimistic lock for {} obtained", id);
        return account;
    }
}

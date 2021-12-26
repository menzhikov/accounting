package com.menzhikov.accounting.services;

import com.menzhikov.accounting.entities.Account;
import com.menzhikov.accounting.repositories.AccountRepository;
import com.menzhikov.accounting.repositories.CustomizedAccountRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class AccountServicePessimisticLockingTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ManagerService managerService;

    @SpyBean
    private AccountService AccountService;

    @SpyBean
    private CustomizedAccountRepositoryImpl customizedAccountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    void shouldIncrementAccountAmount_withoutConcurrency() throws Exception {
        assertIncrementAccountAmountWithPessimisticLocking(false, false, 2);
    }

    @Test
    void shouldIncrementAccountAmount_withinPessimisticLockingConcurrencyWithMinimalLockTimeout() throws Exception {
        assertIncrementAccountAmountWithPessimisticLocking(true, true, 3);
    }

    @Test
    void shouldIncrementAccountAmount_withinPessimisticLockingConcurrencyWithDefaultLockTimeout() throws Exception {
        assertIncrementAccountAmountWithPessimisticLocking(true, false, 2);
    }

    private void assertIncrementAccountAmountWithPessimisticLocking(
            boolean simulatePessimisticLocking,
            boolean hasToSetMinimalLockTimeOut,
            int expectedNumberOfAccountServiceInvocations
    ) throws Exception {

        // given
        if (hasToSetMinimalLockTimeOut) {
            long lockTimeOutInMs = customizedAccountRepository.getMinimalPossibleLockTimeOutInMs();
            when(customizedAccountRepository.getLockTimeOutInMsForQueryGetAccount()).thenReturn(lockTimeOutInMs);
        }

        if (hasToSetMinimalLockTimeOut && customizedAccountRepository.isRequiredToSetLockTimeoutForTestsAtStartup()) {
            log.info("... set lockTimeOut {} ms through native query at startup ...", customizedAccountRepository.getMinimalPossibleLockTimeOutInMs());
            TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
            accountRepository.setLockTimeout(customizedAccountRepository.getMinimalPossibleLockTimeOutInMs());
            transactionManager.commit(tx);
        }

        final Account entity = new Account();
        entity.setId(new Random().nextLong());
        final Account srcAccount = accountRepository.save(entity);

        // when
        final List<Integer> AccountAmounts = Arrays.asList(10, 5);

        if (simulatePessimisticLocking) {
            final ExecutorService executor = Executors.newFixedThreadPool(AccountAmounts.size());

            for (final int amount : AccountAmounts) {
                executor.execute(() -> managerService.incrementAmount(srcAccount.getId(), amount));
            }

            executor.shutdown();
            assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));
        } else {
            for (final int amount : AccountAmounts) {
                managerService.incrementAmount(srcAccount.getId(), amount);
            }
        }

        // then
        final Account Account = accountRepository.findById(srcAccount.getId()).orElseThrow(() -> new IllegalArgumentException("Account not found!"));

        assertAll(
                () -> assertEquals(15, Account.getAmount()),
                () -> verify(AccountService, times(expectedNumberOfAccountServiceInvocations)).incrementAmount(any(Long.class), anyInt())
        );
    }

    @Test
    void shouldSetAndGetLockTimeOut() {
        assertSetLockTimeOut(customizedAccountRepository.getMinimalPossibleLockTimeOutInMs());
        assertSetLockTimeOut(TimeUnit.SECONDS.toMillis(2));
        assertSetLockTimeOut(TimeUnit.MINUTES.toMillis(2));
        assertSetLockTimeOut(TimeUnit.HOURS.toMillis(2));
        assertSetLockTimeOut(TimeUnit.DAYS.toMillis(2));
    }

    private void assertSetLockTimeOut(long expectedMilliseconds) {
        TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        accountRepository.setLockTimeout(expectedMilliseconds);
        assertEquals(expectedMilliseconds, accountRepository.getLockTimeout());
        transactionManager.commit(tx);
    }
}

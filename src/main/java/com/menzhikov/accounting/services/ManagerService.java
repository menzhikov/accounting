package com.menzhikov.accounting.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class ManagerService {

    private static final long PESSIMISTIC_LOCKING_EXCEPTION_HANDLING_RETRY_AFTER_MS = 200;

    private final AccountService accountService;

    public void incrementAmount(Long id, int amount) {
        try {
            accountService.incrementAmount(id, amount);
        } catch (PessimisticLockingFailureException e) {
            log.error("incrementAmount: Found pessimistic lock exception!", e);
            sleepForAWhile();
            accountService.incrementAmount(id, amount);
        }
    }

    public void decrementAmount(Long id, int amount) {
        try {
            accountService.decrementAmount(id, amount);
        } catch (PessimisticLockingFailureException e) {
            log.error("decrementAmount: Found pessimistic lock exception!", e);
            sleepForAWhile();
            accountService.decrementAmount(id, amount);
        }
    }

    public void transferAmount(Long from, Long to, int amount) {
        try {
            accountService.transferAmount(from, to, amount);
        } catch (PessimisticLockingFailureException e) {
            log.error("transferAmount: Found pessimistic lock exception!", e);
            sleepForAWhile();
            accountService.transferAmount(from, to, amount);
        }
    }

    private static void sleepForAWhile() {
        try {
            TimeUnit.MILLISECONDS.sleep(PESSIMISTIC_LOCKING_EXCEPTION_HANDLING_RETRY_AFTER_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}

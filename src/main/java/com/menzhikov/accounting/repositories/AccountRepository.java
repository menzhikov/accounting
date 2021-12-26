package com.menzhikov.accounting.repositories;

import com.menzhikov.accounting.entities.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long>, CustomizedAccountRepository {

}

package dev.saha.redis_test.repository;

import dev.saha.redis_test.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByProductCode(String productCode);
    List<Account> findAllByIsPickedAndIsUsed(String isPicked, String isUsed);
    boolean existsAccountByPlainData(String plainData);

    Optional<Account> findByAccountRef(String accountRef);

    Optional<Account> findByPlainData(String plainData);

    List<Account> findAll();

}

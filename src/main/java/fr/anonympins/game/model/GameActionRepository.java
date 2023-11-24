package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(nativeQuery = false, value = "SELECT a FROM Account a INNER JOIN AccountProvider ap ON ap.provider='internal' AND ap.token=?2 WHERE a.username=?1")
    Optional<Account> findAccountByCredentials(String user, String pass);
}

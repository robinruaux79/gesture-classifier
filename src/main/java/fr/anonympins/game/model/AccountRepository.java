package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.Account;
import fr.anonympins.game.model.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByHash(String hash);

    @Query(nativeQuery = false, value = "SELECT a FROM Account a INNER JOIN AccountProvider ap ON ap.provider='account' AND ap.token=?2 WHERE a.username=?1")
    List<Account> findAccountByCredentials(String user, String pass, Pageable pageable);

    @Query(nativeQuery = false, value = "SELECT a FROM Account a INNER JOIN AccountProvider ap ON ap.provider=?2 AND ap.token=?1")
    List<Account> findAccountByAuth(String token, String provider);
}

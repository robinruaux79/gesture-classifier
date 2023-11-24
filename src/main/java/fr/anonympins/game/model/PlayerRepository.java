package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.Account;
import fr.anonympins.game.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findFirstByAccount(Account a);

}

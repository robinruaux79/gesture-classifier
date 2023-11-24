package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.Account;
import fr.anonympins.game.model.entity.Game;
import fr.anonympins.game.model.entity.GameAction;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface GameActionRepository extends JpaRepository<GameAction, Long> {

    @Query("SELECT a FROM GameAction a WHERE a.type = ?1 AND function('JSON_EXTRACT', a.parameters, '$.'+?2) = ?3")
    List<GameAction> findByParam(GameAction.ActionType type, String param, String value);

    @Query("SELECT a FROM GameAction a WHERE function('JSON_EXTRACT', a.parameters, '$.'+?1) = ?2")
    List<GameAction> findByParam(String param, String value);
}

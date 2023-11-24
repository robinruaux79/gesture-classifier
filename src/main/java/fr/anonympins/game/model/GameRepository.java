package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findFirstByName(String name);

    void deleteByName(String name);

}

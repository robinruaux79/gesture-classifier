package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.GameAction;
import fr.anonympins.game.model.entity.GameObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameObjectRepository extends JpaRepository<GameObject, Long> {

}

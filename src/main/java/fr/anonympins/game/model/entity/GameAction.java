package fr.anonympins.game.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
public class GameAction {

    enum ActionEnum {
        CREATE_GAME,
        EDIT_GAME,
        DELETE_GAME,
        JOIN_GAME,
        LEAVE_GAME,
        CREATE_GAMEOBJECT,
    };

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private ActionEnum name;

    @OneToOne(fetch = FetchType.LAZY)
    private Game game;

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> parameters;

    private LocalDateTime createdAt;
}

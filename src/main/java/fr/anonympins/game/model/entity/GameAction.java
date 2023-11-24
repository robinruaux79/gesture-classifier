package fr.anonympins.game.model.entity;

import fr.anonympins.game.model.GameObjectRepository;
import fr.anonympins.game.model.HashMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.reflections.Reflections;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(of = {"id", "name"})
public class GameAction implements Cloneable {

    public enum ActionType {
        CREATE_GAME,
        EDIT_GAME,
        DELETE_GAME,
        JOIN_GAME,
        LEAVE_GAME,
        CREATE_GAMEOBJECT,

        ATTACK_PLAYER,

        TALK_PLAYER,

        USE_ITEM,



        DROP_ITEM,

        LEFT,
        RIGHT,
        TOP,
        BOTTOM,

        RUN_LEFT,
        RUN_RIGHT,
        RUN_TOP,
        RUN_BOTTOM,

        /*
        EAST,
        WEST,
        NORTH,
        SOUTH,*/
        NOP
    };

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private ActionType type;

    @OneToOne(fetch = FetchType.LAZY)
    private Game game;

    @OneToOne(fetch = FetchType.LAZY)
    private GameObject initiator;

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> parameters;

    private LocalDateTime createdAt;

    public Double getDoubleClass(){
        Reflections reflections = new Reflections(this.getClass().getPackage().getName() + ".command.defaults");
        Set<Class<? extends GameAction>> classes = reflections.getSubTypesOf(GameAction.class);
        // Iterate through all the detected/found classes
        return (double) new ArrayList<>(classes).indexOf(this.getClass());
    }


    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

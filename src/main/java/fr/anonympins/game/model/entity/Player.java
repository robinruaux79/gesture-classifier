package fr.anonympins.game.model.entity;

import fr.anonympins.game.model.Sense;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id"})
@Builder
@EqualsAndHashCode(of={"id"})
public class Player {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private  Account account;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Person character;

    private Boolean isOnline, isBanished;

    private LocalDateTime createdAt, lastOnlineAt;

    public void doAction(GameAction action){
        action.getGame().doAction(action);
    }
}

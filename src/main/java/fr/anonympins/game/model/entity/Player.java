package fr.anonympins.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private  Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private  Account account;

    public void doAction(GameAction action){
        action.getGame().doAction(action);
    }
}

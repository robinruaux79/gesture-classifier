package fr.anonympins.game.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "seed", "players", "gameObjects"})
public class Game {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String seed, name;

    private Integer minPlayers;

    private Integer maxPlayers;

    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private List<Player> players = new ArrayList<>();


    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private List<GameObject> gameObjects = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();


    public void doAction(GameAction action){

    }
}

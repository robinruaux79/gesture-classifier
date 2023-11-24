package fr.anonympins.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String seed;

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

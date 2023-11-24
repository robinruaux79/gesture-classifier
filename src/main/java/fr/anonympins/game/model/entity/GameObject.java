package fr.anonympins.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameObject {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private GameObjectType type;

    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private Location location;

    private Double weight = 0.250;

    private Double basePrice = 1.0;

    String getDescription(){
        return name;
    }
}

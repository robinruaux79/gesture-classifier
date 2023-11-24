package fr.anonympins.game.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Item extends GameObject {

    @Id
    Long id;

    private Double weight = 0.250;

    private Double basePrice = 1.0;

}

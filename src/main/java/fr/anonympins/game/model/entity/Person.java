package fr.anonympins.game.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false, of = {"id"})
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, of = {"firstname", "lastname"})
@SuperBuilder
public class Person extends GameObject {

    private String firstname, lastname;

    private LocalDateTime birthdate;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Location birthPlace;

    @Builder.Default
    private String gender = "male";

}

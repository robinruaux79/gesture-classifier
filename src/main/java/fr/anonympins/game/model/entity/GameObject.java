package fr.anonympins.game.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name"})
public class GameObject {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private Location location;

    String getDescription(){
        return name;
    }

    public Double getDoubleClass(){
        Reflections reflections = new Reflections(this.getClass().getPackage().getName() + ".command.defaults");
        Set<Class<? extends GameObject>> classes = reflections.getSubTypesOf(GameObject.class);
        // Iterate through all the detected/found classes
        return (double) new ArrayList<>(classes).indexOf(this.getClass());
    }
}

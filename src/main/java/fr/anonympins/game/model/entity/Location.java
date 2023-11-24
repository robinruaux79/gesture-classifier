package fr.anonympins.game.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Random;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "latitude", "longitude", "altitude"})
public class Location implements Cloneable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Double x,y,z, latitude, longitude, altitude;

    private String city, postalCode, region, country, administrative_area;

    public Location randomPosition(){

        Random r = new Random();
        setX(r.nextDouble(-100, 100));
        setY(r.nextDouble(-100, 100));
        setZ(0.0d);

        setLatitude(r.nextDouble(-180, 180));
        setLongitude(r.nextDouble(-180, 180));
        setAltitude(0.0d);

        return this;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

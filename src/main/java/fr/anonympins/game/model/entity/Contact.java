package fr.anonympins.game.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Location location;

    String email;

    String mobilePhone, homePhone, faxNumber;
}

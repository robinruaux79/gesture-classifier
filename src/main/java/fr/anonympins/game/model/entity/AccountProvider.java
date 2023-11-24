package fr.anonympins.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountProvider {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String provider;

    private String token;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private Account account;
}

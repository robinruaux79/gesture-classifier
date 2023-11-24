package fr.anonympins.game.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "provider"})
public class AccountProvider {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String provider;

    private String token;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private Account account;
}

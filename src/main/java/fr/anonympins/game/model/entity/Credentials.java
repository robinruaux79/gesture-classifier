package fr.anonympins.game.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString(of = {"username"})
public class Credentials {
    private String username;
    private String password;

}

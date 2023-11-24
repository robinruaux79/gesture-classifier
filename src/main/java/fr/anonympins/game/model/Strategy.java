package fr.anonympins.game.model;

import lombok.Data;

import java.util.function.Function;

@Data
public class Strategy {
    String name;
    Function<Object, Object> callback;
}

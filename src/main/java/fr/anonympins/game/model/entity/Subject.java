package fr.anonympins.game.model.entity;

import lombok.Data;

@Data
public class Subject {

    private Subject parent;

    private String nominalGroup;
    private Integer qty;

}

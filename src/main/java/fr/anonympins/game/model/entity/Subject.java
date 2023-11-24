package fr.anonympins.game.model;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class Subject {

    private Subject parent;

    private String nominalGroup;
    private Integer qty;

}

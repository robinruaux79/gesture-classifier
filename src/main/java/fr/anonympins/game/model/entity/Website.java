package fr.anonympins.game.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Website {
    @Id
    Long id;

    String url, title, generator, description;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    WebContent content;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Account author;

    @ElementCollection
    @CollectionTable(name="keywords", joinColumns=@JoinColumn(name="keyword_id"))
    List<String> keywords;
}

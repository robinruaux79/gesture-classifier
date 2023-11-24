package fr.anonympins.game.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebContent {

    @Id
    Long id;

    @Lob
    @Column(length = 1500)
    String content;

    String title, template;

    LocalDateTime createdAt, updatedAt;
}

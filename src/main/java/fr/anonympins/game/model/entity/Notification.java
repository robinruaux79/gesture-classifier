package fr.anonympins.game.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "canal", "title", "message", "sender"})
public class Notification {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Account sender;

    private String title, canal;

    @Lob
    @Column(length=1024)
    private String message;

    private String iconUrl, backgroundUrl;

    private String targetAction;

    private LocalDateTime sentAt = LocalDateTime.now();
}

package fr.anonympins.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("fr.anonympins.game")
@SpringBootApplication
public class GameApplication {

	public static void main(String[] args) {

		SpringApplication.run(GameApplication.class, args);
	}

}

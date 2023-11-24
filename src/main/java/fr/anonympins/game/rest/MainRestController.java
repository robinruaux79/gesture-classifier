package fr.anonympins.game.rest;

import fr.anonympins.game.model.entity.*;
import fr.anonympins.game.service.AuthService;
import fr.anonympins.game.service.GameService;
import fr.anonympins.game.service.LanguageService;
import fr.anonympins.game.service.MessageService;
import fr.anonympins.game.utils.HashUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class MainRestController {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    MessageService messageService;

    @Autowired
    GameService gameService;

    @Autowired
    AuthService authService;

    @Autowired
    LanguageService languageService;

    public Optional<Player> playerMiddleware(WebSession session){
        var pid = (Long)session.getAttribute("player_id");
        var player = gameService.currentPlayer(pid);
        if( player.isEmpty() ){

            Optional<Account> a = authService.getAccount((Long)session.getAttribute("account_id"));
            if( a.isEmpty() ){
                return Optional.empty();
            }
            player = Optional.of(Player.builder()
                        .account(a.get())
                        .build());
        }
        return player;
    }

    @GetMapping(value = "/")
    public Mono<Rendering> renderConnection(){
        return Mono.just(Rendering.view("index").build());
    }
    @PostMapping(value = "/connect")
    public Mono<Rendering> connectToGame(
            Credentials credentials,
            WebSession session
    ){
        Optional<Account> account;
        try {
            account = authService.getAccountByCredentials(
                    credentials.getUsername(),
                    credentials.getPassword());
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not parse password."));
        }
        return account.map(value -> {
            session.getAttributes().put("account_id", value.getId());
            return session.save().then(Mono.just(Rendering.redirectTo("/game").build()));
        }).orElse(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found")));
    }

    @GetMapping(value = "/game", produces = "text/html;charset=UTF-8")
    @Transactional
    public Mono<Rendering> renderGame(ServerHttpRequest request, WebSession session){
        Optional<Player> player = playerMiddleware(session);
        try {
            return player.map(p -> Mono.just(Rendering.view("game.html")
                    .modelAttribute("notifications", p.getAccount().getNotifications())
                    .build())).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Player not found"));
        }
        catch (Exception e ){
            return Mono.just(Rendering.view("error")
                    .modelAttribute("msg", e.getMessage()).build());
        }
    }

    @Transactional
    @GetMapping(value = "/install", produces = "text/html;charset=UTF-8")
    public Mono<Rendering> install(ServerHttpRequest request, WebSession session) throws NoSuchAlgorithmException {

        gameService.createGame("ADMIN_WORLD", "default");
        var player = Player.builder()
                .character(Person.builder()
                        .firstname("Robin")
                        .lastname("Ruaux")
                        .birthdate(LocalDateTime.of(1990, 4, 20, 13, 10))
                        .location(Location.builder().build().randomPosition())
                        .build())
                .account(Account.builder()
                        .username("robinouu")
                        .hash(HashUtils.sha256("robinouu"))
                        .accountProviders(List.of(
                                AccountProvider.builder()
                                        .provider("account")
                                        .token(HashUtils.sha256("test"))
                                        .build(),
                                AccountProvider.builder()
                                        .provider("token")
                                        .token(HashUtils.sha256("robinouu"))
                                        .build()))
                        .roles(List.of(Role.builder()
                                .name("ADMIN")
                                .authorities(List.of(
                                        Authority.builder()
                                                .name("CAN_CREATE_GAME")
                                                .build(),
                                        Authority.builder()
                                                .name("CAN_DELETE_GAME")
                                                .build()
                                )).build()))

                        .build()
                ).build();
        messageService.send(player.getAccount(),
                Notification.builder()
                        .canal("accounts[robinouu]")
                        .sender(player.getAccount())
                        .title("Test")
                        .message("Bienvenue sur ce jeu, robinouu")
                        .backgroundUrl("https://upload.wikimedia.org/wikipedia/commons/7/76/Foret.JPG")
                        .iconUrl("")
                        .build()
        );
        entityManager.persist(player);
        return Mono.just(Rendering.view("install.html")
                .build());
    }

    @GetMapping(value = "/clear")
    public Mono<Rendering> clearGame(){
        gameService.resetAll();
        return Mono.just(Rendering.redirectTo("/").build());
    }
    @GetMapping(value = "/language")
    public Mono<Rendering> language(@RequestParam String request) throws IOException {
        return languageService.trainModel().flatMap(model -> {
            String msg = "";
            String back = "";
            for(int i = 0; i < request.length(); ++i){
                msg += request.charAt(i);
                back += languageService.getBestResponse(msg);
            }
            return Mono.just(Rendering.view("ia")
                    .modelAttribute("msg", back)
            .build());
        });
    }

    @GetMapping(value = "/llm")
    public Mono<Rendering> llm(@RequestParam String request) throws IOException {
        return languageService.getNextToken(request).flatMap(token -> {
            return Mono.just(Rendering.view("ia")
                    .modelAttribute("msg", token)
                    .build());
        });
    }
    @GetMapping(value = "/generate-model")
    public Mono<Rendering> llmModel() throws IOException {
        languageService.generateModel();
        return Mono.just(Rendering.view("ia")
                .modelAttribute("msg", "Model generated.")
                .build());
    }

//    @Scheduled(fixedDelay = 2000)
//    @GetMapping(value = "/llm-register")
    public Mono<Boolean> llmRegisterText() throws IOException {
        System.out.println("Registering for new text...");
        return languageService.getText().flatMap(text-> Mono.fromCallable(() -> {
                System.out.println(text);
                languageService.registerText(text);
                return Mono.just(true);
            })).block();/*.then(Mono.just(Rendering.view("ia")
                .modelAttribute("msg", "Text trained.")
                .build()));*/
    }

    @GetMapping(value = "/llm-request")
    public Mono<Rendering> llmRequest(@RequestParam String request) throws IOException {
        String token = languageService.predictToken(request);
        return Mono.just(Rendering.view("ia")
                .modelAttribute("msg", token)
                .build());
    }
    @GetMapping(value = "/grammar")
    public Mono<Rendering> grammarRequest(@RequestParam String request) throws IOException {
        String grammar = languageService.predictGrammar(request);
        return Mono.just(Rendering.view("ia")
                .modelAttribute("msg", grammar)
                .build());
    }


}

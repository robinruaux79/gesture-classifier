package fr.anonympins.game.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.anonympins.game.model.entity.Account;
import fr.anonympins.game.model.entity.GameAction;
import fr.anonympins.game.model.entity.Person;
import fr.anonympins.game.model.entity.Player;
import fr.anonympins.game.service.GameService;
import fr.anonympins.game.service.IAService;
import fr.anonympins.game.utils.MapUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kotlin.jvm.functions.Function2;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Data
@Component
public class WebSocketServer implements WebSocketHandler {

    @Autowired
    private TransactionTemplate transactionTemplate;

    List<Function2<WebSocketMessage, WebSocketSession, Boolean>> middlewares;

    private final GameService gameService;

    @Autowired
    GameActionRepository actionRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PersonRepository personsRepository;

    @Autowired
    IAService iaService;

    @PersistenceContext
    EntityManager entityManager;

    public WebSocketServer(GameService gameService){
        this.gameService = gameService;
        middlewares = List.of(

                this::middlewareHandleGameActions);
    }

    @Transactional
    public Boolean middlewareHandleGameActions(WebSocketMessage message, WebSocketSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        GameAction action = null;
        try {
            action = objectMapper.readValue(message.getPayloadAsText(), GameAction.class);
        } catch (JsonProcessingException e) {
            return false;
        }
        if( action.getType() != null ) {
            System.out.println("Handling " + action.getType() + " action...");
            var player = gameService.currentPlayer((String)action.getParameters().getOrDefault("account", ""));
            if(player.isPresent()){
                var p = player.get();
                gameService.getSessions().put(p, session);
                p.setLastOnlineAt(LocalDateTime.now());

                System.out.println("Running action...");
                action.setInitiator(p.getCharacter());
                onAction(session, action, p);

                System.out.println("Learning...");
                iaService.learn(p, action);
                System.out.println("Done.");
                //actionRepository.save(action);
            }
        }
        return true;
    }

    @Transactional
    public void onAction(WebSocketSession session, GameAction action, Player player) {
        if (action.getType().equals(GameAction.ActionType.CREATE_GAME)) {
            gameService.createGame(
                    (String) action.getParameters().getOrDefault("seed", String.valueOf(new Date().getTime())),
                    (String) action.getParameters().getOrDefault("game", "default"));
        } else if (action.getType().equals(GameAction.ActionType.DELETE_GAME)) {
            gameService.deleteGame((String) action.getParameters().getOrDefault("game", "default"));
        } else if (action.getType().equals(GameAction.ActionType.EDIT_GAME)) {
            //gameService.editGame();
        } else if (action.getType().equals(GameAction.ActionType.JOIN_GAME)) {
            gameService.joinGame(
                    (String) action.getParameters().getOrDefault("game", "default"),
                    player.getAccount());
        } else if (action.getType().equals(GameAction.ActionType.LEAVE_GAME)) {
            gameService.leaveGame(
                    (String) action.getParameters().getOrDefault("game", "default"),
                    player.getAccount());
        }else if (action.getType().equals(GameAction.ActionType.CREATE_GAMEOBJECT)) {
            gameService.createGameObject(
                    (String) action.getParameters().getOrDefault("game", "default"),
                    player.getAccount(),
                    action.getParameters().getOrDefault("object", new HashMap<>()));
        }else if (action.getType().equals(GameAction.ActionType.LEFT) ||
                action.getType().equals(GameAction.ActionType.RIGHT) ||
                action.getType().equals(GameAction.ActionType.TOP) ||
                action.getType().equals(GameAction.ActionType.BOTTOM)) {
            iaService.handleAction(action, player.getCharacter()).subscribe();
        }
    }

    @Data
    @Builder
    static public class InitMessage {
        private List<Person> persons;
        private Player player;
    }

    @SneakyThrows
    @NotNull
    @Override
    @Transactional
    public Mono<Void> handle(WebSocketSession session) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return session.send(Mono.just(
                session.textMessage(mapper.writeValueAsString(
                        InitMessage.builder()
                                .player(MapUtils.getKeyByValue(gameService.getSessions(), session))
                                .persons(personsRepository.findAll()).build()
                ))))
                .then(session.receive().doOnNext(msg -> {
                    for (Function2<WebSocketMessage, WebSocketSession, Boolean> m : middlewares) {
                        try {
                            m.invoke(msg, session);
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    }
                }).then());
    }

}
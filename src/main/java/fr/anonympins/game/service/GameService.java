package fr.anonympins.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.anonympins.game.model.*;
import fr.anonympins.game.model.entity.*;
import fr.anonympins.game.utils.DoubleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class GameService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameObjectRepository gameObjectRepository;

    @Autowired
    GameActionRepository gameActionRepository;

    @Autowired
    PersonRepository personsRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    MessageService messageService;

    @Autowired
    IAService iaService;

    public Optional<Player> currentPlayer(Long id){
        if( id == null )
            return Optional.empty();
        var p = playerRepository.findById(id);
        return p;
    }

    public Optional<Player> currentPlayer(String token){
        Optional<Player> player = Optional.empty();
        var accounts = accountRepository.findAccountByAuth(token, "token");
        if (!accounts.isEmpty())
            player = playerRepository.findFirstByAccount(accounts.get(0));
        return player;
    }

    @Transactional
    public void createGame(String seed, String name){
        //todo: has right ?
        Game g = Game.builder().seed(seed).name(name).build();
        entityManager.persist(g);
    }

    @Transactional
    public void deleteGame(String gameName){
        //todo: has right ?
        gameRepository.deleteByName(gameName);
    }

    @Transactional
    public void editGame(){
        //todo: has right ?
        //gameRepository.deleteById(id);
    }

    @Transactional
    public void joinGame(String gameName, Account account){
        var game = gameRepository.findFirstByName(gameName);
        var player = playerRepository.findFirstByAccount(account);
        game.ifPresent(g -> {
            player.ifPresent(p -> {
                if (!g.getPlayers().contains(p)) {
                    g.getPlayers().add(p);
                    entityManager.persist(g);
                }
            });
        });
    }
    @Transactional
    public void leaveGame(String gameName, Account account){
        var game = gameRepository.findFirstByName(gameName);
        var player = playerRepository.findFirstByAccount(account);
            game.ifPresent(g -> {
                player.ifPresent(p -> {
                    g.getPlayers().remove(p);
                    entityManager.persist(g);
                });
            });
    }

    @Transactional
    public void resetAll(){
        //todo: has right ?
        notificationRepository.deleteAll();
        gameActionRepository.deleteAll();
        gameObjectRepository.deleteAll();
        accountRepository.deleteAll();
        playerRepository.deleteAll();
        personsRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @Transactional
    @SuppressWarnings(value = "unchecked")
    public void createGameObject(String gameName, Account account, Object gameObject){
        var game = gameRepository.findFirstByName(gameName);
        game.ifPresent(g -> {
            var player = playerRepository.findFirstByAccount(account);
            Map<String, Object> go = (Map<String, Object>) gameObject;
            if( player.isPresent() ) {
                Location loc;
                try {
                    Map<String, Object> location = (Map<String, Object>) go.getOrDefault("location", new HashMap<>());
                    loc = (Location) player.get().getCharacter().getLocation().clone();
                    loc.setLatitude(DoubleUtils.asDouble(location.getOrDefault("latitude", loc.getLatitude())));
                    loc.setLongitude(DoubleUtils.asDouble(location.getOrDefault("longitude", loc.getLongitude())));
                    loc.setAltitude(DoubleUtils.asDouble(location.getOrDefault("altitude", loc.getAltitude())));
                    loc.setId(null);
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
                Item finalObject = (Item) Item.builder()
                        .build();
                finalObject.setName((String) go.getOrDefault("name", "game object"));
                finalObject.setLocation(loc);
                finalObject.setBasePrice(DoubleUtils.asDouble(go.getOrDefault("basePrice", "0.1")));

                g.getGameObjects().add(finalObject);
                entityManager.persist(finalObject);
            }
        });


    }

    @Getter
    Map<Player, WebSocketSession> sessions = new HashMap<>();

    @Builder
    @Data
    static public class GameMessage {
        private List<Person> persons;
        private Map<Long, List<GameAction>> actions;
    }

    @Scheduled(fixedDelay = 10000L)
    @Transactional
    public void updateDB(){
        System.out.println("Updating players...");
        for(Player p : sessions.keySet()) {
            entityManager.merge(p);
        }
        System.out.println("Done.");

        System.out.println("Updating other entities...");
        iaService.updateDB();
        System.out.println("Done.");
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void decideAndAct() throws JsonProcessingException, CloneNotSupportedException {
        Mono<Map<Long, List<GameAction>>> actions = iaService.handleActions();
        actions.flatMap(acts->{
            GameMessage gm = GameMessage.builder().actions(acts).build();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String str = null;
            try {
                str = mapper.writeValueAsString(gm);
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException(e));
            }
            List<Mono<Void>> fluxAsList = new ArrayList<>();
            for(Player p: sessions.keySet()){
                var session = sessions.get(p);
                if( session.isOpen() ){
                    fluxAsList.add(session.send(Mono.just(session.textMessage(str))));
                }
            }
            return Flux.concat(fluxAsList).then().thenReturn(acts);
        }).subscribe();
    }
}

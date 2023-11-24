package fr.anonympins.game.service;

import fr.anonympins.game.model.GameObjectRepository;
import fr.anonympins.game.model.PersonRepository;
import fr.anonympins.game.model.PlayerRepository;
import fr.anonympins.game.model.Sense;
import fr.anonympins.game.model.entity.GameAction;
import fr.anonympins.game.model.entity.GameObject;
import fr.anonympins.game.model.entity.Person;
import fr.anonympins.game.model.entity.Player;
import fr.anonympins.game.utils.DoubleUtils;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

@Service
@Data
public class IAService {

    @Autowired
    private TransactionTemplate transactionTemplate;

    private List<Sense> senses;
    private List<Person> persons;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    GameObjectRepository gameObjectRepository;

    @PostConstruct
    public void setup(){
        senses = new ArrayList<>();

        Sense sense = new Sense();
        sense.setOutputs(List.of(
                GameAction.builder().type(GameAction.ActionType.CREATE_GAMEOBJECT).build(),
                GameAction.builder().type(GameAction.ActionType.ATTACK_PLAYER).build(),
                GameAction.builder().type(GameAction.ActionType.LEFT).build(),
                GameAction.builder().type(GameAction.ActionType.RIGHT).build(),
                GameAction.builder().type(GameAction.ActionType.TOP).build(),
                GameAction.builder().type(GameAction.ActionType.BOTTOM).build(),
                GameAction.builder().type(GameAction.ActionType.NOP).build()
        ));
        senses.add(sense);

        persons = personRepository.findAll();
    }

    void focusOnTarget(GameObject target){
        for(Sense s: senses){
            s.setTarget(target);
        }
    }

    public void learn(Player p, GameAction action){
        senses.get(0).learn(p, action, personRepository.findNearest(
                p.getCharacter().getLocation().getX(),
                p.getCharacter().getLocation().getY(),
                Pageable.ofSize(1)).get(0));
    }

    @Transactional
    public Mono<Void> handleAction(GameAction action) {
        return handleAction(action, action.getInitiator());
    }

    @Transactional
    public Mono<Void> handleAction(GameAction action, GameObject go){
        double dist = 15d;
        return Mono.fromCallable(() -> {
            if( action.getType().equals(GameAction.ActionType.LEFT)){
                go.getLocation().setX(go.getLocation().getX() - dist);
            } else if( action.getType().equals(GameAction.ActionType.RIGHT)){
                go.getLocation().setX(go.getLocation().getX() + dist);
            } else if( action.getType().equals(GameAction.ActionType.TOP)){
                go.getLocation().setY(go.getLocation().getY() - dist);
            } else if( action.getType().equals(GameAction.ActionType.BOTTOM)){
                go.getLocation().setY(go.getLocation().getY() + dist);
            }
            return go;
        }).delayElement(Duration.ofMillis(1500)).then();
    }

    @Transactional
    public Mono<Map<Long, List<GameAction>>> handleActions() throws CloneNotSupportedException {
        Map<Long, List<GameAction>> personActions = new HashMap<>();
        List<Mono<Void>> actionsToExecute = new ArrayList<>();
        int i = 0;
        Collections.shuffle(persons);
        for(Person go: persons) {
            if( i > 20 )
                break;
            var player = personRepository.findNearest(go.getLocation().getX().doubleValue(), go.getLocation().getY().doubleValue(), Pageable.ofSize(1)).get(0);
            var newActions = senses.get(0).actionToUse(go, player);

            // First Get the flux,

            for(GameAction action: newActions) {
                actionsToExecute.add(handleAction(action, go));
            }

            if( go.getId() != null ) {
                personActions.put(go.getId(), newActions);
            }
            ++i;
        }
        Flux.fromStream(actionsToExecute.stream())
                .publishOn(Schedulers.boundedElastic()).subscribe();
        return Mono.just(personActions);
    }

    public Player randomPlayer(){
        List<Player> players = playerRepository.findAll();
        return players.get(new Random().nextInt(players.size()));
    }

    public void updateDB(){
        for(Person p: persons){
            entityManager.merge(p);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void updateModel(){
        System.out.println("Updating senses model...");
        for(Sense sense: senses){
            sense.saveModel();
        }
        System.out.println("Done.");
    }
}

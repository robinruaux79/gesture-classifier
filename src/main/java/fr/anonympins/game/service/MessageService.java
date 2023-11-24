package fr.anonympins.game.service;

import fr.anonympins.game.model.entity.Account;
import fr.anonympins.game.model.entity.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class MessageService {

    @PersistenceContext
    EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Transactional
    public void send(Notification notification){
        entityManager.persist(notification);
        trigger("message."+notification.getCanal(), notification);
    }

    @Transactional
    public void send(Account to, Notification notification){
        if( to.getNotifications() == null ){
            to.setNotifications(new ArrayList<>());
        }
        to.getNotifications().add(notification);
        entityManager.persist(notification);
        trigger("message."+notification.getCanal(), notification);
    }

    @Transactional
    public void send(String canal, String message){
        var notif =
                Notification.builder()
                        .canal(canal)
                        .message(message)
                        .sentAt(LocalDateTime.now())
                        .build();
        entityManager.persist(notif);
        trigger("message."+canal, notif);
        logger.debug(notif.toString());
    }
    @Transactional
    public void send(String message){
        var notif =
                Notification.builder()
                        .canal("main")
                        .message(message)
                        .sentAt(LocalDateTime.now())
                        .build();
        entityManager.persist(notif);
        trigger("message.main", notif);
    }

    @Transactional
    public void send(Account account, String message){
        var notif = Notification.builder()
                .canal("accounts["+account.getUsername()+"]")
                .message(message)
                .sentAt(LocalDateTime.now())
                .build();
        entityManager.persist(notif);
        account.getNotifications().add(notif);
        trigger("message.accounts["+account.getUsername()+"]", notif);
    }

    Map<String, List<Function<Notification, Boolean>>> callbacks = new HashMap<>();
    public void on(String canal, Function<Notification, Boolean> callback){
        var cbs = callbacks.get(canal);
        if( cbs == null){
            cbs = new ArrayList<>();
        }
        cbs.add(callback);
        callbacks.put(canal, cbs);
    }

    public void trigger(String canal, Notification notif){
        var cbs = callbacks.get(canal);
        if( cbs != null ) {
            for (Function<Notification, Boolean> cb : cbs) {
                cb.apply(notif);
            }
        }
    }
}

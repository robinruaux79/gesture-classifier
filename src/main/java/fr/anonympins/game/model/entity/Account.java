package fr.anonympins.game.model.entity;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "username"})
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String hash;

    private String username;

    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval = true)
    private List<AccountProvider> accountProviders = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private List<Role> roles = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    public Boolean isAuthenticated(String user, String password){
        for(AccountProvider ap : accountProviders){
            if( ap.getToken().equals(password) ){
                return true;
            }
        }
        return false;
    }

    public Boolean hasRole(String role){
        for(Role r: roles){
            if( r.getName().equals(role) ){
                return true;
            }
        }
        return false;
    }
    public Boolean hasAuthority(String authority){
        for(Role r: roles){
            for(Authority a: r.getAuthorities()){
                if( a.getName().equals(authority) ) {
                    return true;
                }
            }
        }
        return false;
    }


}

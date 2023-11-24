package fr.anonympins.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String username;

    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval = true)
    private List<AccountProvider> accountProviders = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private List<Role> roles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
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

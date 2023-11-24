package fr.anonympins.game.service;

import fr.anonympins.game.model.entity.Account;
import fr.anonympins.game.model.AccountRepository;
import fr.anonympins.game.utils.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MessageService messageService;

    @Transactional
    public Optional<Account> getAccount(Long id){
        if( id == null ){
            return Optional.empty();
        }
        var a = accountRepository.findById(id);
        return a;
    }

    @Transactional
    public Optional<Account> getAccountByCredentials(String user, String pass) throws NoSuchAlgorithmException {
        var a = accountRepository.findAccountByCredentials(user, HashUtils.sha256(pass), Pageable.ofSize(1));
        if( a.isEmpty())
            return Optional.empty();
        return Optional.of(a.get(0));
    }
}

package fr.anonympins.gestures.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ApiCallService {

    @Autowired
    WebClient webClient;

    public <T> Mono<T> get(String url, ParameterizedTypeReference<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToMono(ptr);
    }
    public <T> Flux<T> getAll(String url, ParameterizedTypeReference<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToFlux(ptr);
    }
    public <T> Mono<T> get(String url, Class<T> ptr){
        System.out.println("GET " + url);
        return webClient.get()
                .uri(url)
                .retrieve().bodyToMono(ptr);
    }
    public <T> Flux<T> getAll(String url, Class<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToFlux(ptr);
    }

}

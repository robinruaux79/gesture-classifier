package fr.anonympins.game.service;

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

    @Autowired
    MessageService messageService;

    public <T> Mono<T> get(String url, ParameterizedTypeReference<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToMono(ptr).doFinally((d) -> {
                    messageService.send("api-call", "GET "+ url);
                });
    }
    public <T> Flux<T> getAll(String url, ParameterizedTypeReference<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToFlux(ptr).doFinally((d) -> {
                    messageService.send("api-call", "GET ALL "+ url);
                });
    }
    public <T> Mono<T> get(String url, Class<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToMono(ptr).doFinally((d) -> {
                    messageService.send("api-call", "GET "+ url);
                });
    }
    public <T> Flux<T> getAll(String url, Class<T> ptr){
        return webClient.get()
                .uri(url)
                .retrieve().bodyToFlux(ptr).doFinally((d) -> {
                    messageService.send("api-call", "GET ALL "+ url);
                });
    }

}

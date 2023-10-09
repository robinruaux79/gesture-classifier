package fr.anonympins.gestures.repo;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class MainRestController {

    @GetMapping(value = "/", produces = "text/html;charset=UTF-8")
    public Mono<Rendering> home(ServerHttpRequest request, WebSession session){
        return Mono.just(Rendering.view("index.html").build());
    }
}

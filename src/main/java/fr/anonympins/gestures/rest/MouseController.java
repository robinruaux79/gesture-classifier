package fr.anonympins.gestures.rest;

import fr.anonympins.gestures.model.MouseGestureRecognizer;
import fr.anonympins.gestures.service.MouseGestureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
public class MouseController {

    private final MouseGestureService mouseGestureService;

    MouseController(MouseGestureService mouseGestureService){
        this.mouseGestureService = mouseGestureService;
    }

    @PostMapping("/api/mouseGesture")
    public Mono<Void> sendMouseGesture(
            @RequestBody MouseGestureRecognizer.MouseGesture gesture){
        return mouseGestureService.sendMouseGesture(gesture);
    }

    @PostMapping("/api/detect")
    public Mono<MouseGestureRecognizer.ShortcutEnum> detectGesture(
            @RequestBody MouseGestureRecognizer.MouseGesture gesture){
        return mouseGestureService.detectGesture(gesture);
    }

    @GetMapping("/api/applyModel")
    public Mono<Void> applyModel() {
        mouseGestureService.applyModel();
        return Mono.empty();
    }

    @Scheduled(fixedDelay = 20000)
    public void saveModel() throws IOException {
        mouseGestureService.saveTrainingData();
        mouseGestureService.saveModel();
    }
}

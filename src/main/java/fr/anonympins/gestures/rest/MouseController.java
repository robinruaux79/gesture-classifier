package fr.anonympins.gestures.rest;

import fr.anonympins.gestures.model.MouseGestureRecognizer;
import fr.anonympins.gestures.service.MouseGestureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

    @Scheduled(fixedDelay = 20000)
    public void saveModel() throws IOException {
        mouseGestureService.saveModel();
    }
}

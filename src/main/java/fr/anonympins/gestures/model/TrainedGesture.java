package fr.anonympins.gestures.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainedGesture {
    MouseGestureRecognizer.MouseGesture gesture;
    String shortcut;
}

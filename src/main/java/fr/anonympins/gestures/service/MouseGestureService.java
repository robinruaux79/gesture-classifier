package fr.anonympins.gestures.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.anonympins.gestures.model.MouseGestureRecognizer;
import fr.anonympins.gestures.model.NeuralNetwork;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MouseGestureService {

    public static String DATA_MODEL_PATH = "data/modelGestures.dat";

    MouseGestureRecognizer recognizer;

    MouseGestureService() throws FileNotFoundException {
        this.recognizer = new MouseGestureRecognizer();
        loadModel();
    }

    List<MouseGestureRecognizer.MouseGesture> gestures = new ArrayList<>();
    public Mono<Void> sendMouseGesture(MouseGestureRecognizer.MouseGesture gesture){
        gestures.add(gesture);
        recognizer.applyMovementModel(gestures);
        return Mono.empty();
    }

    public Mono<MouseGestureRecognizer.ShortcutEnum> detectGesture(MouseGestureRecognizer.MouseGesture gesture){
        return Mono.just(recognizer.detectGesture(gesture));
    }

    public void saveModel() throws IOException {
        String str = recognizer.getModel();
        File file = new File(DATA_MODEL_PATH);
        FileWriter fw;
        if (file.exists())
        {
            System.out.println(file.getAbsolutePath());
            fw = new FileWriter(file);
        }
        else
        {
            file.createNewFile();
            fw = new FileWriter(file);
        }

        BufferedWriter writer = new BufferedWriter(fw);
        writer.write(str);
        writer.close();
    }

    public void loadModel() throws FileNotFoundException {
        InputStream inputStream = null;
        try {
            File file = new File(DATA_MODEL_PATH);
            inputStream = new FileInputStream(file);

            String text = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            ObjectMapper mapper = new ObjectMapper();
            NeuralNetwork neuralNetwork = mapper.readValue(text, NeuralNetwork.class);
            //...
            recognizer.setModel(neuralNetwork);

        } catch (FileNotFoundException e) {

        } catch (JsonProcessingException e) {

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package fr.anonympins.gestures.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.anonympins.gestures.model.*;
import lombok.Builder;
import org.springframework.core.ParameterizedTypeReference;
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
    public static String DATA_TRAINING_PATH = "data/trainingData.dat";

    MouseGestureRecognizer recognizer;

    List<TrainedGesture> trainedGestures;

    ObjectMapper mapper = new ObjectMapper();

    MouseGestureService() throws JsonProcessingException {
        this.recognizer = new MouseGestureRecognizer();
        loadTrainingData();
        loadModel();
    }


    public void loadTrainingData() throws JsonProcessingException {
        String text = getTextFromFile(DATA_TRAINING_PATH);
        if( text != null ) {
            trainedGestures = mapper.readValue(text, new TypeReference<List<TrainedGesture>>() {
            });
        }else{
            this.trainedGestures = new ArrayList<>();
        }
    }

    public void loadModel() throws JsonProcessingException {
        String text = getTextFromFile(DATA_MODEL_PATH);
        if (text != null ) {
            NeuralNetwork neuralNetwork = mapper.readValue(text, NeuralNetwork.class);
            //...
            recognizer.setModel(neuralNetwork);
        }else{

        }
    }

    public Mono<Void> sendMouseGesture(MouseGestureRecognizer.MouseGesture gesture){
        var trainedGesture = TrainedGesture.builder().gesture(gesture).shortcut(MouseGestureRecognizer.ShortcutEnum.valueOf(gesture.shortcut)).build();
        trainedGestures.add(trainedGesture);
        return Mono.empty();
    }

    public Mono<MouseGestureRecognizer.ShortcutEnum> detectGesture(MouseGestureRecognizer.MouseGesture gesture){
        return Mono.just(recognizer.detectGesture(gesture));
    }

    public void saveTrainingData() throws IOException {
        writeToFile(DATA_TRAINING_PATH, mapper.writeValueAsString(trainedGestures));
    }

    public void applyModel(){
        var gestures = trainedGestures
                .stream()
                .map(TrainedGesture::getGesture)
                .collect(Collectors.toList());
        recognizer.applyMovementModel(gestures);
    }

    public void saveModel() throws IOException {
        String str = recognizer.getModel();
        writeToFile(DATA_MODEL_PATH, str);
    }

    private void writeToFile(String path, String content) throws IOException {
        File file = new File(path);
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
        writer.write(content);
        writer.close();
    }
    private String getTextFromFile(String path) {
        InputStream inputStream = null;
        String text = null;
        try {
            File file = new File(path);
            inputStream = new FileInputStream(file);
            text = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

        } catch (FileNotFoundException e) {

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return text;
    }

}

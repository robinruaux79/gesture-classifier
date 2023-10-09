package fr.anonympins.gestures.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class MouseGestureRecognizer {

    @Data
    @NoArgsConstructor
    static public class Vector2 {
        double x;
        double y;
    }

    @Data
    @NoArgsConstructor
    static public class MouseGesture {
        public List<Vector2> positions;
        public String shortcut = "UNKNOW";
    }

    private NeuralNetwork neuralNetwork;

    @Getter
    private List<String> classes;

    public MouseGestureRecognizer(List<String> classes){
        this.classes = classes;
        resetModel();
    }

    public void resetModel(){
        neuralNetwork = new NeuralNetwork(56, 37, classes.size());
    }

    public void setModel(NeuralNetwork network){
        neuralNetwork = network;
    }

    public void applyMovementModel(List<MouseGesture> gestures){

        System.out.println("Initializing model...");
        resetModel();
        System.out.println("Done.");
        System.out.println("Applying movement model...");


        System.out.println("Creating data from gestures list... (" + gestures.size() + ")");
        double[][] data = new double[gestures.size()][gestures.get(0).positions.size()*2];
        double[][] answer = new double[gestures.size()][classes.size()];

        for(int g = 0; g < gestures.size(); ++g) {
            MouseGesture gesture = gestures.get(g);
            for (int i = 0; i < gesture.positions.size(); i++) {
                data[g][i * 2] = gesture.positions.get(i).x;
                data[g][i * 2 + 1] = gesture.positions.get(i).y;
            }

            List<Double> answers = new ArrayList<>();
            for(String c : classes){
//            for (ShortcutEnum v : ShortcutEnum.values()) {
                if (c.equals(gesture.shortcut)) {
                    answers.add(1d);
                } else {
                    answers.add(0d);
                }
            }
            answer[g] = answers.stream().mapToDouble(Double::doubleValue).toArray();
        }
        System.out.println("Done.");

        int epochs = 7000;
        System.out.println("Fitting... ("+epochs+" epochs)");
        neuralNetwork.fit(data, answer, epochs);
        System.out.println("Done.");
    }

    public String detectGesture(MouseGesture gesture) {

        System.out.println("Detecting gesture...");
        System.out.println("Loading data...");
        double[] data = new double[gesture.positions.size()*2];
        for (int i = 0; i < gesture.positions.size(); i++) {
            data[i * 2] = gesture.positions.get(i).x;
            data[i * 2 + 1] = gesture.positions.get(i).y;
        }
        System.out.println("Done.");

        System.out.println("Predicting data...");
        List<Double> prediction = neuralNetwork.predict(data);
        String finalShortcut = "";
        double bestProb = 0;
        int i = 0;
        String[] c = classes.toArray(new String[classes.size()]);
        for(Double p : prediction){
            var shortcut = c[i];
            if( p > bestProb ){
                finalShortcut = shortcut;
                bestProb = p;
            }
            System.out.println("Prediction " + p + " for " + shortcut);
            ++i;
        }
        System.out.println("Done.");

        return finalShortcut;
    }

    public String getModel(){
        try {
            return neuralNetwork.serialize();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}

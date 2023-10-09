package fr.anonympins.gestures.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public enum ShortcutEnum {
        SHORTCUT_ECLAIR,
        SHORTCUT_MARCHE,

        SHORTCUT_TABLE,
        SHORTCUT_BALL,

        EMPTY;

    };

    public static ShortcutEnum getShortcutByProb(Double prob){
        for(ShortcutEnum e : ShortcutEnum.values()) {
            if( ((float)e.ordinal()+0.5/ShortcutEnum.values().length) > prob ){
                return e;
            }
        }
        return ShortcutEnum.EMPTY;
    }

    public static ShortcutEnum getShortcutByOrdinal(Integer ordinal) {
        for(ShortcutEnum e : ShortcutEnum.values()) {
            if(e.ordinal() == ordinal) return e;
        }
        return ShortcutEnum.EMPTY;
    }

    public static ShortcutEnum getShortcutByName(String name) {
        for(ShortcutEnum e : ShortcutEnum.values()) {
            if(e.name().equals(name)) return e;
        }
        return ShortcutEnum.EMPTY;
    }

    private NeuralNetwork neuralNetwork;

    public MouseGestureRecognizer(){
        neuralNetwork = new NeuralNetwork(56, 8, ShortcutEnum.values().length);
    }

    public void setModel(NeuralNetwork network){
        neuralNetwork = network;
    }

    public void applyMovementModel(List<MouseGesture> gestures){

        System.out.println("Apply movement model");


        System.out.println("Creating data from gestures list (" + gestures.size() + ")");
        double[][] data = new double[gestures.size()][gestures.get(0).positions.size()*2];
        double[][] answer = new double[gestures.size()][ShortcutEnum.values().length];

        for(int g = 0; g < gestures.size(); ++g) {
            MouseGesture gesture = gestures.get(g);
            for (int i = 0; i < gesture.positions.size(); i++) {
                data[g][i * 2] = gesture.positions.get(i).x;
                data[g][i * 2 + 1] = gesture.positions.get(i).y;
            }

            List<Double> answers = new ArrayList<>();
            for (ShortcutEnum v : ShortcutEnum.values()) {
                if (v == getShortcutByName(gesture.shortcut)) {
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

    public ShortcutEnum detectGesture(MouseGesture gesture) {

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
        ShortcutEnum finalShortcut = ShortcutEnum.EMPTY;
        double bestProb = 0;
        int i = 0;
        for(Double p : prediction){
            var shortcut = getShortcutByOrdinal(i);
            if( shortcut == ShortcutEnum.EMPTY)
                continue;
            if( p > bestProb ){
                finalShortcut = shortcut;
                bestProb = p;
            }
            System.out.println("Prediction " + p + " for " + shortcut.name());
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

package fr.anonympins.game.model;


import fr.anonympins.game.model.entity.GameAction;
import fr.anonympins.game.utils.FileUtils;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class Memory {

    private List<List<Double>> facts;
    private List<List<Double>> qualifiers;

    private Integer epochs = 500;

    private NeuralNetwork neuralNetwork;

    public Memory() {
        // load from file the model
        // save to file the model
        // push data to it (recreates the model)
    }

    public Boolean pushData(List<Double> inputs, List<Double> outputs){
        if(facts == null){
            facts = new ArrayList<>();
        }
        facts.add(inputs);
        if( qualifiers == null){
            qualifiers = new ArrayList<>();
        }
        qualifiers.add(outputs);
        generateModel(outputs.size());
        return true;
    }

    public List<List<Double>> fromDoubleArrs(double[][] arr){
        List<List<Double>> dback = new ArrayList<>();
        for(double[] d : arr) {
            List<Double> a = new ArrayList<>();
            for (double d1 : d) {
                a.add(d1);
            }
            dback.add(a);
        }
        return dback;
    }

    public Boolean loadFromFile(String filepath) {
        try {
            FileInputStream fis = new FileInputStream(filepath);
            ObjectInputStream in = new ObjectInputStream(fis);
            facts = fromDoubleArrs((double[][]) in.readObject());
            qualifiers = fromDoubleArrs((double[][]) in.readObject());
            epochs = in.readInt();
            in.close();
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    public Boolean saveToFile(String filename){
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(facts);
            out.writeObject(qualifiers);
            out.writeInt(epochs);
            out.flush();
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    double[][] doubleToArr(List<List<Double>> doubles){
        double[][] dback = new double[doubles.size()][];
        for(int i = 0; i < doubles.size(); ++i){
            List<Double> ds = doubles.get(i);
            dback[i] = new double[ds.size()];
            for(int j = 0; j < ds.size(); ++j){
                dback[i][j] = ds.get(j);
            }
        }
        return dback;
    }

    public void generateModel(Integer outputSize) {
        neuralNetwork = new NeuralNetwork(9, Math.min(4, 9 * 2 / 3), outputSize);
        if( facts != null && qualifiers != null )
            neuralNetwork.fit(doubleToArr(facts), doubleToArr(qualifiers), epochs);
    }

    public List<Double> getQualifiers(double[] inputs) {
        if( neuralNetwork == null ){
            return new ArrayList<>();
        }
        return neuralNetwork.predict(inputs);
    }
}

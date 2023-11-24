package fr.anonympins.game.model;

import fr.anonympins.game.model.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.anonympins.game.model.entity.GameAction.ActionType.NOP;
import static fr.anonympins.game.utils.MapUtils.sortByValue;

@Data
public class Sense {

    /**
     * Entrainement de l'action NOP selon ce taux d'activité
     */
    double senseActivityRate = 0.15; // 0-1
    List<GameAction> outputs;

    GameObject target;

    Memory shortTermMemory;

    List<GameAction> pastGameActions = new ArrayList<>();

    final String path = "data/sense.dat";
    public Sense(){
        shortTermMemory = new Memory();
    }

    public void loadMemory(Integer output){
        if (!shortTermMemory.loadFromFile(path)){
            shortTermMemory.generateModel(output);
            shortTermMemory.saveToFile(path);
        }
    }

    public void saveModel(){
        shortTermMemory.generateModel(getOutputs().size());
        shortTermMemory.saveToFile(path);
    }

    public void     learn(Player p, GameAction action, GameObject target){
        List<Double> answers = new ArrayList<>();
        List<Double> playerData = new ArrayList<>();

        playerData.add(p.getCharacter().getLocation().getX());
        playerData.add(p.getCharacter().getLocation().getY());
        playerData.add(p.getCharacter().getLocation().getZ());
        playerData.add(0d);
        playerData.add(0d);
        playerData.add(Math.abs(target.getLocation().getX() - p.getCharacter().getLocation().getX()));
        playerData.add(Math.abs(target.getLocation().getY() - p.getCharacter().getLocation().getY()));
        playerData.add(Math.abs(target.getLocation().getZ() - p.getCharacter().getLocation().getZ()));
        playerData.add(0d);//(double) (new Date().getTime() - Timestamp.valueOf(p.getCharacter().getBirthdate()).getTime()));
        int a = 0;
        for(GameAction o: outputs){
            if( o.getType() == action.getType() ){
                answers.add(1d);
            }else{
                answers.add(0.0d);
            }
        }
        shortTermMemory.pushData(playerData, answers);
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public class ClassKey {
        public Double d;
        public Integer act;
    }

    public List<GameAction> actionToUse(GameObject go, Person person) throws CloneNotSupportedException {
        double[] ds = new double[9];
        //x position dans l'espace relative à l'oeil
        ds[0] = go.getLocation().getX();
        //y position dans l'espace relative à l'oeil
        ds[1] = go.getLocation().getY();
        //z position dans l'espace relative à l'oeil  |._
        ds[2] = go.getLocation().getZ();
        // classe de l'objet
        ds[3] = go.getDoubleClass();
        // classe de l'objet player
        ds[4] = person.getDoubleClass();
        // classe de l'objet player
        ds[5] = Math.abs(person.getLocation().getX() - go.getLocation().getX());
        ds[6] = Math.abs(person.getLocation().getY() - go.getLocation().getY());
        ds[7] = Math.abs(person.getLocation().getZ() - go.getLocation().getZ());
        // temps relatif (expérience)
        ds[8] = new Date().getTime() - Timestamp.valueOf(person.getBirthdate()).getTime();

        var nop = GameAction.builder().type(NOP).build();
        if( pastGameActions.size() == 5 ) {
            ds[5] = Optional.ofNullable(pastGameActions.get(0)).orElse(nop).getDoubleClass();
            ds[6] = Optional.ofNullable(pastGameActions.get(1)).orElse(nop).getDoubleClass();
            ds[7] = Optional.ofNullable(pastGameActions.get(2)).orElse(nop).getDoubleClass();
            ds[8] = Optional.ofNullable(pastGameActions.get(3)).orElse(nop).getDoubleClass();
            ds[9] = Optional.ofNullable(pastGameActions.get(4)).orElse(nop).getDoubleClass();
        }

        List<Double> qualifiers = shortTermMemory.getQualifiers(ds);
        // get prob
        List<GameAction> actions = new ArrayList<>();
        Random r = new Random();
        boolean br = true;
        int i = 0;
        Double bestProb = Double.MIN_VALUE;
        GameAction action = null;
        List<ClassKey> classes = new ArrayList<>();
        for(Double q: qualifiers) {
            classes.add(
                    ClassKey.builder()
                            .act(i)
                            .d(q).build());
            i++;
        }
        classes.sort((c1,c2)->{
            return c2.getD().compareTo(c1.getD());
        });
        i = 0;
        for(ClassKey ck : classes){
            var prob = ck.getD();
            var d = r.nextDouble(0, 1);
            /*if( prob > bestProb ) {
                bestProb = prob;
                action = (GameAction) outputs.get(i).clone();
                break;
            }*/
            if( d < prob ){
                var inst = outputs.get(ck.getAct());
                inst.setInitiator(go);
                inst.setParameters(new HashMap<>());
//                inst.getParameters().put("ratio", 5D);
                actions.add(inst);
                break;
            }
            ++i;
        }
        //if( action != null )
        //actions.add(action);
        return actions;
    }
}

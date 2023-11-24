package fr.anonympins.game.utils;

public class DoubleUtils {
    static public Double asDouble(Object o) {
        Double val = null;
        if (o instanceof Number) {
            val = ((Number) o).doubleValue();
        }
        return val;
    }
}

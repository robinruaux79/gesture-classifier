package fr.anonympins.gestures.model;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class MatrixDeserializer extends StdDeserializer<Matrix> {
    public MatrixDeserializer() {
        this(null);
    }

    @Override
    public Matrix deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        int rows = (Integer) node.get("rows").numberValue();
        int cols = (Integer) node.get("cols").numberValue();

        double[][] finalData = new double[rows][cols];
        for( int i = 0; i < rows; ++i){
            var arr = node.withArray("data");
            var els = arr.elements();
            double[] cd = new double[cols];
            int c = 0;
            while (els.hasNext()) {
                cd[c] = els.next().asDouble();
                ++c;
            }
            finalData[i] = cd;
        }

        Matrix m = new Matrix(rows, cols);
        m.data = finalData;
        return m;
    }

    protected MatrixDeserializer(Class<Matrix> t) {
        super(t);
    }

}

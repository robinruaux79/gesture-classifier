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
        var arr = node.withArray("data");
        var els = arr.elements();
        int c = 0;
        while (els.hasNext()) {
            var d = els.next().asDouble();
            finalData[c/rows][c%cols] = d;
            ++c;
        }

        Matrix m = new Matrix(rows, cols);
        m.data = finalData;
        return m;
    }

    protected MatrixDeserializer(Class<Matrix> t) {
        super(t);
    }

}

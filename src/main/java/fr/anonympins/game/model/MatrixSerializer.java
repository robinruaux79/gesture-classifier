package fr.anonympins.gestures.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MatrixSerializer extends StdSerializer<Matrix> {
    public MatrixSerializer() {
        this(null);
    }

    protected MatrixSerializer(Class<Matrix> t) {
        super(t);
    }

    @Override
    public void serialize(Matrix matrix, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("rows", matrix.rows);
        jsonGenerator.writeNumberField("cols", matrix.cols);

        jsonGenerator.writeArrayFieldStart("data");
        for(int y = 0; y < matrix.rows; ++y) {
            //jsonGenerator.writeString("test");
            jsonGenerator.writeArray(matrix.data[y], 0, matrix.cols);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();

    }
}

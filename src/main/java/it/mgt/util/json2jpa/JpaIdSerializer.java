package it.mgt.util.json2jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.mgt.util.jpa.JpaUtils;

import java.io.IOException;
import java.util.Collection;

public class JpaIdSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof Collection)
            serializeEntityCollection((Collection<?>) o, jsonGenerator, serializerProvider);
        else
            serializeEntity(o, jsonGenerator, serializerProvider);
    }

    protected void serializeEntityCollection(Collection<?> collection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        for (Object e : collection) {
            Object id = JpaUtils.getId(e);
            jsonGenerator.writeObject(id);
        }

        jsonGenerator.writeEndArray();
    }

    protected void serializeEntity(Object entity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Object id = JpaUtils.getId(entity);
        jsonGenerator.writeObject(id);
    }

}

package it.mgt.json2jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

public class JpaIdSerializer extends JsonSerializer<Object> {

    @PersistenceContext
    EntityManager em;

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof Collection)
            serializeEntityCollection((Collection<?>) o, jsonGenerator, serializerProvider);
        else
            serializeEntity(o, jsonGenerator, serializerProvider);
    }

    protected void serializeEntityCollection(Collection<?> collection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        try {
            jsonGenerator.writeStartArray();

            for (Object e : collection)
                jsonGenerator.writeObject(e);

            jsonGenerator.writeEndArray();
        } catch (IOException ignored) { }
    }

    protected void serializeEntity(Object entity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        Field idField = JpaUtils.getIdField(entity.getClass());
        idField.setAccessible(true);

        try {
            Object id = idField.get(entity);
            jsonGenerator.writeObject(id);
        } catch (IllegalAccessException e) {
            try {
                jsonGenerator.writeNull();
            } catch (IOException ignored) { }
        } catch (IOException ignored) { }
    }

}

package it.mgt.util.json2jpa;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import it.mgt.util.jpa.JpaUtils;
import it.mgt.util.jpa.ParamHint;
import it.mgt.util.jpa.ParamHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class JpaDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final static Logger LOGGER = LoggerFactory.getLogger(JpaDeserializer.class);

    @PersistenceContext
    private EntityManager em;
    
    private Class<?> type;
    private boolean single;
    private JpaDeserializerQuery ann;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext dc, BeanProperty bp) throws JsonMappingException {    
        ann = bp.getAnnotation(JpaDeserializerQuery.class);
        if (List.class.isAssignableFrom(bp.getType().getRawClass())) {
            type = bp.getType().getContentType().getRawClass();
            single = false;
        }
        else if (List.class.isAssignableFrom(bp.getType().getRawClass())) {
            LOGGER.error(bp.getType().getRawClass().getName() + " is not a supported collection");
            throw new IllegalArgumentException(bp.getType().getRawClass().getName() + " is not a supported collection");
        }
        else {
            type = bp.getType().getRawClass();
            single = true;
        }
        
        return this;
    }
    
    @Override
    public Object deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        Object result;
        
        try {
            if (ann != null) {
                result = findByQuery(parser);
            }
            else {
                result = findByPrimaryKey(parser);
            }
        }
        catch (JpaDeserializerException e) {
            throw e;
        }
        catch(Exception e) {
            LOGGER.warn("Deserialization failed", e);
            throw new JpaDeserializerException(e);
        }
        
        if (single && result == null) {
            LOGGER.warn("Single non-null result was expected, but null value was resolved");
            throw new JpaDeserializerException("Could not deserialize reference entity");
        }
        
        return result;
    }
    
    private Object findByQuery(JsonParser parser) throws IOException {
        Object result;
        
        Map<String, Object> params;

        if (parser.getCurrentToken().equals(JsonToken.START_OBJECT)) {
            params = loadObjectParam(parser);
        }
        else {
            LOGGER.warn("Deserialization by query expects an object containing query parameters");
            throw new JpaDeserializerException("Unable to load params");
        }

        TypedQuery<?> query = em.createNamedQuery(ann.value(), type);
        params.forEach(query::setParameter);

        List<?> resultList = query.getResultList();

        if (single) {
            result = resultList.stream()
                    .findFirst()
                    .orElse(null);
        }
        else {
            result = resultList;
        }
            
        return result;
    }
    
    private Map<String, Object> loadObjectParam(JsonParser parser) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Map<String, Class<?>> hints = new HashMap<>();
        ParamHints paramHints = type.getAnnotation(ParamHints.class);
        if (paramHints != null) {
            ParamHint[] hint = paramHints.value();
            for (ParamHint ph : hint)
                hints.put(ph.value(), ph.type());
        }
        
        for (JsonToken jsonToken = parser.nextToken();  jsonToken != null; jsonToken = parser.nextToken()) {                    
            Class<?> paramType = hints.get(parser.getCurrentName());
            if (paramType == null)
                paramType = String.class;

            if (jsonToken.isScalarValue()) {
                params.put(parser.getCurrentName(), parser.readValueAs(paramType));
            }
            else if (jsonToken.equals(JsonToken.START_ARRAY)) {
                jsonToken = parser.nextToken();
                if (jsonToken.equals(JsonToken.END_ARRAY)) {
                    continue;
                }

                List<Object> list = new ArrayList<>();
                Iterator<?> iterator = parser.readValuesAs(paramType);
                iterator.forEachRemaining(list::add);
                params.put(parser.getCurrentName(), list);
            }
            else if (jsonToken.equals(JsonToken.START_OBJECT)) {
                LOGGER.debug("Skipping parameter children object");
                parser.skipChildren();
            }
            else if (jsonToken.equals(JsonToken.END_OBJECT)) {
                break;
            }
        }
        
        return params;
    }
    
    private Object findByPrimaryKey(JsonParser parser) throws IOException {
        Object id = parser.readValueAs(JpaUtils.getIdClass(type));
        return em.find(type, id);
    }

}

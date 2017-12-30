package it.mgt.jpa.json2jpa;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class JpaDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

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
            throw new JpaDeserializerException(e);
        }
        
        if (single && result == null)
            throw new JpaDeserializerException("Could not deserialize reference entity");
        
        return result;
    }
    
    private Object findByQuery(JsonParser parser) throws IOException {
        Object result;
        
        Map<String, Object> params;

        if (parser.getCurrentToken().isScalarValue()) {
            params = loadSingleParam(parser);
        }
        else if (parser.getCurrentToken().equals(JsonToken.START_ARRAY)) {
            params = loadArrayParam(parser);
        }
        else if (parser.getCurrentToken().equals(JsonToken.START_OBJECT)) {
            params = loadObjectParam(parser);
        }
        else {
            throw new JpaDeserializerException("Unable to load params");
        }

        TypedQuery<?> query = em.createNamedQuery(ann.query(), type);
        params.entrySet().forEach((e) -> {
            query.setParameter(e.getKey(), e.getValue());
        });

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
    
    private Map<String, Object> loadSingleParam(JsonParser parser) throws IOException {
        Map<String, Object> params = new HashMap<>();
        
        JpaDeserializerQueryParam hint = ann.params()[0];
        params.put(hint.name(), parser.readValueAs(hint.type()));
        
        return params;
    }
    
    private Map<String, Object> loadArrayParam(JsonParser parser) throws IOException {
        Map<String, Object> params = new HashMap<>();
        
        JpaDeserializerQueryParam hint = ann.params()[0];

        JsonToken jsonToken = parser.nextToken();
        if (!jsonToken.equals(JsonToken.END_ARRAY)) {
            List list = new ArrayList();
            Iterator iter = parser.readValuesAs(hint.type());
            iter.forEachRemaining(list::add);
            params.put(parser.getCurrentName(), list);
        }
        
        return params;
    }
    
    private Map<String, Object> loadObjectParam(JsonParser parser) throws IOException {
        Map<String, Object> params = new HashMap<>();
        
        Map<String, Class<?>> paramHints = new HashMap<>();
        for (JpaDeserializerQueryParam p : ann.params()) {
            paramHints.put(p.name(), p.type());
        }
        
        for (JsonToken jsonToken = parser.nextToken();  jsonToken != null; jsonToken = parser.nextToken()) {                    
            Class<?> paramType = paramHints.get(parser.getCurrentName());                    
            if (jsonToken.isScalarValue()) {
                if (paramType == null) {
                    continue;
                }

                params.put(parser.getCurrentName(), parser.readValueAs(paramType));
            }
            else if (jsonToken.equals(JsonToken.START_ARRAY)) {
                if (paramType == null) {
                    parser.skipChildren();
                    continue;
                }

                jsonToken = parser.nextToken();
                if (jsonToken.equals(JsonToken.END_ARRAY)) {
                    continue;
                }

                List list = new ArrayList();
                Iterator iter = parser.readValuesAs(paramType);
                iter.forEachRemaining(list::add);
                params.put(parser.getCurrentName(), list);
            }
            else if (jsonToken.equals(JsonToken.START_OBJECT)) {
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

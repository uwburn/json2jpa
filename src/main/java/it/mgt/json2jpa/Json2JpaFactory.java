package it.mgt.json2jpa;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class Json2JpaFactory {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ObjectMapper objectMapper;

    public Json2JpaFactory() {
    }

    public Json2JpaFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Json2Jpa build() {
        return new Json2Jpa(em, objectMapper);
    }

}

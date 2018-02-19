package it.mgt.util.json2jpa;

import com.fasterxml.jackson.databind.JsonNode;
import it.mgt.util.jpa.JpaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

class Json2JpaEntity {

    private final static Logger logger = LoggerFactory.getLogger(Json2JpaEntity.class);

    final Json2Jpa j2j;
    final Class<?> clazz;
    BeanInfo beanInfo;
    private JpaAccessType accessType;
    Json2JpaProperty idProperty;
    final Map<String, Json2JpaProperty> properties;
    final boolean concrete;

    Json2JpaEntity(Class<?> clazz, JsonNode jsonNode, Json2Jpa j2j) {
        this.j2j = j2j;
        Class<?> concreteType = this.j2j.getConcreteType(clazz, jsonNode);
        if (concreteType != null) {
            concrete = true;
            this.clazz = concreteType;
        }
        else {
            concrete = false;
            this.clazz = clazz;
        }

        Entity entityAnnotation = JpaUtils.getAnnotation(clazz, Entity.class);
        if (entityAnnotation == null)
            throw new Json2JpaException(clazz.getName() + "is not a JPA entity");

        try {
            this.beanInfo = Introspector.getBeanInfo(this.clazz);
        }
        catch (Exception ignored) { }

        this.accessType = this.getAccessType();

        this.properties = this.loadProperties();
        this.idProperty = this.findIdProperty();
    }

    private JpaAccessType getAccessType() {
        if (beanInfo != null)
            for (PropertyDescriptor pd : this.beanInfo.getPropertyDescriptors())
                if (pd.getReadMethod().getAnnotation(Id.class) != null)
                    return JpaAccessType.PROPERTY;

        for (Field f : JpaUtils.getFields(this.clazz))
            if (f.getAnnotation(Id.class) != null)
                return JpaAccessType.FIELD;

        throw new Json2JpaException("Id annotation not found");
    }

    private Map<String, Json2JpaProperty> loadProperties() {
        switch (this.accessType) {
            case PROPERTY:
                return loadPropertiesFromAccessors();
            case FIELD:
                return loadPropertiesFromFields();
        }

        throw new Json2JpaException("Unable to load properties");
    }

    private Map<String, Json2JpaProperty> loadPropertiesFromFields() {
        Map<String, Json2JpaProperty> properties = new HashMap<>();

        for (Field f : JpaUtils.getFields(this.clazz))
            properties.put(f.getName(), new Json2JpaProperty(f, this));

        return properties;
    }

    private Map<String, Json2JpaProperty> loadPropertiesFromAccessors() {
        Map<String, Json2JpaProperty> properties;

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(this.clazz);
            properties = Arrays.stream(beanInfo.getPropertyDescriptors())
                    .filter(pd -> pd.getReadMethod() != null)
                    .filter(pd -> pd.getWriteMethod() != null)
                    .collect(Collectors.toMap(FeatureDescriptor::getName, pd -> new Json2JpaProperty(pd, this)));
        }
        catch (Exception e) {
            throw new Json2JpaException("Properties loading failed", e);
        }

        return properties;
    }

    private Json2JpaProperty findIdProperty() {
        for (Map.Entry<String, Json2JpaProperty> e : this.properties.entrySet())
            if (e.getValue().id)
                return e.getValue();

        throw new Json2JpaException("Id property not found");
    }

    Object getId(Object jpaObject) {
        return idProperty.get(jpaObject);
    }

    @SuppressWarnings("unchecked")
    void merge(Object jpaObject, JsonNode json) {
        if (logger.isTraceEnabled())
            logger.trace("Merging " + this);

        if (!this.clazz.isInstance(jpaObject))
            throw new Json2JpaException("Object is not of the expected class");

        if (!this.concrete)
            throw new Json2JpaException("Cannot merge a non concrete class");

        if (json != null) {
            Iterator<Map.Entry<String, JsonNode>> jsonIterator = json.fields();
            while (jsonIterator.hasNext()) {
                Map.Entry<String, JsonNode> jsonField = jsonIterator.next();

                Json2JpaProperty property = this.properties.get(jsonField.getKey());
                if (property == null)
                    continue;

                if (this.j2j.pushPathIfAllowed(property.name)) {
                    if (this.j2j.matchesViews(property.views))
                        if (!property.ignore || this.j2j.discardIgnore)
                            property.merge(jpaObject, jsonField.getValue());

                    this.j2j.popPath();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Json2JpaEntity{" + clazz.getName() + "}";
    }
}

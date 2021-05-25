package it.mgt.util.json2jpa;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class Json2JpaEntity {

    private final static Logger logger = LoggerFactory.getLogger(Json2JpaEntity.class);

    final Json2Jpa j2j;
    final Class<?> clazz;
    BeanInfo beanInfo;
    private JpaAccessType accessType;
    Json2JpaProperty idProperty;
    final Map<String, Json2JpaProperty> properties;
    final boolean concrete;
    private JsonPropertyOrder orderAnnotation;

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

        orderAnnotation = JpaUtils.getAnnotation(clazz, JsonPropertyOrder.class);

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

    private static int indexOf(String[] arr, String str) {
        for (int i = 0; i < arr.length; i++) {
            if(arr[i].equals(str)){
                return i;
            }
        }
        return -1;
    }

    private Stream<Json2JpaFieldProperty> getSortedFiledProperties(JsonNode json) {
        Iterable<Map.Entry<String, JsonNode>> iterable = json::fields;

        return StreamSupport.stream(iterable.spliterator(), false)
                .map(e -> {
                    Json2JpaProperty property = this.properties.get(e.getKey());
                    if (property == null)
                        return null;

                    int index = -1;
                    if (this.orderAnnotation != null) {
                        index = indexOf(this.orderAnnotation.value(), e.getKey());
                    }

                    return new Json2JpaFieldProperty(property, e.getValue(), index);
                })
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> {
                    if (o1.index >= 0 && o2.index >= 0) {
                        return o1.index - o2.index;
                    }
                    else if (o1.index >= 0) {
                        return -1;
                    }
                    else if (o2.index >= 0) {
                        return 1;
                    }
                    else if (this.orderAnnotation != null && this.orderAnnotation.alphabetic()) {
                        return o1.property.name.compareTo(o2.property.name);
                    }
                    else {
                        return 0;
                    }
                });
    }

    void merge(Object jpaObject, JsonNode json, boolean tryMergeUnexpectedClass) {
        if (logger.isTraceEnabled())
            logger.trace("Merging " + this);

        if (tryMergeUnexpectedClass) {
            if (!this.clazz.isInstance(jpaObject))
                jpaObject = this.j2j.em.merge(jpaObject);
        }

        if (!this.clazz.isInstance(jpaObject))
            throw new Json2JpaException("Object is not of the expected class");

        if (!this.concrete)
            throw new Json2JpaException("Cannot merge a non concrete class");

        if (json != null) {
            final Object finalJpaObject = jpaObject;
            getSortedFiledProperties(json).forEach(fp -> {
                if (this.j2j.pushPathIfAllowed(fp.property.name)) {
                    if (this.j2j.matchesViews(fp.property.views))
                        if (!fp.property.ignore || this.j2j.discardIgnore)
                            fp.property.merge(finalJpaObject, fp.jsonNode);

                    this.j2j.popPath();
                }
            });
        }
    }

    @Override
    public String toString() {
        return "Json2JpaEntity{" + clazz.getName() + "}";
    }
}

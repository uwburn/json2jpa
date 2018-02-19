package it.mgt.util.json2jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import it.mgt.util.jpa.JpaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

class Json2JpaProperty {

    private final static Logger logger = LoggerFactory.getLogger(Json2JpaProperty.class);

    final String name;
    private final Field field;
    private final PropertyDescriptor propertyDescriptor;
    private final Method getter;
    private final Method setter;
    final boolean id;
    final Class<?> clazz;
    private Class<?> rawClazz;
    private Class<?> parameterClazz;
    private final Json2JpaEntity j2jEntity;
    private final JpaPropertyType type;
    private final String mappedBy;
    private final boolean removeOnOrphans;
    Class<?>[] views;
    boolean ignore;

    Json2JpaProperty(Field field, Json2JpaEntity j2jEntity) {
        this.name = field.getName();
        this.field = field;
        this.field.setAccessible(true);
        this.j2jEntity = j2jEntity;
        this.clazz = field.getType();

        this.id = field.getAnnotation(Id.class) != null;
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        this.removeOnOrphans = field.getAnnotation(RemoveOnOrphans.class) != null;

        if (oneToOne != null) {
            type = JpaPropertyType.ONE_TO_ONE;
            mappedBy = oneToOne.mappedBy();
        }
        else if (manyToOne != null) {
            type = JpaPropertyType.MANY_TO_ONE;
            mappedBy = null;
        }
        else if (oneToMany != null) {
            type = JpaPropertyType.ONE_TO_MANY;
            mappedBy = oneToMany.mappedBy();
        }
        else if (manyToMany != null) {
            type = JpaPropertyType.MANY_TO_MANY;
            mappedBy = manyToMany.mappedBy();
        }
        else {
            type = JpaPropertyType.BASIC;
            mappedBy = null;
        }

        this.propertyDescriptor = JpaUtils.getPropertyDescriptor(field, j2jEntity.beanInfo);
        if (this.propertyDescriptor != null) {
            if (!this.propertyDescriptor.getReadMethod().getReturnType().equals(field.getType())) {
                this.getter = null;
                this.setter = null;
            }
            else {
                this.getter = this.propertyDescriptor.getReadMethod();
                this.getter.setAccessible(true);
                this.setter = this.propertyDescriptor.getWriteMethod();
                this.setter.setAccessible(true);
            }
        }
        else {
            this.getter = null;
            this.setter = null;
        }

        if (this.setter != null) {
            JsonView jsonView = this.setter.getAnnotation(JsonView.class);
            if (jsonView != null)
                views = jsonView.value();
            else
                views = new Class<?>[0];
        }
        else {
            views = new Class<?>[0];
        }

        loadJacksonAnnotations();
        loadGenericClasses();
    }

    Json2JpaProperty(PropertyDescriptor propertyDescriptor, Json2JpaEntity j2jEntity) {
        this.name = propertyDescriptor.getName();
        this.propertyDescriptor = propertyDescriptor;
        this.j2jEntity = j2jEntity;
        this.field = JpaUtils.getField(propertyDescriptor, j2jEntity.clazz);
        if (this.field != null)
            this.field.setAccessible(true);

        this.getter = propertyDescriptor.getReadMethod();
        this.getter.setAccessible(true);
        this.setter = propertyDescriptor.getWriteMethod();
        this.setter.setAccessible(true);

        this.clazz = getter.getReturnType();

        this.id = this.getter.getAnnotation(Id.class) != null;
        OneToOne oneToOne = this.getter.getAnnotation(OneToOne.class);
        ManyToOne manyToOne = this.getter.getAnnotation(ManyToOne.class);
        OneToMany oneToMany = this.getter.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = this.getter.getAnnotation(ManyToMany.class);
        this.removeOnOrphans = this.getter.getAnnotation(RemoveOnOrphans.class) != null;

        if (oneToOne != null) {
            type = JpaPropertyType.ONE_TO_ONE;
            mappedBy = oneToOne.mappedBy();
        }
        else if (manyToOne != null) {
            type = JpaPropertyType.MANY_TO_ONE;
            mappedBy = null;
        }
        else if (oneToMany != null) {
            type = JpaPropertyType.ONE_TO_MANY;
            mappedBy = oneToMany.mappedBy();
        }
        else if (manyToMany != null) {
            type = JpaPropertyType.MANY_TO_MANY;
            mappedBy = manyToMany.mappedBy();
        }
        else {
            type = JpaPropertyType.BASIC;
            mappedBy = null;
        }

        JsonView jsonView = this.setter.getAnnotation(JsonView.class);
        if (jsonView != null)
            views = jsonView.value();
        else
            views = new Class<?>[0];

        loadJacksonAnnotations();
        loadGenericClasses();
    }

    private void loadJacksonAnnotations() {
        VisibilityChecker<?> vc = j2jEntity.j2j.objectMapper.getVisibilityChecker();

        JsonIgnoreProperties ignoreProperties = JpaUtils.getAnnotation(this.j2jEntity.clazz, JsonIgnoreProperties.class);
        if (ignoreProperties != null && Arrays.stream(ignoreProperties.value()).anyMatch(this.name::equals))
                ignore = true;

        if (this.field != null && vc.isFieldVisible(this.field)) {
            JsonIgnore jsonIgnore = this.field.getAnnotation(JsonIgnore.class);
            if (jsonIgnore != null)
                ignore = jsonIgnore.value();

            JsonView jsonView = this.field.getAnnotation(JsonView.class);
            if (jsonView != null)
                views = jsonView.value();
        }

        if (this.getter != null && vc.isGetterVisible(this.getter)) {
            JsonIgnore jsonIgnore = this.getter.getAnnotation(JsonIgnore.class);
            if (jsonIgnore != null)
                ignore = jsonIgnore.value();

            JsonView jsonView = this.getter.getAnnotation(JsonView.class);
            if (jsonView != null)
                views = jsonView.value();
        }

        if (this.setter != null && vc.isSetterVisible(this.setter)) {
            JsonIgnore jsonIgnore = this.setter.getAnnotation(JsonIgnore.class);
            if (jsonIgnore != null)
                ignore = jsonIgnore.value();

            JsonView jsonView = this.setter.getAnnotation(JsonView.class);
            if (jsonView != null)
                views = jsonView.value();
        }
    }

    private void loadGenericClasses() {
        if (this.clazz.getTypeParameters().length == 1) {
            try {
                Type genericType;
                if (this.getter != null)
                    genericType = this.getter.getGenericReturnType();
                else
                    genericType = this.field.getGenericType();

                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type parameterType = parameterizedType.getActualTypeArguments()[0];
                this.rawClazz = Class.forName(parameterizedType.getRawType().getTypeName());
                this.parameterClazz = Class.forName(parameterType.getTypeName());
            }
            catch (Exception ignored) { }
        }
    }

    void merge(Object jpaObject, JsonNode json) {
        if (!j2jEntity.j2j.isClassAllowed(this.clazz))
            return;

        if (logger.isTraceEnabled())
            logger.trace("Merging " + this);

        switch (this.type) {
            case BASIC:
                mergeBasic(jpaObject, json);
                break;
            case ONE_TO_ONE:
            case MANY_TO_ONE:
                mergeToOne(jpaObject, json);
                break;
            case MANY_TO_MANY:
            case ONE_TO_MANY:
                mergeToMany(jpaObject, json);
                break;
        }
    }

    Object get(Object jpaObject) {
        try {
            if (this.setter != null)
                return this.getter.invoke(jpaObject);
            else
                return this.field.get(jpaObject);
        }
        catch (Exception e) {
            throw new Json2JpaException("Cannot get property value", e);
        }
    }

    void set(Object jpaObject, Object arg) {
        try {
            if (this.setter != null)
                this.setter.invoke(jpaObject, arg);
            else
                this.field.set(jpaObject, arg);
        }
        catch (Exception e) {
            throw new Json2JpaException("Cannot set property value", e);
        }
    }

    private void mergeBasic(Object jpaObject, JsonNode json) {
        try {
            if (this.clazz.equals(byte[].class)) {
                this.set(jpaObject, json.toString().getBytes());
                return;
            }

            if (this.parameterClazz != null && this.rawClazz != null) {
                JavaType type = j2jEntity.j2j.objectMapper.getTypeFactory().constructParametricType(this.rawClazz, this.parameterClazz);
                this.set(jpaObject, j2jEntity.j2j.objectMapper.readValue(json.traverse(), type));
                return;
            }

            this.set(jpaObject, j2jEntity.j2j.objectMapper.readValue(json.traverse(), this.clazz));
        }
        catch (IOException e) {
            throw new Json2JpaException("Cannot read JSON value");
        }
    }

    private Object buildNewObject(Json2JpaEntity jn2nEntity, Object updateId, JsonNode json) {
        Object target;

        try {
            Constructor<?> ctor = jn2nEntity.clazz.getConstructor();
            target = ctor.newInstance();
        }
        catch (Exception e) {
            throw new Json2JpaException("Cannot build new object", e);
        }

        this.j2jEntity.j2j.merge(jn2nEntity, target, json);

        if (updateId == null) {
            j2jEntity.j2j.em.persist(target);
        } else {
            target = j2jEntity.j2j.em.merge(target);
        }

        return target;
    }

    private void mergeToOne(Object jpaObject, JsonNode json) {
        Json2JpaEntity jn2nEntity = this.j2jEntity.j2j.getEntity(this.clazz, json);

        Object updateId = null;
        Object currentTarget = this.get(jpaObject);
        Object target = null;
        switch (json.getNodeType()) {
            case POJO:
            case OBJECT:
                try {
                    updateId = j2jEntity.j2j.objectMapper.readValue(json.get(jn2nEntity.idProperty.name).traverse(), jn2nEntity.idProperty.clazz);
                }
                catch (NullPointerException ignored) { }
                catch (IOException e) {
                    throw new Json2JpaException("Unable to read update id", e);
                }

                if (updateId != null) {
                    // Try object traversal and see if target matches... so we avoid em.find
                    Object currentId = null;
                    if (currentTarget != null) {
                        target = currentTarget;
                        currentId = jn2nEntity.idProperty.get(target);
                    }

                    if (!updateId.equals(currentId))
                        target = j2jEntity.j2j.em.find(this.clazz, updateId);
                }

                if (target == null)
                    target = buildNewObject(jn2nEntity, updateId, json);
                else
                    this.j2jEntity.j2j.merge(jn2nEntity, target, json);

                this.set(jpaObject, target);
                break;
            default:
                try {
                    updateId = j2jEntity.j2j.objectMapper.readValue(json.traverse(), jn2nEntity.idProperty.clazz);
                }
                catch (IOException e) {
                    throw new Json2JpaException("Unable to read update id", e);
                }

                if (updateId == null) {
                    this.set(jpaObject, null);
                    if (currentTarget != null && this.removeOnOrphans)
                        j2jEntity.j2j.remove(currentTarget);

                    break;
                }

                target = j2jEntity.j2j.em.find(this.clazz, updateId);

                if (target == null)
                    throw new Json2JpaException("Reference error");

                this.set(jpaObject, target);
                break;
        }

        if (this.mappedBy != null) {
            Json2JpaProperty mappedByProperty = jn2nEntity.properties.get(this.mappedBy);
            if (mappedByProperty != null)
                if (target != null)
                    mappedByProperty.set(target, jpaObject);
                else
                    mappedByProperty.set(currentTarget, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeToMany(Object jpaObject, JsonNode json) {
        Json2JpaEntity jn2nEntity = this.j2jEntity.j2j.getEntity(this.parameterClazz, json);

        Object objCollection = this.get(jpaObject);
        if (!(objCollection instanceof Collection)) {
            String error = "ToMany relation not mapped by a collection";
            if (objCollection == null)
                error += " (Have you initialized the collection field in " + this.j2jEntity.clazz + "?)";
            throw new Json2JpaException(error);
        }
        Collection<Object> collection = (Collection) objCollection;

        if (json.getNodeType() == JsonNodeType.NULL)
            json = new ArrayNode(j2jEntity.j2j.objectMapper.getNodeFactory());
        else if (json.getNodeType() != JsonNodeType.ARRAY)
            throw new Json2JpaException("Expected null or array for ToMany fields");

        Map<Object, Object> missingElements = new HashMap<>();
        for (Object e : collection) {
            Object id = jn2nEntity.getId(e);
            missingElements.put(id, e);
        }

        Iterator<JsonNode> updateIterator = json.elements();
        while (updateIterator.hasNext()) {
            JsonNode elementUpdate = updateIterator.next();

            Json2JpaEntity elementJn2nEntity = this.j2jEntity.j2j.getEntity(this.parameterClazz, elementUpdate);

            Object updateId = null;
            Object currentTarget = null;
            Object target = null;
            switch (elementUpdate.getNodeType()) {
                case POJO:
                case OBJECT:
                    try {
                        updateId = j2jEntity.j2j.objectMapper.readValue(elementUpdate.get(elementJn2nEntity.idProperty.name).traverse(), elementJn2nEntity.idProperty.clazz);
                    } catch (NullPointerException ignored) {
                    } catch (IOException e) {
                        throw new Json2JpaException("Unable to read update id", e);
                    }

                    if (updateId != null) {
                        currentTarget = missingElements.get(updateId);
                        Object currentId = null;
                        if (currentTarget != null) {
                            target = currentTarget;
                            currentId = elementJn2nEntity.idProperty.get(target);
                        }

                        if (!updateId.equals(currentId))
                            target = j2jEntity.j2j.em.find(this.parameterClazz, updateId);

                        missingElements.remove(updateId);
                    }

                    if (target == null)
                        target = buildNewObject(elementJn2nEntity, updateId, elementUpdate);
                    else
                        this.j2jEntity.j2j.merge(elementJn2nEntity, target, elementUpdate);

                    collection.add(target);
                    break;
                default:
                    try {
                        updateId = j2jEntity.j2j.objectMapper.readValue(elementUpdate.traverse(), elementJn2nEntity.idProperty.clazz);
                    } catch (NullPointerException ignored) {
                    } catch (IOException e) {
                        throw new Json2JpaException("Unable to read update id", e);
                    }

                    if (updateId == null)
                        break;

                    target = j2jEntity.j2j.em.find(this.parameterClazz, updateId);

                    if (target == null)
                        throw new Json2JpaException("Reference error");

                    collection.add(target);
                    missingElements.remove(updateId);
                    break;
            }

            if (type == JpaPropertyType.ONE_TO_MANY) {
                if (this.mappedBy != null && !"".equals(this.mappedBy)) {
                    Json2JpaProperty mappedByProperty = elementJn2nEntity.properties.get(this.mappedBy);
                    if (target != null)
                        mappedByProperty.set(target, jpaObject);
                    else
                        mappedByProperty.set(currentTarget, null);
                }
            }
            else if (type == JpaPropertyType.MANY_TO_MANY) {
                Json2JpaProperty mappedByProperty;
                if (this.mappedBy != null && !"".equals(this.mappedBy))
                    mappedByProperty = elementJn2nEntity.properties.get(this.mappedBy);
                else
                    mappedByProperty = elementJn2nEntity.properties.entrySet()
                            .stream()
                            .map(Map.Entry::getValue)
                            .filter(p -> p.mappedBy != null && this.name.equals(p.mappedBy))
                            .findFirst()
                            .orElse(null);

                if (mappedByProperty != null) {
                    Object collectionObj = mappedByProperty.get(target);
                    Collection<Object> mappedCollection;
                    if (collectionObj != null) {
                        mappedCollection = (Collection<Object>) collectionObj;
                    }
                    else {
                        Class<?> collectionClazz = JpaUtils.getConcreteCollectionClass(mappedByProperty.clazz);
                        if (collectionClazz == null)
                            throw new Json2JpaException("Unknown collection class");

                        try {
                            Constructor<?> collectionCtor = collectionClazz.getConstructor();
                            collectionObj = collectionCtor.newInstance();
                            mappedCollection = (Collection<Object>) collectionObj;
                        }
                        catch (Exception e) {
                            throw new Json2JpaException("Cannot build new collection", e);
                        }
                    }

                    mappedCollection.add(jpaObject);
                }
            }
        }

        for (Object e : missingElements.values()) {
            collection.remove(e);
            if (type == JpaPropertyType.MANY_TO_MANY) {
                Collection<Object> mappedCollection = null;
                if (this.mappedBy != null && !"".equals(this.mappedBy)) {
                    Json2JpaProperty mappedByProperty = jn2nEntity.properties.get(this.mappedBy);

                    if (mappedByProperty != null) {
                        Object collectionObj = mappedByProperty.get(e);
                        mappedCollection = (Collection<Object>) collectionObj;
                    }
                }
                else {
                    Json2JpaProperty mappedByProperty = jn2nEntity.properties.entrySet()
                            .stream()
                            .map(Map.Entry::getValue)
                            .filter(p -> p.mappedBy != null && this.name.equals(p.mappedBy))
                            .findFirst()
                            .orElse(null);

                    if (mappedByProperty != null) {
                        Object collectionObj = mappedByProperty.get(e);
                        mappedCollection = (Collection<Object>) collectionObj;
                    }
                }

                if (mappedCollection != null) {
                    mappedCollection.remove(jpaObject);

                    if (mappedCollection.size() > 0)
                        continue;
                }
            }
            else if (type == JpaPropertyType.ONE_TO_MANY) {
                if (this.mappedBy != null && !"".equals(this.mappedBy)) {
                    Json2JpaProperty mappedByProperty = jn2nEntity.properties.get(this.mappedBy);

                    if (mappedByProperty != null) {
                        mappedByProperty.set(e, null);
                    }
                }
            }

            if (removeOnOrphans)
                j2jEntity.j2j.remove(e);
        }
    }

    @Override
    public String toString() {
        return "Json2JpaProperty{" + j2jEntity.clazz.getName() + "." + name + "[" + type+ "]}";
    }
}

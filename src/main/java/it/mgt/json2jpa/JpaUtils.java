package it.mgt.json2jpa;

import javax.persistence.Id;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

class JpaUtils {

    static List<Field> getFields(Class<?> clazz) {
        if (clazz == null)
            return null;

        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        List<Field> superFields = getFields(clazz.getSuperclass());
        if (superFields != null)
            fields.addAll(superFields);

        return fields.stream()
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toList());
    }

    static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClazz) {
        if (annotationClazz == null)
            return null;

        if (clazz == null)
            return null;

        T annotation = clazz.getAnnotation(annotationClazz);

        if (annotation != null)
            return annotation;
        else
            return getAnnotation(clazz.getSuperclass(), annotationClazz);
    }

    static Field getIdField(Class<?> clazz) {
        if (clazz == null)
            return null;

        for (Field f : getFields(clazz))
            if (f.getAnnotation(Id.class) != null)
                return f;

        return null;
    }

    static PropertyDescriptor getPropertyDescriptor(Field field, BeanInfo beanInfo) {
        if (beanInfo == null)
            return null;

        if (field == null)
            return null;

        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors())
            if (pd.getName().equals(field.getName()))
                return pd;

        return null;
    }

    static Field getField(PropertyDescriptor propertyDescriptor, Class<?> clazz) {
        if (clazz == null)
            return null;

        if (propertyDescriptor == null)
            return null;

        for (Field f : getFields(clazz))
            if (f.getName().equals(propertyDescriptor.getName()))
                return f;

        return null;
    }

    static Class<?> getConcreteCollectionClass(Class<?> collection) {
        if (!Collection.class.isAssignableFrom(collection))
            return null;

        if (!collection.isInterface() && !Modifier.isAbstract(collection.getModifiers()))
            return collection;

        if (Set.class.isAssignableFrom(collection)) {
            return HashSet.class;
        }
        else if (List.class.isAssignableFrom(collection)) {
            return ArrayList.class;
        }
        else if (Collection.class.isAssignableFrom(collection)) {
            return HashSet.class;
        }

        return null;
    }

    static Object getId(Object entity) {
        Class<?> clazz = entity.getClass();

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors())
                if (pd.getReadMethod() != null)
                    if (pd.getReadMethod().getAnnotation(Id.class) != null)
                        return pd.getReadMethod().invoke(entity);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not retrieve id from getter", e);
        }

        Field idField = getIdField(clazz);
        if (idField == null)
            throw new RuntimeException("No @Id found on getters or fields");

        try {
            PropertyDescriptor idPropertyDescriptor = getPropertyDescriptor(idField, beanInfo);
            if (idPropertyDescriptor != null)
                return idPropertyDescriptor.getReadMethod().invoke(entity);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not retrieve id from getter", e);
        }

        try {
            idField.setAccessible(true);
            return idField.get(entity);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not retrieve id from field", e);
        }

    }

}

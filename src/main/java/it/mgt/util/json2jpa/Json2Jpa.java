package it.mgt.util.json2jpa;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import it.mgt.util.jpa.JpaUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Json2Jpa {

    private final static Logger logger = LoggerFactory.getLogger(Json2Jpa.class);

    @PersistenceContext
    EntityManager em;

    ObjectMapper objectMapper;

    private int maxDepth = -1;
    private int depth = 0;

    private Set<String> ignoredPaths = new HashSet<>();
    private Set<String> allowedPaths = new HashSet<>();
    private final Stack<String> pathStack = new Stack<>();

    private boolean ignoreRootClass = false;
    private Set<Class> ignoredClasses = new HashSet<>();
    private Set<Class> allowedClasses = new HashSet<>();

    boolean discardIgnore = false;

    private Class<?> view;

    private boolean skipTerminalJpaOperation = false;

    private Set<Object> mergedObjects = new HashSet<>();
    private Set<Object> removedObjects = new HashSet<>();

    private final Map<Class<?>, Json2JpaEntity> entities = new HashMap<>();

    public Json2Jpa() {
    }

    public Json2Jpa(EntityManager em, ObjectMapper objectMapper) {
        this.em = em;
        this.objectMapper = objectMapper;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Json2Jpa setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public Set<String> getIgnoredPaths() {
        return ignoredPaths;
    }

    public Json2Jpa setIgnoredPaths(Set<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths;
        return this;
    }

    public Json2Jpa addIgnoredPath(String ignoredPath) {
        this.ignoredPaths.add(ignoredPath);
        return this;
    }

    public Set<String> getAllowedPaths() {
        return allowedPaths;
    }

    public Json2Jpa setAllowedPaths(Set<String> allowedPaths) {
        this.allowedPaths = allowedPaths;
        return this;
    }

    public Json2Jpa addAllowedPaths(String allowedPath) {
        this.allowedPaths.add(allowedPath);
        return this;
    }

    public boolean isIgnoreRootClass() {
        return ignoreRootClass;
    }

    public Json2Jpa setIgnoreRootClass(boolean ignoreRootClass) {
        this.ignoreRootClass = ignoreRootClass;
        return this;
    }

    public Set<Class> getIgnoredClasses() {
        return ignoredClasses;
    }

    public Json2Jpa setIgnoredClasses(Set<Class> ignoredClasses) {
        this.ignoredClasses = ignoredClasses;
        return this;
    }

    public Json2Jpa addIgnoredClass(Class<?> clazz) {
        this.ignoredClasses.add(clazz);
        return this;
    }

    public Set<Class> getAllowedClasses() {
        return allowedClasses;
    }

    public Json2Jpa setAllowedClasses(Set<Class> allowedClasses) {
        this.allowedClasses = allowedClasses;
        return this;
    }

    public Json2Jpa addAllowedClass(Class<?> clazz) {
        this.allowedClasses.add(clazz);
        return this;
    }

    public Json2Jpa discardIgnore(boolean value) {
        this.discardIgnore = value;
        return this;
    }

    public Class<?> getView() {
        return view;
    }

    public void setView(Class<?> view) {
        this.view = view;
    }

    public Json2Jpa withView(Class<?> view) {
        this.view = view;
        return this;
    }

    public Json2Jpa skipTerminalJpaOperation(boolean value) {
        this.skipTerminalJpaOperation = value;
        return this;
    }

    boolean pushPathIfAllowed(String path) {
        boolean allowed = isPathAllowed(path);

        if (allowed)
            pathStack.push(path);

        if (logger.isDebugEnabled() && pathStack.size() > 0)
            logger.debug("Path: " + StringUtils.join(pathStack, '/'));

        return allowed;
    }

    void popPath() {
        pathStack.pop();
    }

    private boolean isPathAllowed(String path) {
        String jsonPath = StringUtils.join(pathStack, '/');
        if (path != null)
            if (jsonPath.length() > 0)
                jsonPath += '/' + path;
            else
                jsonPath = path;

        boolean matchAllowed = true;
        if (allowedPaths.size() > 0) {
            matchAllowed = false;
            for (String allowedPath : allowedPaths) {
                if (GlobMatcher.match(allowedPath, jsonPath)) {
                    matchAllowed = true;
                    break;
                }
            }
        }

        boolean matchIgnored = false;
        for (String ignoredPath : ignoredPaths) {
            if (GlobMatcher.match(ignoredPath, jsonPath)) {
                matchIgnored = true;
            }
        }

        return matchAllowed && !matchIgnored;
    }

    boolean isClassAllowed(Class<?> clazz) {
        boolean match = true;
        if (allowedClasses.size() > 0) {
            match = allowedClasses.contains(clazz);
        }

        return match && !ignoredClasses.contains(clazz);
    }

    boolean matchesViews(Class<?>[] jsonViews) {
        if (this.view == null)
            return true;

        if (jsonViews == null)
            return false;

        if (jsonViews.length > 0)
            return false;

        return Arrays.stream(jsonViews)
                .anyMatch(v -> v.equals(this.view));
    }

    Json2JpaEntity getEntity(Class<?> clazz, JsonNode jsonNode) {
        Class<?> resolvedType = getConcreteType(clazz, jsonNode);
        if (resolvedType == null)
            resolvedType = clazz;

        return entities.computeIfAbsent(resolvedType, k -> new Json2JpaEntity(clazz, jsonNode, this));
    }

    @SuppressWarnings("unchecked")
    <T> Class<T> getConcreteType(Class<T> clazz, JsonNode json) {
        AnnotatedClass annotatedClazz = AnnotatedClassResolver.resolveWithoutSuperTypes(objectMapper.getDeserializationConfig(), clazz);
        Collection<NamedType> namedTypes = objectMapper.getSubtypeResolver().collectAndResolveSubtypesByTypeId(objectMapper.getDeserializationConfig(), annotatedClazz);

        Class<T> concreteType = null;
        if (!Modifier.isInterface(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers()))
            concreteType = clazz;

        if (json == null)
            return concreteType;

        JsonTypeInfo typeInfo = annotatedClazz.getAnnotation(JsonTypeInfo.class);
        if (typeInfo == null)
            return concreteType;

        String typeProperty = typeInfo.property();
        if (typeProperty.equals(""))
            typeProperty = "@type";

        JsonNode typeNode = json.get(typeProperty);
        if (typeNode == null)
            return concreteType;

        String typeName = typeNode.asText();

        concreteType = namedTypes.stream()
                .filter(nt -> {
                    if (nt.getName() != null)
                        return nt.getName().equals(typeName);
                    else
                        return nt.getType().getSimpleName().equals(typeName);
                })
                .findFirst()
                .map(nt -> (Class<T>) nt.getType())
                .orElse(null);

        return concreteType;
    }

    /*<T> T constructObject(Class<T> clazz, JsonNode json) {
        try {
            Class<T> concreteType = getConcreteType(clazz, json);
            Constructor<T> ctor = concreteType.getConstructor();
            return ctor.newInstance();
        }
        catch (Exception e) {
            throw new Json2JpaException(e);
        }
    }*/

    @SuppressWarnings("unchecked")
    public <T> T construct(Class<T> clazz, JsonNode json) {
        try {
            Json2JpaEntity json2JpaEntity = getEntity(clazz, json);

            Constructor<?> ctor = json2JpaEntity.clazz.getConstructor();
            T jpaObject = (T) ctor.newInstance();

            merge(json2JpaEntity, jpaObject, json, false);

            flushRemoved();

            if (!skipTerminalJpaOperation) {
                if (json2JpaEntity.getId(jpaObject) != null && em.find(clazz, json2JpaEntity.getId(jpaObject)) != null)
                    jpaObject = em.merge(jpaObject);
                else
                    em.persist(jpaObject);
            }

            return jpaObject;
        }
        catch (Json2JpaException e) {
            throw e;
        }
        catch (Exception e) {
            throw new Json2JpaException(e);
        }
    }

    public <T> T merge(T jpaObject, JsonNode json) {
        return merge(jpaObject, json, false);
    }

    public <T> T merge(T jpaObject, JsonNode json, boolean tryMergeUnexpectedClass) {
        Class<?> clazz = jpaObject.getClass();
        Json2JpaEntity json2JpaEntity = getEntity(clazz, json);

        try {
            merge(json2JpaEntity, jpaObject, json, tryMergeUnexpectedClass);

            flushRemoved();

            if (!skipTerminalJpaOperation)
                jpaObject = em.merge(jpaObject);

            return jpaObject;
        }
        catch (Json2JpaException e) {
            throw e;
        }
        catch (Exception e) {
            throw new Json2JpaException(e);
        }
    }

    void merge(Json2JpaEntity json2JpaEntity, Object jpaObject, JsonNode json, boolean tryMergeUnexpectedClass) {
        Class<?> clazz = jpaObject.getClass();

        if (depth == 0 && ignoreRootClass)
            ignoredClasses.add(clazz);

        if (mergedObjects.contains(jpaObject))
            return;

        if (depth > 0 && !isClassAllowed(clazz))
            return;

        if (maxDepth >= 0 && depth > maxDepth)
            return;

        if (logger.isDebugEnabled())
            logger.debug("Depth: " + depth);

        ++depth;

        try {
            json2JpaEntity.merge(jpaObject, json, tryMergeUnexpectedClass);
            mergedObjects.add(jpaObject);

            --depth;
        }
        catch (Json2JpaException e) {
            throw e;
        }
        catch (Exception e) {
            throw new Json2JpaException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<T> construct(Class<?> collectionClazz, Class<T> clazz, JsonNode json) {
        collectionClazz = JpaUtils.getConcreteCollectionClass(collectionClazz);
        if (collectionClazz == null)
            throw new Json2JpaException("Expected null or array for collections");

        try {
            Constructor<?> collectionCtor = collectionClazz.getConstructor();
            Object collectionObj = collectionCtor.newInstance();
            Collection<T> collection = (Collection<T>) collectionObj;
            return doMergeEntities(collection, clazz, json, false);
        }
        catch (Json2JpaException e) {
            throw e;
        }
        catch (Exception e) {
            throw new Json2JpaException(e);
        }
    }

    public <T> Collection<T> merge(Collection<T> jpaCollection, Class<T> clazz, JsonNode json) {
        return merge(jpaCollection, clazz, json, false);
    }

    public <T> Collection<T> merge(Collection<T> jpaCollection, Class<T> clazz, JsonNode json, boolean tryMergeUnexpectedClass) {
        try {
            return doMergeEntities(jpaCollection, clazz, json, tryMergeUnexpectedClass);
        }
        catch (Json2JpaException e) {
            throw e;
        }
        catch (Exception e) {
            throw new Json2JpaException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<T> doMergeEntities(Collection<T> jpaCollection, Class<T> clazz, JsonNode json, boolean tryMergeUnexpectedClass) {
        Json2JpaEntity jn2nEntity = getEntity(clazz, null);

        Map<Object, T> missingElements = new HashMap<>();
        for (T e : jpaCollection) {
            Object id = jn2nEntity.getId(e);
            missingElements.put(id, e);
        }

        if (json.getNodeType() == JsonNodeType.NULL)
            json = new ArrayNode(objectMapper.getNodeFactory());
        else if (json.getNodeType() != JsonNodeType.ARRAY)
            throw new Json2JpaException("Expected null or array for collections");

        Collection<T> mergedJpaObjects = new LinkedHashSet<>();

        Iterator<JsonNode> updateIterator = json.elements();
        while (updateIterator.hasNext()) {
            depth = 0;
            pathStack.clear();
            JsonNode elementUpdate = updateIterator.next();

            Json2JpaEntity elementJn2nEntity = getEntity(clazz, elementUpdate);

            Object updateId = null;
            T jpaObject = null;
            switch (elementUpdate.getNodeType()) {
                case POJO:
                case OBJECT:
                    try {
                        updateId = objectMapper.readValue(elementUpdate.get(elementJn2nEntity.idProperty.name).traverse(), elementJn2nEntity.idProperty.clazz);
                    }
                    catch (NullPointerException ignored) { }
                    catch (IOException e) {
                        throw new Json2JpaException("Cannot read id value", e);
                    }

                    if (updateId != null) {
                        T currentJpaObject = missingElements.get(updateId);
                        Object currentId = null;
                        if (currentJpaObject != null)
                            currentId = elementJn2nEntity.getId(currentJpaObject);

                        if (updateId.equals(currentId))
                            jpaObject = currentJpaObject;
                        else
                            jpaObject = em.find(clazz, updateId);

                        missingElements.remove(updateId);
                    }

                    if (jpaObject == null) {
                        try {
                            Constructor<?> ctor = elementJn2nEntity.clazz.getConstructor();
                            jpaObject = (T) ctor.newInstance();
                        }
                        catch (Exception e) {
                            throw new Json2JpaException("Cannot build new object", e);
                        }

                        elementJn2nEntity.merge(jpaObject, elementUpdate, tryMergeUnexpectedClass);

                        if (updateId == null) {
                            if (!skipTerminalJpaOperation)
                                em.persist(jpaObject);
                        }
                        else {
                            if (!skipTerminalJpaOperation)
                                jpaObject = em.merge(jpaObject);
                        }
                    }
                    else {
                        elementJn2nEntity.merge(jpaObject, elementUpdate, tryMergeUnexpectedClass);
                    }

                    mergedJpaObjects.add(jpaObject);
                    break;
                default:
                    try {
                        updateId = objectMapper.readValue(elementUpdate.traverse(), elementJn2nEntity.idProperty.clazz);
                    }
                    catch (IOException e) {
                        throw new Json2JpaException("Cannot read id value", e);
                    }

                    if (updateId == null)
                        break;

                    jpaObject = em.find(clazz, updateId);

                    if (jpaObject == null)
                        throw new Json2JpaException("Reference error");

                    mergedJpaObjects.add(jpaObject);
                    missingElements.remove(updateId);
                    break;
            }
        }

        for (T e : missingElements.values())
            remove(e);

        if (removedObjects.size() > 0)
            em.flush();

        return mergedJpaObjects;
    }

    void remove(Object object) {
        if (removedObjects.contains(object))
            return;

        em.remove(object);
        //em.flush();
        removedObjects.add(object);
    }

    private void flushRemoved() {
        if (removedObjects.size() > 0)
            em.flush();
    }
}

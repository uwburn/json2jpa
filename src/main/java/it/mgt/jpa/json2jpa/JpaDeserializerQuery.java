package it.mgt.jpa.json2jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface JpaDeserializerQuery {
    
    String query();
    JpaDeserializerQueryParam[] params();
    
}

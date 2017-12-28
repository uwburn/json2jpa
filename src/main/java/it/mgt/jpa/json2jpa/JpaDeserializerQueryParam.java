package it.mgt.jpa.json2jpa;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface JpaDeserializerQueryParam {
    
    String name();
    Class<?> type();
    
    
}

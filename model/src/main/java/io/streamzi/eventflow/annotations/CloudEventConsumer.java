package io.streamzi.eventflow.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method as taking cloud events
 * @author hhiden
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudEventConsumer {
    String name() default "in";
    ObjectType type() default ObjectType.CLOUDEVENT;
}
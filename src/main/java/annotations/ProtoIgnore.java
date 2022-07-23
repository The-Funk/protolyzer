package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to denote a field on a POJO that should be ignored by Protolyzer.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProtoIgnore {
    public boolean incoming() default true;
    public boolean outgoing() default true;
}
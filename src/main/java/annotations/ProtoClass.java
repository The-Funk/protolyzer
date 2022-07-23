package annotations;

import com.google.protobuf.GeneratedMessageV3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be place on a POJO field and is used to denote a nested protobuf object class
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProtoClass {
    public Class<? extends GeneratedMessageV3> value();
}

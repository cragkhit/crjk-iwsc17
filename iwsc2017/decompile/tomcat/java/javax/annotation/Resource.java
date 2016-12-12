package javax.annotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
@Target ( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface Resource {
String name() default "";
Class<?> type() default Object.class;
AuthenticationType authenticationType() default AuthenticationType.CONTAINER;
boolean shareable() default true;
String description() default "";
String mappedName() default "";
String lookup() default "";
    public enum AuthenticationType {
        CONTAINER,
        APPLICATION;
    }
}

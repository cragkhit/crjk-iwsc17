package javax.persistence;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
@Target ( {} )
@Retention ( RetentionPolicy.RUNTIME )
public @interface PersistenceProperty {
    String name();
    String value();
}

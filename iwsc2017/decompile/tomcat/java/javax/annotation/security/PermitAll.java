package javax.annotation.security;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
@Target ( { ElementType.TYPE, ElementType.METHOD } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface PermitAll {
}

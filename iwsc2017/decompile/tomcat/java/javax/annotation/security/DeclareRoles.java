package javax.annotation.security;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
@Target ( { ElementType.TYPE } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface DeclareRoles {
    String[] value();
}

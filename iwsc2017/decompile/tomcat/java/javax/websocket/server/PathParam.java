package javax.websocket.server;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
@Retention ( RetentionPolicy.RUNTIME )
@Target ( { ElementType.PARAMETER } )
public @interface PathParam {
    String value();
}

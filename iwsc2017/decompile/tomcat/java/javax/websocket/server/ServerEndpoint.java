package javax.websocket.server;
import javax.websocket.Encoder;
import javax.websocket.Decoder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
@Retention ( RetentionPolicy.RUNTIME )
@Target ( { ElementType.TYPE } )
public @interface ServerEndpoint {
    String value();
String[] subprotocols() default {};
Class<? extends Decoder>[] decoders() default {};
Class<? extends Encoder>[] encoders() default {};
Class<? extends ServerEndpointConfig.Configurator> configurator() default ServerEndpointConfig.Configurator.class;
}

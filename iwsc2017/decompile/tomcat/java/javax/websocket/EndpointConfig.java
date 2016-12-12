package javax.websocket;
import java.util.Map;
import java.util.List;
public interface EndpointConfig {
    List<Class<? extends Encoder>> getEncoders();
    List<Class<? extends Decoder>> getDecoders();
    Map<String, Object> getUserProperties();
}

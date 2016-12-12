package javax.websocket.server;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import java.util.List;
final class DefaultServerEndpointConfig implements ServerEndpointConfig {
    private final Class<?> endpointClass;
    private final String path;
    private final List<String> subprotocols;
    private final List<Extension> extensions;
    private final List<Class<? extends Encoder>> encoders;
    private final List<Class<? extends Decoder>> decoders;
    private final Configurator serverEndpointConfigurator;
    private final Map<String, Object> userProperties;
    DefaultServerEndpointConfig ( final Class<?> endpointClass, final String path, final List<String> subprotocols, final List<Extension> extensions, final List<Class<? extends Encoder>> encoders, final List<Class<? extends Decoder>> decoders, final Configurator serverEndpointConfigurator ) {
        this.userProperties = new ConcurrentHashMap<String, Object>();
        this.endpointClass = endpointClass;
        this.path = path;
        this.subprotocols = subprotocols;
        this.extensions = extensions;
        this.encoders = encoders;
        this.decoders = decoders;
        this.serverEndpointConfigurator = serverEndpointConfigurator;
    }
    @Override
    public Class<?> getEndpointClass() {
        return this.endpointClass;
    }
    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return this.encoders;
    }
    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return this.decoders;
    }
    @Override
    public String getPath() {
        return this.path;
    }
    @Override
    public Configurator getConfigurator() {
        return this.serverEndpointConfigurator;
    }
    @Override
    public final Map<String, Object> getUserProperties() {
        return this.userProperties;
    }
    @Override
    public final List<String> getSubprotocols() {
        return this.subprotocols;
    }
    @Override
    public final List<Extension> getExtensions() {
        return this.extensions;
    }
}

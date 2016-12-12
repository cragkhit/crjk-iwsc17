package javax.websocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
final class DefaultClientEndpointConfig implements ClientEndpointConfig {
    private final List<String> preferredSubprotocols;
    private final List<Extension> extensions;
    private final List<Class<? extends Encoder>> encoders;
    private final List<Class<? extends Decoder>> decoders;
    private final Map<String, Object> userProperties;
    private final Configurator configurator;
    DefaultClientEndpointConfig ( final List<String> preferredSubprotocols, final List<Extension> extensions, final List<Class<? extends Encoder>> encoders, final List<Class<? extends Decoder>> decoders, final Configurator configurator ) {
        this.userProperties = new ConcurrentHashMap<String, Object>();
        this.preferredSubprotocols = preferredSubprotocols;
        this.extensions = extensions;
        this.decoders = decoders;
        this.encoders = encoders;
        this.configurator = configurator;
    }
    @Override
    public List<String> getPreferredSubprotocols() {
        return this.preferredSubprotocols;
    }
    @Override
    public List<Extension> getExtensions() {
        return this.extensions;
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
    public final Map<String, Object> getUserProperties() {
        return this.userProperties;
    }
    @Override
    public Configurator getConfigurator() {
        return this.configurator;
    }
}

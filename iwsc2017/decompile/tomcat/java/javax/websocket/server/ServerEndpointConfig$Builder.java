package javax.websocket.server;
import java.util.Collections;
import javax.websocket.Extension;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import java.util.List;
public static final class Builder {
    private final Class<?> endpointClass;
    private final String path;
    private List<Class<? extends Encoder>> encoders;
    private List<Class<? extends Decoder>> decoders;
    private List<String> subprotocols;
    private List<Extension> extensions;
    private Configurator configurator;
    public static Builder create ( final Class<?> endpointClass, final String path ) {
        return new Builder ( endpointClass, path );
    }
    private Builder ( final Class<?> endpointClass, final String path ) {
        this.encoders = Collections.emptyList();
        this.decoders = Collections.emptyList();
        this.subprotocols = Collections.emptyList();
        this.extensions = Collections.emptyList();
        this.configurator = Configurator.fetchContainerDefaultConfigurator();
        this.endpointClass = endpointClass;
        this.path = path;
    }
    public ServerEndpointConfig build() {
        return new DefaultServerEndpointConfig ( this.endpointClass, this.path, this.subprotocols, this.extensions, this.encoders, this.decoders, this.configurator );
    }
    public Builder encoders ( final List<Class<? extends Encoder>> encoders ) {
        if ( encoders == null || encoders.size() == 0 ) {
            this.encoders = Collections.emptyList();
        } else {
            this.encoders = Collections.unmodifiableList ( ( List<? extends Class<? extends Encoder>> ) encoders );
        }
        return this;
    }
    public Builder decoders ( final List<Class<? extends Decoder>> decoders ) {
        if ( decoders == null || decoders.size() == 0 ) {
            this.decoders = Collections.emptyList();
        } else {
            this.decoders = Collections.unmodifiableList ( ( List<? extends Class<? extends Decoder>> ) decoders );
        }
        return this;
    }
    public Builder subprotocols ( final List<String> subprotocols ) {
        if ( subprotocols == null || subprotocols.size() == 0 ) {
            this.subprotocols = Collections.emptyList();
        } else {
            this.subprotocols = Collections.unmodifiableList ( ( List<? extends String> ) subprotocols );
        }
        return this;
    }
    public Builder extensions ( final List<Extension> extensions ) {
        if ( extensions == null || extensions.size() == 0 ) {
            this.extensions = Collections.emptyList();
        } else {
            this.extensions = Collections.unmodifiableList ( ( List<? extends Extension> ) extensions );
        }
        return this;
    }
    public Builder configurator ( final Configurator serverEndpointConfigurator ) {
        if ( serverEndpointConfigurator == null ) {
            this.configurator = Configurator.fetchContainerDefaultConfigurator();
        } else {
            this.configurator = serverEndpointConfigurator;
        }
        return this;
    }
}

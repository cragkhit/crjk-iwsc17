package javax.websocket;
import java.util.Collections;
import java.util.List;
public static final class Builder {
    private static final Configurator DEFAULT_CONFIGURATOR;
    private Configurator configurator;
    private List<String> preferredSubprotocols;
    private List<Extension> extensions;
    private List<Class<? extends Encoder>> encoders;
    private List<Class<? extends Decoder>> decoders;
    public static Builder create() {
        return new Builder();
    }
    private Builder() {
        this.configurator = Builder.DEFAULT_CONFIGURATOR;
        this.preferredSubprotocols = Collections.emptyList();
        this.extensions = Collections.emptyList();
        this.encoders = Collections.emptyList();
        this.decoders = Collections.emptyList();
    }
    public ClientEndpointConfig build() {
        return new DefaultClientEndpointConfig ( this.preferredSubprotocols, this.extensions, this.encoders, this.decoders, this.configurator );
    }
    public Builder configurator ( final Configurator configurator ) {
        if ( configurator == null ) {
            this.configurator = Builder.DEFAULT_CONFIGURATOR;
        } else {
            this.configurator = configurator;
        }
        return this;
    }
    public Builder preferredSubprotocols ( final List<String> preferredSubprotocols ) {
        if ( preferredSubprotocols == null || preferredSubprotocols.size() == 0 ) {
            this.preferredSubprotocols = Collections.emptyList();
        } else {
            this.preferredSubprotocols = Collections.unmodifiableList ( ( List<? extends String> ) preferredSubprotocols );
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
    static {
        DEFAULT_CONFIGURATOR = new Configurator() {};
    }
}

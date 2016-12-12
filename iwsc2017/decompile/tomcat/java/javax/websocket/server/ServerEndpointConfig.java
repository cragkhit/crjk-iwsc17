package javax.websocket.server;
import javax.websocket.HandshakeResponse;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Collections;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import java.util.List;
import javax.websocket.EndpointConfig;
public interface ServerEndpointConfig extends EndpointConfig {
    Class<?> getEndpointClass();
    String getPath();
    List<String> getSubprotocols();
    List<Extension> getExtensions();
    Configurator getConfigurator();
    public static final class Builder {
        private final Class<?> endpointClass;
        private final String path;
        private List<Class<? extends Encoder>> encoders;
        private List<Class<? extends Decoder>> decoders;
        private List<String> subprotocols;
        private List<Extension> extensions;
        private Configurator configurator;
        public static Builder create ( Class<?> endpointClass, String path ) {
            return new Builder ( endpointClass, path );
        }
        private Builder ( Class<?> endpointClass, String path ) {
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
        public Builder encoders ( List<Class<? extends Encoder>> encoders ) {
            if ( encoders == null || encoders.size() == 0 ) {
                this.encoders = Collections.emptyList();
            } else {
                this.encoders = Collections.unmodifiableList ( ( List<? extends Class<? extends Encoder>> ) encoders );
            }
            return this;
        }
        public Builder decoders ( List<Class<? extends Decoder>> decoders ) {
            if ( decoders == null || decoders.size() == 0 ) {
                this.decoders = Collections.emptyList();
            } else {
                this.decoders = Collections.unmodifiableList ( ( List<? extends Class<? extends Decoder>> ) decoders );
            }
            return this;
        }
        public Builder subprotocols ( List<String> subprotocols ) {
            if ( subprotocols == null || subprotocols.size() == 0 ) {
                this.subprotocols = Collections.emptyList();
            } else {
                this.subprotocols = Collections.unmodifiableList ( ( List<? extends String> ) subprotocols );
            }
            return this;
        }
        public Builder extensions ( List<Extension> extensions ) {
            if ( extensions == null || extensions.size() == 0 ) {
                this.extensions = Collections.emptyList();
            } else {
                this.extensions = Collections.unmodifiableList ( ( List<? extends Extension> ) extensions );
            }
            return this;
        }
        public Builder configurator ( Configurator serverEndpointConfigurator ) {
            if ( serverEndpointConfigurator == null ) {
                this.configurator = Configurator.fetchContainerDefaultConfigurator();
            } else {
                this.configurator = serverEndpointConfigurator;
            }
            return this;
        }
    }
    public static class Configurator {
        private static volatile Configurator defaultImpl;
        private static final Object defaultImplLock;
        private static final String DEFAULT_IMPL_CLASSNAME = "org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator";
        static Configurator fetchContainerDefaultConfigurator() {
            if ( Configurator.defaultImpl == null ) {
                synchronized ( Configurator.defaultImplLock ) {
                    if ( Configurator.defaultImpl == null ) {
                        Configurator.defaultImpl = loadDefault();
                    }
                }
            }
            return Configurator.defaultImpl;
        }
        private static Configurator loadDefault() {
            Configurator result;
            ServiceLoader<Configurator> serviceLoader;
            Iterator<Configurator> iter;
            Class<Configurator> clazz;
            result = null;
            serviceLoader = ServiceLoader.load ( Configurator.class );
            for ( Iterator<Configurator> iter = serviceLoader.iterator(); result == null && iter.hasNext(); result = iter.next() ) {}
            if ( result == null ) {
                try {
                    clazz = ( Class<Configurator> ) Class.forName ( "org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator" );
                    result = clazz.newInstance();
                } catch ( ClassNotFoundException ) {}
                catch ( InstantiationException ) {}
                catch ( IllegalAccessException ex ) {}
            }
            return result;
        }
        public String getNegotiatedSubprotocol ( List<String> supported, List<String> requested ) {
            return fetchContainerDefaultConfigurator().getNegotiatedSubprotocol ( supported, requested );
        }
        public List<Extension> getNegotiatedExtensions ( List<Extension> installed, List<Extension> requested ) {
            return fetchContainerDefaultConfigurator().getNegotiatedExtensions ( installed, requested );
        }
        public boolean checkOrigin ( String originHeaderValue ) {
            return fetchContainerDefaultConfigurator().checkOrigin ( originHeaderValue );
        }
        public void modifyHandshake ( ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response ) {
            fetchContainerDefaultConfigurator().modifyHandshake ( sec, request, response );
        }
        public <T> T getEndpointInstance ( Class<T> clazz ) throws InstantiationException {
            return ( T ) fetchContainerDefaultConfigurator().getEndpointInstance ( ( Class<Object> ) clazz );
        }
        static {
            Configurator.defaultImpl = null;
            defaultImplLock = new Object();
        }
    }
}

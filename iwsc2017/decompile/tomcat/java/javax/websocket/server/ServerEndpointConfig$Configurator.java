package javax.websocket.server;
import javax.websocket.HandshakeResponse;
import javax.websocket.Extension;
import java.util.List;
import java.util.Iterator;
import java.util.ServiceLoader;
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
        Configurator result = null;
        final ServiceLoader<Configurator> serviceLoader = ServiceLoader.load ( Configurator.class );
        for ( Iterator<Configurator> iter = serviceLoader.iterator(); result == null && iter.hasNext(); result = iter.next() ) {}
        if ( result == null ) {
            try {
                final Class<Configurator> clazz = ( Class<Configurator> ) Class.forName ( "org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator" );
                result = clazz.newInstance();
            } catch ( ClassNotFoundException ) {}
            catch ( InstantiationException ) {}
            catch ( IllegalAccessException ex ) {}
        }
        return result;
    }
    public String getNegotiatedSubprotocol ( final List<String> supported, final List<String> requested ) {
        return fetchContainerDefaultConfigurator().getNegotiatedSubprotocol ( supported, requested );
    }
    public List<Extension> getNegotiatedExtensions ( final List<Extension> installed, final List<Extension> requested ) {
        return fetchContainerDefaultConfigurator().getNegotiatedExtensions ( installed, requested );
    }
    public boolean checkOrigin ( final String originHeaderValue ) {
        return fetchContainerDefaultConfigurator().checkOrigin ( originHeaderValue );
    }
    public void modifyHandshake ( final ServerEndpointConfig sec, final HandshakeRequest request, final HandshakeResponse response ) {
        fetchContainerDefaultConfigurator().modifyHandshake ( sec, request, response );
    }
    public <T> T getEndpointInstance ( final Class<T> clazz ) throws InstantiationException {
        return ( T ) fetchContainerDefaultConfigurator().getEndpointInstance ( ( Class<Object> ) clazz );
    }
    static {
        Configurator.defaultImpl = null;
        defaultImplLock = new Object();
    }
}

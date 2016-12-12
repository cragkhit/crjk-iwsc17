package org.apache.catalina.webresources;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.catalina.webresources.war.Handler;
public class TomcatURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final String WAR_PROTOCOL = "war";
    private static final String CLASSPTH_PROTOCOL = "classpath";
    private static volatile TomcatURLStreamHandlerFactory instance = null;
    public static TomcatURLStreamHandlerFactory getInstance() {
        getInstanceInternal ( true );
        return instance;
    }
    private static TomcatURLStreamHandlerFactory getInstanceInternal ( boolean register ) {
        if ( instance == null ) {
            synchronized ( TomcatURLStreamHandlerFactory.class ) {
                if ( instance == null ) {
                    instance = new TomcatURLStreamHandlerFactory ( register );
                }
            }
        }
        return instance;
    }
    private final boolean registered;
    private final List<URLStreamHandlerFactory> userFactories =
        new CopyOnWriteArrayList<>();
    public static boolean register() {
        return getInstanceInternal ( true ).isRegistered();
    }
    public static boolean disable() {
        return !getInstanceInternal ( false ).isRegistered();
    }
    public static void release ( ClassLoader classLoader ) {
        if ( instance == null ) {
            return;
        }
        List<URLStreamHandlerFactory> factories = instance.userFactories;
        for ( URLStreamHandlerFactory factory : factories ) {
            ClassLoader factoryLoader = factory.getClass().getClassLoader();
            while ( factoryLoader != null ) {
                if ( classLoader.equals ( factoryLoader ) ) {
                    factories.remove ( factory );
                    break;
                }
                factoryLoader = factoryLoader.getParent();
            }
        }
    }
    private TomcatURLStreamHandlerFactory ( boolean register ) {
        this.registered = register;
        if ( register ) {
            URL.setURLStreamHandlerFactory ( this );
        }
    }
    public boolean isRegistered() {
        return registered;
    }
    public void addUserFactory ( URLStreamHandlerFactory factory ) {
        userFactories.add ( factory );
    }
    @Override
    public URLStreamHandler createURLStreamHandler ( String protocol ) {
        if ( WAR_PROTOCOL.equals ( protocol ) ) {
            return new Handler();
        } else if ( CLASSPTH_PROTOCOL.equals ( protocol ) ) {
            return new ClasspathURLStreamHandler();
        }
        for ( URLStreamHandlerFactory factory : userFactories ) {
            URLStreamHandler handler =
                factory.createURLStreamHandler ( protocol );
            if ( handler != null ) {
                return handler;
            }
        }
        return null;
    }
}

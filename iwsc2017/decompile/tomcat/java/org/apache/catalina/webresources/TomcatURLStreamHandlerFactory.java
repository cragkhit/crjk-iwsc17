package org.apache.catalina.webresources;
import org.apache.catalina.webresources.war.Handler;
import java.net.URLStreamHandler;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URLStreamHandlerFactory;
public class TomcatURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final String WAR_PROTOCOL = "war";
    private static final String CLASSPTH_PROTOCOL = "classpath";
    private static volatile TomcatURLStreamHandlerFactory instance;
    private final boolean registered;
    private final List<URLStreamHandlerFactory> userFactories;
    public static TomcatURLStreamHandlerFactory getInstance() {
        getInstanceInternal ( true );
        return TomcatURLStreamHandlerFactory.instance;
    }
    private static TomcatURLStreamHandlerFactory getInstanceInternal ( final boolean register ) {
        if ( TomcatURLStreamHandlerFactory.instance == null ) {
            synchronized ( TomcatURLStreamHandlerFactory.class ) {
                if ( TomcatURLStreamHandlerFactory.instance == null ) {
                    TomcatURLStreamHandlerFactory.instance = new TomcatURLStreamHandlerFactory ( register );
                }
            }
        }
        return TomcatURLStreamHandlerFactory.instance;
    }
    public static boolean register() {
        return getInstanceInternal ( true ).isRegistered();
    }
    public static boolean disable() {
        return !getInstanceInternal ( false ).isRegistered();
    }
    public static void release ( final ClassLoader classLoader ) {
        if ( TomcatURLStreamHandlerFactory.instance == null ) {
            return;
        }
        final List<URLStreamHandlerFactory> factories = TomcatURLStreamHandlerFactory.instance.userFactories;
        for ( final URLStreamHandlerFactory factory : factories ) {
            for ( ClassLoader factoryLoader = factory.getClass().getClassLoader(); factoryLoader != null; factoryLoader = factoryLoader.getParent() ) {
                if ( classLoader.equals ( factoryLoader ) ) {
                    factories.remove ( factory );
                    break;
                }
            }
        }
    }
    private TomcatURLStreamHandlerFactory ( final boolean register ) {
        this.userFactories = new CopyOnWriteArrayList<URLStreamHandlerFactory>();
        this.registered = register;
        if ( register ) {
            URL.setURLStreamHandlerFactory ( this );
        }
    }
    public boolean isRegistered() {
        return this.registered;
    }
    public void addUserFactory ( final URLStreamHandlerFactory factory ) {
        this.userFactories.add ( factory );
    }
    @Override
    public URLStreamHandler createURLStreamHandler ( final String protocol ) {
        if ( "war".equals ( protocol ) ) {
            return new Handler();
        }
        if ( "classpath".equals ( protocol ) ) {
            return new ClasspathURLStreamHandler();
        }
        for ( final URLStreamHandlerFactory factory : this.userFactories ) {
            final URLStreamHandler handler = factory.createURLStreamHandler ( protocol );
            if ( handler != null ) {
                return handler;
            }
        }
        return null;
    }
    static {
        TomcatURLStreamHandlerFactory.instance = null;
    }
}

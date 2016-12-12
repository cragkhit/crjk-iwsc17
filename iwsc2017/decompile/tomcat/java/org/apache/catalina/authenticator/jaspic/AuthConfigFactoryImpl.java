package org.apache.catalina.authenticator.jaspic;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.File;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import javax.security.auth.message.config.AuthConfigFactory;
public class AuthConfigFactoryImpl extends AuthConfigFactory {
    private static final Log log;
    private static final StringManager sm;
    private static final String CONFIG_PATH = "conf/jaspic-providers.xml";
    private static final File CONFIG_FILE;
    private static final Object CONFIG_FILE_LOCK;
    private static final String[] EMPTY_STRING_ARRAY;
    private final Map<String, RegistrationContextImpl> registrations;
    public AuthConfigFactoryImpl() {
        this.registrations = new ConcurrentHashMap<String, RegistrationContextImpl>();
        this.loadPersistentRegistrations();
    }
    public AuthConfigProvider getConfigProvider ( final String layer, final String appContext, final RegistrationListener listener ) {
        final String registrationID = this.getRegistrarionID ( layer, appContext );
        final RegistrationContextImpl registrationContext = this.registrations.get ( registrationID );
        if ( registrationContext != null ) {
            registrationContext.addListener ( null );
            return registrationContext.getProvider();
        }
        return null;
    }
    public String registerConfigProvider ( final String className, final Map properties, final String layer, final String appContext, final String description ) {
        final String registrationID = this.doRegisterConfigProvider ( className, properties, layer, appContext, description );
        this.savePersistentRegistrations();
        return registrationID;
    }
    private String doRegisterConfigProvider ( final String className, final Map properties, final String layer, final String appContext, final String description ) {
        if ( AuthConfigFactoryImpl.log.isDebugEnabled() ) {
            AuthConfigFactoryImpl.log.debug ( AuthConfigFactoryImpl.sm.getString ( "authConfigFactoryImpl.registerClass", className, layer, appContext ) );
        }
        AuthConfigProvider provider = null;
        try {
            final Class<?> clazz = Class.forName ( className, true, Thread.currentThread().getContextClassLoader() );
        } catch ( ClassNotFoundException ex ) {}
        try {
            final Class<?> clazz = Class.forName ( className );
            final Constructor<?> constructor = clazz.getConstructor ( Map.class, AuthConfigFactory.class );
            provider = ( AuthConfigProvider ) constructor.newInstance ( properties, null );
        } catch ( ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            throw new SecurityException ( e );
        }
        final String registrationID = this.getRegistrarionID ( layer, appContext );
        this.registrations.put ( registrationID, new RegistrationContextImpl ( layer, appContext, description, true, provider, properties ) );
        return registrationID;
    }
    public String registerConfigProvider ( final AuthConfigProvider provider, final String layer, final String appContext, final String description ) {
        if ( AuthConfigFactoryImpl.log.isDebugEnabled() ) {
            AuthConfigFactoryImpl.log.debug ( AuthConfigFactoryImpl.sm.getString ( "authConfigFactoryImpl.registerInstance", provider.getClass().getName(), layer, appContext ) );
        }
        final String registrationID = this.getRegistrarionID ( layer, appContext );
        this.registrations.put ( registrationID, new RegistrationContextImpl ( layer, appContext, description, false, provider, ( Map ) null ) );
        return registrationID;
    }
    public boolean removeRegistration ( final String registrationID ) {
        return this.registrations.remove ( registrationID ) != null;
    }
    public String[] detachListener ( final RegistrationListener listener, final String layer, final String appContext ) {
        final String registrationID = this.getRegistrarionID ( layer, appContext );
        final RegistrationContextImpl registrationContext = this.registrations.get ( registrationID );
        if ( registrationContext.removeListener ( listener ) ) {
            return new String[] { registrationID };
        }
        return AuthConfigFactoryImpl.EMPTY_STRING_ARRAY;
    }
    public String[] getRegistrationIDs ( final AuthConfigProvider provider ) {
        if ( provider == null ) {
            return this.registrations.keySet().toArray ( AuthConfigFactoryImpl.EMPTY_STRING_ARRAY );
        }
        final List<String> results = new ArrayList<String>();
        for ( final Map.Entry<String, RegistrationContextImpl> entry : this.registrations.entrySet() ) {
            if ( provider.equals ( entry.getValue().getProvider() ) ) {
                results.add ( entry.getKey() );
            }
        }
        return results.toArray ( AuthConfigFactoryImpl.EMPTY_STRING_ARRAY );
    }
    public AuthConfigFactory.RegistrationContext getRegistrationContext ( final String registrationID ) {
        return ( AuthConfigFactory.RegistrationContext ) this.registrations.get ( registrationID );
    }
    public void refresh() {
        this.loadPersistentRegistrations();
    }
    private String getRegistrarionID ( final String layer, final String appContext ) {
        return layer + ":" + appContext;
    }
    private void loadPersistentRegistrations() {
        synchronized ( AuthConfigFactoryImpl.CONFIG_FILE_LOCK ) {
            if ( AuthConfigFactoryImpl.log.isDebugEnabled() ) {
                AuthConfigFactoryImpl.log.debug ( AuthConfigFactoryImpl.sm.getString ( "authConfigFactoryImpl.load", AuthConfigFactoryImpl.CONFIG_FILE.getAbsolutePath() ) );
            }
            if ( !AuthConfigFactoryImpl.CONFIG_FILE.isFile() ) {
                return;
            }
            final PersistentProviderRegistrations.Providers providers = PersistentProviderRegistrations.loadProviders ( AuthConfigFactoryImpl.CONFIG_FILE );
            for ( final PersistentProviderRegistrations.Provider provider : providers.getProviders() ) {
                this.doRegisterConfigProvider ( provider.getClassName(), provider.getProperties(), provider.getLayer(), provider.getAppContext(), provider.getDescription() );
            }
        }
    }
    private void savePersistentRegistrations() {
        synchronized ( AuthConfigFactoryImpl.CONFIG_FILE_LOCK ) {
            final PersistentProviderRegistrations.Providers providers = new PersistentProviderRegistrations.Providers();
            for ( final Map.Entry<String, RegistrationContextImpl> entry : this.registrations.entrySet() ) {
                if ( entry.getValue().isPersistent() ) {
                    final PersistentProviderRegistrations.Provider provider = new PersistentProviderRegistrations.Provider();
                    provider.setAppContext ( entry.getValue().getAppContext() );
                    provider.setClassName ( entry.getValue().getProvider().getClass().getName() );
                    provider.setDescription ( entry.getValue().getDescription() );
                    provider.setLayer ( entry.getValue().getMessageLayer() );
                    for ( final Map.Entry<String, String> property : entry.getValue().getProperties().entrySet() ) {
                        provider.addProperty ( property.getKey(), property.getValue() );
                    }
                    providers.addProvider ( provider );
                }
            }
            PersistentProviderRegistrations.writeProviders ( providers, AuthConfigFactoryImpl.CONFIG_FILE );
        }
    }
    static {
        log = LogFactory.getLog ( AuthConfigFactoryImpl.class );
        sm = StringManager.getManager ( AuthConfigFactoryImpl.class );
        CONFIG_FILE = new File ( System.getProperty ( "catalina.base" ), "conf/jaspic-providers.xml" );
        CONFIG_FILE_LOCK = new Object();
        EMPTY_STRING_ARRAY = new String[0];
    }
    private static class RegistrationContextImpl implements AuthConfigFactory.RegistrationContext {
        private final String messageLayer;
        private final String appContext;
        private final String description;
        private final boolean persistent;
        private final AuthConfigProvider provider;
        private final Map<String, String> properties;
        private final List<RegistrationListener> listeners;
        private RegistrationContextImpl ( final String messageLayer, final String appContext, final String description, final boolean persistent, final AuthConfigProvider provider, final Map<String, String> properties ) {
            this.listeners = new CopyOnWriteArrayList<RegistrationListener>();
            this.messageLayer = messageLayer;
            this.appContext = appContext;
            this.description = description;
            this.persistent = persistent;
            this.provider = provider;
            final Map<String, String> propertiesCopy = new HashMap<String, String>();
            if ( properties != null ) {
                propertiesCopy.putAll ( properties );
            }
            this.properties = Collections.unmodifiableMap ( ( Map<? extends String, ? extends String> ) propertiesCopy );
        }
        public String getMessageLayer() {
            return this.messageLayer;
        }
        public String getAppContext() {
            return this.appContext;
        }
        public String getDescription() {
            return this.description;
        }
        public boolean isPersistent() {
            return this.persistent;
        }
        private AuthConfigProvider getProvider() {
            return this.provider;
        }
        private void addListener ( final RegistrationListener listener ) {
            if ( listener != null ) {
                this.listeners.add ( listener );
            }
        }
        private Map<String, String> getProperties() {
            return this.properties;
        }
        private boolean removeListener ( final RegistrationListener listener ) {
            final boolean result = false;
            final Iterator<RegistrationListener> iter = this.listeners.iterator();
            while ( iter.hasNext() ) {
                if ( iter.next().equals ( listener ) ) {
                    iter.remove();
                }
            }
            return result;
        }
    }
}

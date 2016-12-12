package org.apache.catalina.authenticator.jaspic;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.security.auth.message.config.RegistrationListener;
import java.util.List;
import java.util.Map;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.AuthConfigFactory;
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

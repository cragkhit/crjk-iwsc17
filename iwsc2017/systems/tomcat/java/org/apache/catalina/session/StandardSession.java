package org.apache.catalina.session;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.WriteAbortedException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.TomcatPrincipal;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class StandardSession implements HttpSession, Session, Serializable {
    private static final long serialVersionUID = 1L;
    protected static final boolean STRICT_SERVLET_COMPLIANCE;
    protected static final boolean ACTIVITY_CHECK;
    protected static final boolean LAST_ACCESS_AT_START;
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        String activityCheck = System.getProperty (
                                   "org.apache.catalina.session.StandardSession.ACTIVITY_CHECK" );
        if ( activityCheck == null ) {
            ACTIVITY_CHECK = STRICT_SERVLET_COMPLIANCE;
        } else {
            ACTIVITY_CHECK = Boolean.parseBoolean ( activityCheck );
        }
        String lastAccessAtStart = System.getProperty (
                                       "org.apache.catalina.session.StandardSession.LAST_ACCESS_AT_START" );
        if ( lastAccessAtStart == null ) {
            LAST_ACCESS_AT_START = STRICT_SERVLET_COMPLIANCE;
        } else {
            LAST_ACCESS_AT_START = Boolean.parseBoolean ( lastAccessAtStart );
        }
    }
    public StandardSession ( Manager manager ) {
        super();
        this.manager = manager;
        if ( ACTIVITY_CHECK ) {
            accessCount = new AtomicInteger();
        }
    }
    protected static final String EMPTY_ARRAY[] = new String[0];
    protected ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
    protected transient String authType = null;
    protected long creationTime = 0L;
    protected transient volatile boolean expiring = false;
    protected transient StandardSessionFacade facade = null;
    protected String id = null;
    protected volatile long lastAccessedTime = creationTime;
    protected transient ArrayList<SessionListener> listeners = new ArrayList<>();
    protected transient Manager manager = null;
    protected volatile int maxInactiveInterval = -1;
    protected volatile boolean isNew = false;
    protected volatile boolean isValid = false;
    protected transient Map<String, Object> notes = new Hashtable<>();
    protected transient Principal principal = null;
    protected static final StringManager sm = StringManager.getManager ( StandardSession.class );
    @Deprecated
    protected static volatile
    javax.servlet.http.HttpSessionContext sessionContext = null;
    protected final transient PropertyChangeSupport support =
        new PropertyChangeSupport ( this );
    protected volatile long thisAccessedTime = creationTime;
    protected transient AtomicInteger accessCount = null;
    @Override
    public String getAuthType() {
        return ( this.authType );
    }
    @Override
    public void setAuthType ( String authType ) {
        String oldAuthType = this.authType;
        this.authType = authType;
        support.firePropertyChange ( "authType", oldAuthType, this.authType );
    }
    @Override
    public void setCreationTime ( long time ) {
        this.creationTime = time;
        this.lastAccessedTime = time;
        this.thisAccessedTime = time;
    }
    @Override
    public String getId() {
        return ( this.id );
    }
    @Override
    public String getIdInternal() {
        return ( this.id );
    }
    @Override
    public void setId ( String id ) {
        setId ( id, true );
    }
    @Override
    public void setId ( String id, boolean notify ) {
        if ( ( this.id != null ) && ( manager != null ) ) {
            manager.remove ( this );
        }
        this.id = id;
        if ( manager != null ) {
            manager.add ( this );
        }
        if ( notify ) {
            tellNew();
        }
    }
    public void tellNew() {
        fireSessionEvent ( Session.SESSION_CREATED_EVENT, null );
        Context context = manager.getContext();
        Object listeners[] = context.getApplicationLifecycleListeners();
        if ( listeners != null && listeners.length > 0 ) {
            HttpSessionEvent event =
                new HttpSessionEvent ( getSession() );
            for ( int i = 0; i < listeners.length; i++ ) {
                if ( ! ( listeners[i] instanceof HttpSessionListener ) ) {
                    continue;
                }
                HttpSessionListener listener =
                    ( HttpSessionListener ) listeners[i];
                try {
                    context.fireContainerEvent ( "beforeSessionCreated",
                                                 listener );
                    listener.sessionCreated ( event );
                    context.fireContainerEvent ( "afterSessionCreated", listener );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    try {
                        context.fireContainerEvent ( "afterSessionCreated",
                                                     listener );
                    } catch ( Exception e ) {
                    }
                    manager.getContext().getLogger().error
                    ( sm.getString ( "standardSession.sessionEvent" ), t );
                }
            }
        }
    }
    @Override
    public void tellChangedSessionId ( String newId, String oldId,
                                       boolean notifySessionListeners, boolean notifyContainerListeners ) {
        Context context = manager.getContext();
        if ( notifyContainerListeners ) {
            context.fireContainerEvent ( Context.CHANGE_SESSION_ID_EVENT,
                                         new String[] {oldId, newId} );
        }
        if ( notifySessionListeners ) {
            Object listeners[] = context.getApplicationEventListeners();
            if ( listeners != null && listeners.length > 0 ) {
                HttpSessionEvent event =
                    new HttpSessionEvent ( getSession() );
                for ( Object listener : listeners ) {
                    if ( ! ( listener instanceof HttpSessionIdListener ) ) {
                        continue;
                    }
                    HttpSessionIdListener idListener =
                        ( HttpSessionIdListener ) listener;
                    try {
                        idListener.sessionIdChanged ( event, oldId );
                    } catch ( Throwable t ) {
                        manager.getContext().getLogger().error
                        ( sm.getString ( "standardSession.sessionEvent" ), t );
                    }
                }
            }
        }
    }
    @Override
    public long getThisAccessedTime() {
        if ( !isValidInternal() ) {
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getThisAccessedTime.ise" ) );
        }
        return ( this.thisAccessedTime );
    }
    @Override
    public long getThisAccessedTimeInternal() {
        return ( this.thisAccessedTime );
    }
    @Override
    public long getLastAccessedTime() {
        if ( !isValidInternal() ) {
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getLastAccessedTime.ise" ) );
        }
        return ( this.lastAccessedTime );
    }
    @Override
    public long getLastAccessedTimeInternal() {
        return ( this.lastAccessedTime );
    }
    @Override
    public long getIdleTime() {
        if ( !isValidInternal() ) {
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getIdleTime.ise" ) );
        }
        return getIdleTimeInternal();
    }
    @Override
    public long getIdleTimeInternal() {
        long timeNow = System.currentTimeMillis();
        long timeIdle;
        if ( LAST_ACCESS_AT_START ) {
            timeIdle = timeNow - lastAccessedTime;
        } else {
            timeIdle = timeNow - thisAccessedTime;
        }
        return timeIdle;
    }
    @Override
    public Manager getManager() {
        return this.manager;
    }
    @Override
    public void setManager ( Manager manager ) {
        this.manager = manager;
    }
    @Override
    public int getMaxInactiveInterval() {
        return ( this.maxInactiveInterval );
    }
    @Override
    public void setMaxInactiveInterval ( int interval ) {
        this.maxInactiveInterval = interval;
    }
    @Override
    public void setNew ( boolean isNew ) {
        this.isNew = isNew;
    }
    @Override
    public Principal getPrincipal() {
        return ( this.principal );
    }
    @Override
    public void setPrincipal ( Principal principal ) {
        Principal oldPrincipal = this.principal;
        this.principal = principal;
        support.firePropertyChange ( "principal", oldPrincipal, this.principal );
    }
    @Override
    public HttpSession getSession() {
        if ( facade == null ) {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                final StandardSession fsession = this;
                facade = AccessController.doPrivileged (
                new PrivilegedAction<StandardSessionFacade>() {
                    @Override
                    public StandardSessionFacade run() {
                        return new StandardSessionFacade ( fsession );
                    }
                } );
            } else {
                facade = new StandardSessionFacade ( this );
            }
        }
        return ( facade );
    }
    @Override
    public boolean isValid() {
        if ( !this.isValid ) {
            return false;
        }
        if ( this.expiring ) {
            return true;
        }
        if ( ACTIVITY_CHECK && accessCount.get() > 0 ) {
            return true;
        }
        if ( maxInactiveInterval > 0 ) {
            int timeIdle = ( int ) ( getIdleTimeInternal() / 1000L );
            if ( timeIdle >= maxInactiveInterval ) {
                expire ( true );
            }
        }
        return this.isValid;
    }
    @Override
    public void setValid ( boolean isValid ) {
        this.isValid = isValid;
    }
    @Override
    public void access() {
        this.thisAccessedTime = System.currentTimeMillis();
        if ( ACTIVITY_CHECK ) {
            accessCount.incrementAndGet();
        }
    }
    @Override
    public void endAccess() {
        isNew = false;
        if ( LAST_ACCESS_AT_START ) {
            this.lastAccessedTime = this.thisAccessedTime;
            this.thisAccessedTime = System.currentTimeMillis();
        } else {
            this.thisAccessedTime = System.currentTimeMillis();
            this.lastAccessedTime = this.thisAccessedTime;
        }
        if ( ACTIVITY_CHECK ) {
            accessCount.decrementAndGet();
        }
    }
    @Override
    public void addSessionListener ( SessionListener listener ) {
        listeners.add ( listener );
    }
    @Override
    public void expire() {
        expire ( true );
    }
    public void expire ( boolean notify ) {
        if ( !isValid ) {
            return;
        }
        synchronized ( this ) {
            if ( expiring || !isValid ) {
                return;
            }
            if ( manager == null ) {
                return;
            }
            expiring = true;
            Context context = manager.getContext();
            if ( notify ) {
                ClassLoader oldContextClassLoader = null;
                try {
                    oldContextClassLoader = context.bind ( Globals.IS_SECURITY_ENABLED, null );
                    Object listeners[] = context.getApplicationLifecycleListeners();
                    if ( listeners != null && listeners.length > 0 ) {
                        HttpSessionEvent event =
                            new HttpSessionEvent ( getSession() );
                        for ( int i = 0; i < listeners.length; i++ ) {
                            int j = ( listeners.length - 1 ) - i;
                            if ( ! ( listeners[j] instanceof HttpSessionListener ) ) {
                                continue;
                            }
                            HttpSessionListener listener =
                                ( HttpSessionListener ) listeners[j];
                            try {
                                context.fireContainerEvent ( "beforeSessionDestroyed",
                                                             listener );
                                listener.sessionDestroyed ( event );
                                context.fireContainerEvent ( "afterSessionDestroyed",
                                                             listener );
                            } catch ( Throwable t ) {
                                ExceptionUtils.handleThrowable ( t );
                                try {
                                    context.fireContainerEvent (
                                        "afterSessionDestroyed", listener );
                                } catch ( Exception e ) {
                                }
                                manager.getContext().getLogger().error
                                ( sm.getString ( "standardSession.sessionEvent" ), t );
                            }
                        }
                    }
                } finally {
                    context.unbind ( Globals.IS_SECURITY_ENABLED, oldContextClassLoader );
                }
            }
            if ( ACTIVITY_CHECK ) {
                accessCount.set ( 0 );
            }
            manager.remove ( this, true );
            if ( notify ) {
                fireSessionEvent ( Session.SESSION_DESTROYED_EVENT, null );
            }
            if ( principal instanceof TomcatPrincipal ) {
                TomcatPrincipal gp = ( TomcatPrincipal ) principal;
                try {
                    gp.logout();
                } catch ( Exception e ) {
                    manager.getContext().getLogger().error (
                        sm.getString ( "standardSession.logoutfail" ),
                        e );
                }
            }
            setValid ( false );
            expiring = false;
            String keys[] = keys();
            ClassLoader oldContextClassLoader = null;
            try {
                oldContextClassLoader = context.bind ( Globals.IS_SECURITY_ENABLED, null );
                for ( int i = 0; i < keys.length; i++ ) {
                    removeAttributeInternal ( keys[i], notify );
                }
            } finally {
                context.unbind ( Globals.IS_SECURITY_ENABLED, oldContextClassLoader );
            }
        }
    }
    public void passivate() {
        fireSessionEvent ( Session.SESSION_PASSIVATED_EVENT, null );
        HttpSessionEvent event = null;
        String keys[] = keys();
        for ( int i = 0; i < keys.length; i++ ) {
            Object attribute = attributes.get ( keys[i] );
            if ( attribute instanceof HttpSessionActivationListener ) {
                if ( event == null ) {
                    event = new HttpSessionEvent ( getSession() );
                }
                try {
                    ( ( HttpSessionActivationListener ) attribute )
                    .sessionWillPassivate ( event );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    manager.getContext().getLogger().error
                    ( sm.getString ( "standardSession.attributeEvent" ), t );
                }
            }
        }
    }
    public void activate() {
        if ( ACTIVITY_CHECK ) {
            accessCount = new AtomicInteger();
        }
        fireSessionEvent ( Session.SESSION_ACTIVATED_EVENT, null );
        HttpSessionEvent event = null;
        String keys[] = keys();
        for ( int i = 0; i < keys.length; i++ ) {
            Object attribute = attributes.get ( keys[i] );
            if ( attribute instanceof HttpSessionActivationListener ) {
                if ( event == null ) {
                    event = new HttpSessionEvent ( getSession() );
                }
                try {
                    ( ( HttpSessionActivationListener ) attribute )
                    .sessionDidActivate ( event );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    manager.getContext().getLogger().error
                    ( sm.getString ( "standardSession.attributeEvent" ), t );
                }
            }
        }
    }
    @Override
    public Object getNote ( String name ) {
        return ( notes.get ( name ) );
    }
    @Override
    public Iterator<String> getNoteNames() {
        return ( notes.keySet().iterator() );
    }
    @Override
    public void recycle() {
        attributes.clear();
        setAuthType ( null );
        creationTime = 0L;
        expiring = false;
        id = null;
        lastAccessedTime = 0L;
        maxInactiveInterval = -1;
        notes.clear();
        setPrincipal ( null );
        isNew = false;
        isValid = false;
        manager = null;
    }
    @Override
    public void removeNote ( String name ) {
        notes.remove ( name );
    }
    @Override
    public void removeSessionListener ( SessionListener listener ) {
        listeners.remove ( listener );
    }
    @Override
    public void setNote ( String name, Object value ) {
        notes.put ( name, value );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append ( "StandardSession[" );
        sb.append ( id );
        sb.append ( "]" );
        return ( sb.toString() );
    }
    public void readObjectData ( ObjectInputStream stream )
    throws ClassNotFoundException, IOException {
        doReadObject ( stream );
    }
    public void writeObjectData ( ObjectOutputStream stream )
    throws IOException {
        doWriteObject ( stream );
    }
    @Override
    public long getCreationTime() {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getCreationTime.ise" ) );
        return ( this.creationTime );
    }
    @Override
    public long getCreationTimeInternal() {
        return this.creationTime;
    }
    @Override
    public ServletContext getServletContext() {
        if ( manager == null ) {
            return null;
        }
        Context context = manager.getContext();
        return context.getServletContext();
    }
    @Override
    @Deprecated
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        if ( sessionContext == null ) {
            sessionContext = new StandardSessionContext();
        }
        return ( sessionContext );
    }
    @Override
    public Object getAttribute ( String name ) {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getAttribute.ise" ) );
        if ( name == null ) {
            return null;
        }
        return ( attributes.get ( name ) );
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getAttributeNames.ise" ) );
        Set<String> names = new HashSet<>();
        names.addAll ( attributes.keySet() );
        return Collections.enumeration ( names );
    }
    @Override
    @Deprecated
    public Object getValue ( String name ) {
        return ( getAttribute ( name ) );
    }
    @Override
    @Deprecated
    public String[] getValueNames() {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.getValueNames.ise" ) );
        return ( keys() );
    }
    @Override
    public void invalidate() {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.invalidate.ise" ) );
        expire();
    }
    @Override
    public boolean isNew() {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.isNew.ise" ) );
        return ( this.isNew );
    }
    @Override
    @Deprecated
    public void putValue ( String name, Object value ) {
        setAttribute ( name, value );
    }
    @Override
    public void removeAttribute ( String name ) {
        removeAttribute ( name, true );
    }
    public void removeAttribute ( String name, boolean notify ) {
        if ( !isValidInternal() )
            throw new IllegalStateException
            ( sm.getString ( "standardSession.removeAttribute.ise" ) );
        removeAttributeInternal ( name, notify );
    }
    @Override
    @Deprecated
    public void removeValue ( String name ) {
        removeAttribute ( name );
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        setAttribute ( name, value, true );
    }
    public void setAttribute ( String name, Object value, boolean notify ) {
        if ( name == null )
            throw new IllegalArgumentException
            ( sm.getString ( "standardSession.setAttribute.namenull" ) );
        if ( value == null ) {
            removeAttribute ( name );
            return;
        }
        if ( !isValidInternal() ) {
            throw new IllegalStateException ( sm.getString (
                                                  "standardSession.setAttribute.ise", getIdInternal() ) );
        }
        if ( ( manager != null ) && manager.getContext().getDistributable() &&
                !isAttributeDistributable ( name, value ) && !exclude ( name, value ) ) {
            throw new IllegalArgumentException ( sm.getString (
                    "standardSession.setAttribute.iae", name ) );
        }
        HttpSessionBindingEvent event = null;
        if ( notify && value instanceof HttpSessionBindingListener ) {
            Object oldValue = attributes.get ( name );
            if ( value != oldValue ) {
                event = new HttpSessionBindingEvent ( getSession(), name, value );
                try {
                    ( ( HttpSessionBindingListener ) value ).valueBound ( event );
                } catch ( Throwable t ) {
                    manager.getContext().getLogger().error
                    ( sm.getString ( "standardSession.bindingEvent" ), t );
                }
            }
        }
        Object unbound = attributes.put ( name, value );
        if ( notify && ( unbound != null ) && ( unbound != value ) &&
                ( unbound instanceof HttpSessionBindingListener ) ) {
            try {
                ( ( HttpSessionBindingListener ) unbound ).valueUnbound
                ( new HttpSessionBindingEvent ( getSession(), name ) );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                manager.getContext().getLogger().error
                ( sm.getString ( "standardSession.bindingEvent" ), t );
            }
        }
        if ( !notify ) {
            return;
        }
        Context context = manager.getContext();
        Object listeners[] = context.getApplicationEventListeners();
        if ( listeners == null ) {
            return;
        }
        for ( int i = 0; i < listeners.length; i++ ) {
            if ( ! ( listeners[i] instanceof HttpSessionAttributeListener ) ) {
                continue;
            }
            HttpSessionAttributeListener listener =
                ( HttpSessionAttributeListener ) listeners[i];
            try {
                if ( unbound != null ) {
                    context.fireContainerEvent ( "beforeSessionAttributeReplaced",
                                                 listener );
                    if ( event == null ) {
                        event = new HttpSessionBindingEvent
                        ( getSession(), name, unbound );
                    }
                    listener.attributeReplaced ( event );
                    context.fireContainerEvent ( "afterSessionAttributeReplaced",
                                                 listener );
                } else {
                    context.fireContainerEvent ( "beforeSessionAttributeAdded",
                                                 listener );
                    if ( event == null ) {
                        event = new HttpSessionBindingEvent
                        ( getSession(), name, value );
                    }
                    listener.attributeAdded ( event );
                    context.fireContainerEvent ( "afterSessionAttributeAdded",
                                                 listener );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                try {
                    if ( unbound != null ) {
                        context.fireContainerEvent (
                            "afterSessionAttributeReplaced", listener );
                    } else {
                        context.fireContainerEvent ( "afterSessionAttributeAdded",
                                                     listener );
                    }
                } catch ( Exception e ) {
                }
                manager.getContext().getLogger().error
                ( sm.getString ( "standardSession.attributeEvent" ), t );
            }
        }
    }
    protected boolean isValidInternal() {
        return this.isValid;
    }
    @Override
    public boolean isAttributeDistributable ( String name, Object value ) {
        return value instanceof Serializable;
    }
    protected void doReadObject ( ObjectInputStream stream )
    throws ClassNotFoundException, IOException {
        authType = null;
        creationTime = ( ( Long ) stream.readObject() ).longValue();
        lastAccessedTime = ( ( Long ) stream.readObject() ).longValue();
        maxInactiveInterval = ( ( Integer ) stream.readObject() ).intValue();
        isNew = ( ( Boolean ) stream.readObject() ).booleanValue();
        isValid = ( ( Boolean ) stream.readObject() ).booleanValue();
        thisAccessedTime = ( ( Long ) stream.readObject() ).longValue();
        principal = null;
        id = ( String ) stream.readObject();
        if ( manager.getContext().getLogger().isDebugEnabled() )
            manager.getContext().getLogger().debug
            ( "readObject() loading session " + id );
        if ( attributes == null ) {
            attributes = new ConcurrentHashMap<>();
        }
        int n = ( ( Integer ) stream.readObject() ).intValue();
        boolean isValidSave = isValid;
        isValid = true;
        for ( int i = 0; i < n; i++ ) {
            String name = ( String ) stream.readObject();
            final Object value;
            try {
                value = stream.readObject();
            } catch ( WriteAbortedException wae ) {
                if ( wae.getCause() instanceof NotSerializableException ) {
                    String msg = sm.getString ( "standardSession.notDeserializable", name, id );
                    if ( manager.getContext().getLogger().isDebugEnabled() ) {
                        manager.getContext().getLogger().debug ( msg, wae );
                    } else {
                        manager.getContext().getLogger().warn ( msg );
                    }
                    continue;
                }
                throw wae;
            }
            if ( manager.getContext().getLogger().isDebugEnabled() )
                manager.getContext().getLogger().debug ( "  loading attribute '" + name +
                        "' with value '" + value + "'" );
            if ( exclude ( name, value ) ) {
                continue;
            }
            attributes.put ( name, value );
        }
        isValid = isValidSave;
        if ( listeners == null ) {
            listeners = new ArrayList<>();
        }
        if ( notes == null ) {
            notes = new Hashtable<>();
        }
    }
    protected void doWriteObject ( ObjectOutputStream stream ) throws IOException {
        stream.writeObject ( Long.valueOf ( creationTime ) );
        stream.writeObject ( Long.valueOf ( lastAccessedTime ) );
        stream.writeObject ( Integer.valueOf ( maxInactiveInterval ) );
        stream.writeObject ( Boolean.valueOf ( isNew ) );
        stream.writeObject ( Boolean.valueOf ( isValid ) );
        stream.writeObject ( Long.valueOf ( thisAccessedTime ) );
        stream.writeObject ( id );
        if ( manager.getContext().getLogger().isDebugEnabled() )
            manager.getContext().getLogger().debug
            ( "writeObject() storing session " + id );
        String keys[] = keys();
        ArrayList<String> saveNames = new ArrayList<>();
        ArrayList<Object> saveValues = new ArrayList<>();
        for ( int i = 0; i < keys.length; i++ ) {
            Object value = attributes.get ( keys[i] );
            if ( value == null ) {
                continue;
            } else if ( isAttributeDistributable ( keys[i], value ) && !exclude ( keys[i], value ) ) {
                saveNames.add ( keys[i] );
                saveValues.add ( value );
            } else {
                removeAttributeInternal ( keys[i], true );
            }
        }
        int n = saveNames.size();
        stream.writeObject ( Integer.valueOf ( n ) );
        for ( int i = 0; i < n; i++ ) {
            stream.writeObject ( saveNames.get ( i ) );
            try {
                stream.writeObject ( saveValues.get ( i ) );
                if ( manager.getContext().getLogger().isDebugEnabled() )
                    manager.getContext().getLogger().debug (
                        "  storing attribute '" + saveNames.get ( i ) + "' with value '" + saveValues.get ( i ) + "'" );
            } catch ( NotSerializableException e ) {
                manager.getContext().getLogger().warn (
                    sm.getString ( "standardSession.notSerializable", saveNames.get ( i ), id ), e );
            }
        }
    }
    protected boolean exclude ( String name, Object value ) {
        if ( Constants.excludedAttributeNames.contains ( name ) ) {
            return true;
        }
        Manager manager = getManager();
        if ( manager == null ) {
            return false;
        }
        return !manager.willAttributeDistribute ( name, value );
    }
    public void fireSessionEvent ( String type, Object data ) {
        if ( listeners.size() < 1 ) {
            return;
        }
        SessionEvent event = new SessionEvent ( this, type, data );
        SessionListener list[] = new SessionListener[0];
        synchronized ( listeners ) {
            list = listeners.toArray ( list );
        }
        for ( int i = 0; i < list.length; i++ ) {
            ( list[i] ).sessionEvent ( event );
        }
    }
    protected String[] keys() {
        return attributes.keySet().toArray ( EMPTY_ARRAY );
    }
    protected void removeAttributeInternal ( String name, boolean notify ) {
        if ( name == null ) {
            return;
        }
        Object value = attributes.remove ( name );
        if ( !notify || ( value == null ) ) {
            return;
        }
        HttpSessionBindingEvent event = null;
        if ( value instanceof HttpSessionBindingListener ) {
            event = new HttpSessionBindingEvent ( getSession(), name, value );
            ( ( HttpSessionBindingListener ) value ).valueUnbound ( event );
        }
        Context context = manager.getContext();
        Object listeners[] = context.getApplicationEventListeners();
        if ( listeners == null ) {
            return;
        }
        for ( int i = 0; i < listeners.length; i++ ) {
            if ( ! ( listeners[i] instanceof HttpSessionAttributeListener ) ) {
                continue;
            }
            HttpSessionAttributeListener listener =
                ( HttpSessionAttributeListener ) listeners[i];
            try {
                context.fireContainerEvent ( "beforeSessionAttributeRemoved",
                                             listener );
                if ( event == null ) {
                    event = new HttpSessionBindingEvent
                    ( getSession(), name, value );
                }
                listener.attributeRemoved ( event );
                context.fireContainerEvent ( "afterSessionAttributeRemoved",
                                             listener );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                try {
                    context.fireContainerEvent ( "afterSessionAttributeRemoved",
                                                 listener );
                } catch ( Exception e ) {
                }
                manager.getContext().getLogger().error
                ( sm.getString ( "standardSession.attributeEvent" ), t );
            }
        }
    }
}
@Deprecated
final class StandardSessionContext
    implements javax.servlet.http.HttpSessionContext {
    private static final List<String> emptyString = Collections.emptyList();
    @Override
    @Deprecated
    public Enumeration<String> getIds() {
        return Collections.enumeration ( emptyString );
    }
    @Override
    @Deprecated
    public HttpSession getSession ( String id ) {
        return ( null );
    }
}

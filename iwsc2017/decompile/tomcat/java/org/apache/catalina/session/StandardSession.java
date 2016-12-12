package org.apache.catalina.session;
import org.apache.catalina.SessionEvent;
import java.io.WriteAbortedException;
import java.io.NotSerializableException;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import javax.servlet.http.HttpSessionActivationListener;
import org.apache.catalina.TomcatPrincipal;
import org.apache.catalina.Globals;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.catalina.security.SecurityUtil;
import javax.servlet.http.HttpSessionIdListener;
import org.apache.catalina.Context;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.beans.PropertyChangeSupport;
import javax.servlet.http.HttpSessionContext;
import org.apache.tomcat.util.res.StringManager;
import java.security.Principal;
import java.util.Map;
import org.apache.catalina.Manager;
import org.apache.catalina.SessionListener;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.io.Serializable;
import org.apache.catalina.Session;
import javax.servlet.http.HttpSession;
public class StandardSession implements HttpSession, Session, Serializable {
    private static final long serialVersionUID = 1L;
    protected static final boolean STRICT_SERVLET_COMPLIANCE;
    protected static final boolean ACTIVITY_CHECK;
    protected static final boolean LAST_ACCESS_AT_START;
    protected static final String[] EMPTY_ARRAY;
    protected ConcurrentMap<String, Object> attributes;
    protected transient String authType;
    protected long creationTime;
    protected transient volatile boolean expiring;
    protected transient StandardSessionFacade facade;
    protected String id;
    protected volatile long lastAccessedTime;
    protected transient ArrayList<SessionListener> listeners;
    protected transient Manager manager;
    protected volatile int maxInactiveInterval;
    protected volatile boolean isNew;
    protected volatile boolean isValid;
    protected transient Map<String, Object> notes;
    protected transient Principal principal;
    protected static final StringManager sm;
    @Deprecated
    protected static volatile HttpSessionContext sessionContext;
    protected final transient PropertyChangeSupport support;
    protected volatile long thisAccessedTime;
    protected transient AtomicInteger accessCount;
    public StandardSession ( final Manager manager ) {
        this.attributes = new ConcurrentHashMap<String, Object>();
        this.authType = null;
        this.creationTime = 0L;
        this.expiring = false;
        this.facade = null;
        this.id = null;
        this.lastAccessedTime = this.creationTime;
        this.listeners = new ArrayList<SessionListener>();
        this.manager = null;
        this.maxInactiveInterval = -1;
        this.isNew = false;
        this.isValid = false;
        this.notes = new Hashtable<String, Object>();
        this.principal = null;
        this.support = new PropertyChangeSupport ( this );
        this.thisAccessedTime = this.creationTime;
        this.accessCount = null;
        this.manager = manager;
        if ( StandardSession.ACTIVITY_CHECK ) {
            this.accessCount = new AtomicInteger();
        }
    }
    public String getAuthType() {
        return this.authType;
    }
    public void setAuthType ( final String authType ) {
        final String oldAuthType = this.authType;
        this.authType = authType;
        this.support.firePropertyChange ( "authType", oldAuthType, this.authType );
    }
    public void setCreationTime ( final long time ) {
        this.creationTime = time;
        this.lastAccessedTime = time;
        this.thisAccessedTime = time;
    }
    public String getId() {
        return this.id;
    }
    public String getIdInternal() {
        return this.id;
    }
    public void setId ( final String id ) {
        this.setId ( id, true );
    }
    public void setId ( final String id, final boolean notify ) {
        if ( this.id != null && this.manager != null ) {
            this.manager.remove ( this );
        }
        this.id = id;
        if ( this.manager != null ) {
            this.manager.add ( this );
        }
        if ( notify ) {
            this.tellNew();
        }
    }
    public void tellNew() {
        this.fireSessionEvent ( "createSession", null );
        final Context context = this.manager.getContext();
        final Object[] listeners = context.getApplicationLifecycleListeners();
        if ( listeners != null && listeners.length > 0 ) {
            final HttpSessionEvent event = new HttpSessionEvent ( this.getSession() );
            for ( int i = 0; i < listeners.length; ++i ) {
                if ( listeners[i] instanceof HttpSessionListener ) {
                    final HttpSessionListener listener = ( HttpSessionListener ) listeners[i];
                    try {
                        context.fireContainerEvent ( "beforeSessionCreated", listener );
                        listener.sessionCreated ( event );
                        context.fireContainerEvent ( "afterSessionCreated", listener );
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                        try {
                            context.fireContainerEvent ( "afterSessionCreated", listener );
                        } catch ( Exception ex ) {}
                        this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.sessionEvent" ), t );
                    }
                }
            }
        }
    }
    public void tellChangedSessionId ( final String newId, final String oldId, final boolean notifySessionListeners, final boolean notifyContainerListeners ) {
        final Context context = this.manager.getContext();
        if ( notifyContainerListeners ) {
            context.fireContainerEvent ( "changeSessionId", new String[] { oldId, newId } );
        }
        if ( notifySessionListeners ) {
            final Object[] listeners = context.getApplicationEventListeners();
            if ( listeners != null && listeners.length > 0 ) {
                final HttpSessionEvent event = new HttpSessionEvent ( this.getSession() );
                for ( final Object listener : listeners ) {
                    if ( listener instanceof HttpSessionIdListener ) {
                        final HttpSessionIdListener idListener = ( HttpSessionIdListener ) listener;
                        try {
                            idListener.sessionIdChanged ( event, oldId );
                        } catch ( Throwable t ) {
                            this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.sessionEvent" ), t );
                        }
                    }
                }
            }
        }
    }
    public long getThisAccessedTime() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getThisAccessedTime.ise" ) );
        }
        return this.thisAccessedTime;
    }
    public long getThisAccessedTimeInternal() {
        return this.thisAccessedTime;
    }
    public long getLastAccessedTime() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getLastAccessedTime.ise" ) );
        }
        return this.lastAccessedTime;
    }
    public long getLastAccessedTimeInternal() {
        return this.lastAccessedTime;
    }
    public long getIdleTime() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getIdleTime.ise" ) );
        }
        return this.getIdleTimeInternal();
    }
    public long getIdleTimeInternal() {
        final long timeNow = System.currentTimeMillis();
        long timeIdle;
        if ( StandardSession.LAST_ACCESS_AT_START ) {
            timeIdle = timeNow - this.lastAccessedTime;
        } else {
            timeIdle = timeNow - this.thisAccessedTime;
        }
        return timeIdle;
    }
    public Manager getManager() {
        return this.manager;
    }
    public void setManager ( final Manager manager ) {
        this.manager = manager;
    }
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }
    public void setMaxInactiveInterval ( final int interval ) {
        this.maxInactiveInterval = interval;
    }
    public void setNew ( final boolean isNew ) {
        this.isNew = isNew;
    }
    public Principal getPrincipal() {
        return this.principal;
    }
    public void setPrincipal ( final Principal principal ) {
        final Principal oldPrincipal = this.principal;
        this.principal = principal;
        this.support.firePropertyChange ( "principal", oldPrincipal, this.principal );
    }
    public HttpSession getSession() {
        if ( this.facade == null ) {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                this.facade = AccessController.doPrivileged ( ( PrivilegedAction<StandardSessionFacade> ) new PrivilegedAction<StandardSessionFacade>() {
                    @Override
                    public StandardSessionFacade run() {
                        return new StandardSessionFacade ( ( HttpSession ) StandardSession.this );
                    }
                } );
            } else {
                this.facade = new StandardSessionFacade ( ( HttpSession ) this );
            }
        }
        return ( HttpSession ) this.facade;
    }
    public boolean isValid() {
        if ( !this.isValid ) {
            return false;
        }
        if ( this.expiring ) {
            return true;
        }
        if ( StandardSession.ACTIVITY_CHECK && this.accessCount.get() > 0 ) {
            return true;
        }
        if ( this.maxInactiveInterval > 0 ) {
            final int timeIdle = ( int ) ( this.getIdleTimeInternal() / 1000L );
            if ( timeIdle >= this.maxInactiveInterval ) {
                this.expire ( true );
            }
        }
        return this.isValid;
    }
    public void setValid ( final boolean isValid ) {
        this.isValid = isValid;
    }
    public void access() {
        this.thisAccessedTime = System.currentTimeMillis();
        if ( StandardSession.ACTIVITY_CHECK ) {
            this.accessCount.incrementAndGet();
        }
    }
    public void endAccess() {
        this.isNew = false;
        if ( StandardSession.LAST_ACCESS_AT_START ) {
            this.lastAccessedTime = this.thisAccessedTime;
            this.thisAccessedTime = System.currentTimeMillis();
        } else {
            this.thisAccessedTime = System.currentTimeMillis();
            this.lastAccessedTime = this.thisAccessedTime;
        }
        if ( StandardSession.ACTIVITY_CHECK ) {
            this.accessCount.decrementAndGet();
        }
    }
    public void addSessionListener ( final SessionListener listener ) {
        this.listeners.add ( listener );
    }
    public void expire() {
        this.expire ( true );
    }
    public void expire ( final boolean notify ) {
        if ( !this.isValid ) {
            return;
        }
        synchronized ( this ) {
            if ( this.expiring || !this.isValid ) {
                return;
            }
            if ( this.manager == null ) {
                return;
            }
            this.expiring = true;
            final Context context = this.manager.getContext();
            if ( notify ) {
                ClassLoader oldContextClassLoader = null;
                try {
                    oldContextClassLoader = context.bind ( Globals.IS_SECURITY_ENABLED, null );
                    final Object[] listeners = context.getApplicationLifecycleListeners();
                    if ( listeners != null && listeners.length > 0 ) {
                        final HttpSessionEvent event = new HttpSessionEvent ( this.getSession() );
                        for ( int i = 0; i < listeners.length; ++i ) {
                            final int j = listeners.length - 1 - i;
                            if ( listeners[j] instanceof HttpSessionListener ) {
                                final HttpSessionListener listener = ( HttpSessionListener ) listeners[j];
                                try {
                                    context.fireContainerEvent ( "beforeSessionDestroyed", listener );
                                    listener.sessionDestroyed ( event );
                                    context.fireContainerEvent ( "afterSessionDestroyed", listener );
                                } catch ( Throwable t ) {
                                    ExceptionUtils.handleThrowable ( t );
                                    try {
                                        context.fireContainerEvent ( "afterSessionDestroyed", listener );
                                    } catch ( Exception ex ) {}
                                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.sessionEvent" ), t );
                                }
                            }
                        }
                    }
                } finally {
                    context.unbind ( Globals.IS_SECURITY_ENABLED, oldContextClassLoader );
                }
            }
            if ( StandardSession.ACTIVITY_CHECK ) {
                this.accessCount.set ( 0 );
            }
            this.manager.remove ( this, true );
            if ( notify ) {
                this.fireSessionEvent ( "destroySession", null );
            }
            if ( this.principal instanceof TomcatPrincipal ) {
                final TomcatPrincipal gp = ( TomcatPrincipal ) this.principal;
                try {
                    gp.logout();
                } catch ( Exception e ) {
                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.logoutfail" ), e );
                }
            }
            this.setValid ( false );
            this.expiring = false;
            final String[] keys = this.keys();
            ClassLoader oldContextClassLoader2 = null;
            try {
                oldContextClassLoader2 = context.bind ( Globals.IS_SECURITY_ENABLED, null );
                for ( int k = 0; k < keys.length; ++k ) {
                    this.removeAttributeInternal ( keys[k], notify );
                }
            } finally {
                context.unbind ( Globals.IS_SECURITY_ENABLED, oldContextClassLoader2 );
            }
        }
    }
    public void passivate() {
        this.fireSessionEvent ( "passivateSession", null );
        HttpSessionEvent event = null;
        final String[] keys = this.keys();
        for ( int i = 0; i < keys.length; ++i ) {
            final Object attribute = this.attributes.get ( keys[i] );
            if ( attribute instanceof HttpSessionActivationListener ) {
                if ( event == null ) {
                    event = new HttpSessionEvent ( this.getSession() );
                }
                try {
                    ( ( HttpSessionActivationListener ) attribute ).sessionWillPassivate ( event );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.attributeEvent" ), t );
                }
            }
        }
    }
    public void activate() {
        if ( StandardSession.ACTIVITY_CHECK ) {
            this.accessCount = new AtomicInteger();
        }
        this.fireSessionEvent ( "activateSession", null );
        HttpSessionEvent event = null;
        final String[] keys = this.keys();
        for ( int i = 0; i < keys.length; ++i ) {
            final Object attribute = this.attributes.get ( keys[i] );
            if ( attribute instanceof HttpSessionActivationListener ) {
                if ( event == null ) {
                    event = new HttpSessionEvent ( this.getSession() );
                }
                try {
                    ( ( HttpSessionActivationListener ) attribute ).sessionDidActivate ( event );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.attributeEvent" ), t );
                }
            }
        }
    }
    public Object getNote ( final String name ) {
        return this.notes.get ( name );
    }
    public Iterator<String> getNoteNames() {
        return this.notes.keySet().iterator();
    }
    public void recycle() {
        this.attributes.clear();
        this.setAuthType ( null );
        this.creationTime = 0L;
        this.expiring = false;
        this.id = null;
        this.lastAccessedTime = 0L;
        this.maxInactiveInterval = -1;
        this.notes.clear();
        this.setPrincipal ( null );
        this.isNew = false;
        this.isValid = false;
        this.manager = null;
    }
    public void removeNote ( final String name ) {
        this.notes.remove ( name );
    }
    public void removeSessionListener ( final SessionListener listener ) {
        this.listeners.remove ( listener );
    }
    public void setNote ( final String name, final Object value ) {
        this.notes.put ( name, value );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "StandardSession[" );
        sb.append ( this.id );
        sb.append ( "]" );
        return sb.toString();
    }
    public void readObjectData ( final ObjectInputStream stream ) throws ClassNotFoundException, IOException {
        this.doReadObject ( stream );
    }
    public void writeObjectData ( final ObjectOutputStream stream ) throws IOException {
        this.doWriteObject ( stream );
    }
    public long getCreationTime() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getCreationTime.ise" ) );
        }
        return this.creationTime;
    }
    public long getCreationTimeInternal() {
        return this.creationTime;
    }
    public ServletContext getServletContext() {
        if ( this.manager == null ) {
            return null;
        }
        final Context context = this.manager.getContext();
        return context.getServletContext();
    }
    @Deprecated
    public HttpSessionContext getSessionContext() {
        if ( StandardSession.sessionContext == null ) {
            StandardSession.sessionContext = ( HttpSessionContext ) new StandardSessionContext();
        }
        return StandardSession.sessionContext;
    }
    public Object getAttribute ( final String name ) {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getAttribute.ise" ) );
        }
        if ( name == null ) {
            return null;
        }
        return this.attributes.get ( name );
    }
    public Enumeration<String> getAttributeNames() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getAttributeNames.ise" ) );
        }
        final Set<String> names = new HashSet<String>();
        names.addAll ( ( Collection<? extends String> ) this.attributes.keySet() );
        return Collections.enumeration ( names );
    }
    @Deprecated
    public Object getValue ( final String name ) {
        return this.getAttribute ( name );
    }
    @Deprecated
    public String[] getValueNames() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.getValueNames.ise" ) );
        }
        return this.keys();
    }
    public void invalidate() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.invalidate.ise" ) );
        }
        this.expire();
    }
    public boolean isNew() {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.isNew.ise" ) );
        }
        return this.isNew;
    }
    @Deprecated
    public void putValue ( final String name, final Object value ) {
        this.setAttribute ( name, value );
    }
    public void removeAttribute ( final String name ) {
        this.removeAttribute ( name, true );
    }
    public void removeAttribute ( final String name, final boolean notify ) {
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.removeAttribute.ise" ) );
        }
        this.removeAttributeInternal ( name, notify );
    }
    @Deprecated
    public void removeValue ( final String name ) {
        this.removeAttribute ( name );
    }
    public void setAttribute ( final String name, final Object value ) {
        this.setAttribute ( name, value, true );
    }
    public void setAttribute ( final String name, final Object value, final boolean notify ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( StandardSession.sm.getString ( "standardSession.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            this.removeAttribute ( name );
            return;
        }
        if ( !this.isValidInternal() ) {
            throw new IllegalStateException ( StandardSession.sm.getString ( "standardSession.setAttribute.ise", this.getIdInternal() ) );
        }
        if ( this.manager != null && this.manager.getContext().getDistributable() && !this.isAttributeDistributable ( name, value ) && !this.exclude ( name, value ) ) {
            throw new IllegalArgumentException ( StandardSession.sm.getString ( "standardSession.setAttribute.iae", name ) );
        }
        HttpSessionBindingEvent event = null;
        if ( notify && value instanceof HttpSessionBindingListener ) {
            final Object oldValue = this.attributes.get ( name );
            if ( value != oldValue ) {
                event = new HttpSessionBindingEvent ( this.getSession(), name, value );
                try {
                    ( ( HttpSessionBindingListener ) value ).valueBound ( event );
                } catch ( Throwable t ) {
                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.bindingEvent" ), t );
                }
            }
        }
        final Object unbound = this.attributes.put ( name, value );
        if ( notify && unbound != null && unbound != value && unbound instanceof HttpSessionBindingListener ) {
            try {
                ( ( HttpSessionBindingListener ) unbound ).valueUnbound ( new HttpSessionBindingEvent ( this.getSession(), name ) );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.bindingEvent" ), t );
            }
        }
        if ( !notify ) {
            return;
        }
        final Context context = this.manager.getContext();
        final Object[] listeners = context.getApplicationEventListeners();
        if ( listeners == null ) {
            return;
        }
        for ( int i = 0; i < listeners.length; ++i ) {
            if ( listeners[i] instanceof HttpSessionAttributeListener ) {
                final HttpSessionAttributeListener listener = ( HttpSessionAttributeListener ) listeners[i];
                try {
                    if ( unbound != null ) {
                        context.fireContainerEvent ( "beforeSessionAttributeReplaced", listener );
                        if ( event == null ) {
                            event = new HttpSessionBindingEvent ( this.getSession(), name, unbound );
                        }
                        listener.attributeReplaced ( event );
                        context.fireContainerEvent ( "afterSessionAttributeReplaced", listener );
                    } else {
                        context.fireContainerEvent ( "beforeSessionAttributeAdded", listener );
                        if ( event == null ) {
                            event = new HttpSessionBindingEvent ( this.getSession(), name, value );
                        }
                        listener.attributeAdded ( event );
                        context.fireContainerEvent ( "afterSessionAttributeAdded", listener );
                    }
                } catch ( Throwable t2 ) {
                    ExceptionUtils.handleThrowable ( t2 );
                    try {
                        if ( unbound != null ) {
                            context.fireContainerEvent ( "afterSessionAttributeReplaced", listener );
                        } else {
                            context.fireContainerEvent ( "afterSessionAttributeAdded", listener );
                        }
                    } catch ( Exception ex ) {}
                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.attributeEvent" ), t2 );
                }
            }
        }
    }
    protected boolean isValidInternal() {
        return this.isValid;
    }
    public boolean isAttributeDistributable ( final String name, final Object value ) {
        return value instanceof Serializable;
    }
    protected void doReadObject ( final ObjectInputStream stream ) throws ClassNotFoundException, IOException {
        this.authType = null;
        this.creationTime = ( long ) stream.readObject();
        this.lastAccessedTime = ( long ) stream.readObject();
        this.maxInactiveInterval = ( int ) stream.readObject();
        this.isNew = ( boolean ) stream.readObject();
        this.isValid = ( boolean ) stream.readObject();
        this.thisAccessedTime = ( long ) stream.readObject();
        this.principal = null;
        this.id = ( String ) stream.readObject();
        if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
            this.manager.getContext().getLogger().debug ( "readObject() loading session " + this.id );
        }
        if ( this.attributes == null ) {
            this.attributes = new ConcurrentHashMap<String, Object>();
        }
        final int n = ( int ) stream.readObject();
        final boolean isValidSave = this.isValid;
        this.isValid = true;
        for ( int i = 0; i < n; ++i ) {
            final String name = ( String ) stream.readObject();
            Object value;
            try {
                value = stream.readObject();
            } catch ( WriteAbortedException wae ) {
                if ( wae.getCause() instanceof NotSerializableException ) {
                    final String msg = StandardSession.sm.getString ( "standardSession.notDeserializable", name, this.id );
                    if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
                        this.manager.getContext().getLogger().debug ( msg, wae );
                    } else {
                        this.manager.getContext().getLogger().warn ( msg );
                    }
                    continue;
                }
                throw wae;
            }
            if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
                this.manager.getContext().getLogger().debug ( "  loading attribute '" + name + "' with value '" + value + "'" );
            }
            if ( !this.exclude ( name, value ) ) {
                this.attributes.put ( name, value );
            }
        }
        this.isValid = isValidSave;
        if ( this.listeners == null ) {
            this.listeners = new ArrayList<SessionListener>();
        }
        if ( this.notes == null ) {
            this.notes = new Hashtable<String, Object>();
        }
    }
    protected void doWriteObject ( final ObjectOutputStream stream ) throws IOException {
        stream.writeObject ( this.creationTime );
        stream.writeObject ( this.lastAccessedTime );
        stream.writeObject ( this.maxInactiveInterval );
        stream.writeObject ( this.isNew );
        stream.writeObject ( this.isValid );
        stream.writeObject ( this.thisAccessedTime );
        stream.writeObject ( this.id );
        if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
            this.manager.getContext().getLogger().debug ( "writeObject() storing session " + this.id );
        }
        final String[] keys = this.keys();
        final ArrayList<String> saveNames = new ArrayList<String>();
        final ArrayList<Object> saveValues = new ArrayList<Object>();
        for ( int i = 0; i < keys.length; ++i ) {
            final Object value = this.attributes.get ( keys[i] );
            if ( value != null ) {
                if ( this.isAttributeDistributable ( keys[i], value ) && !this.exclude ( keys[i], value ) ) {
                    saveNames.add ( keys[i] );
                    saveValues.add ( value );
                } else {
                    this.removeAttributeInternal ( keys[i], true );
                }
            }
        }
        final int n = saveNames.size();
        stream.writeObject ( n );
        for ( int j = 0; j < n; ++j ) {
            stream.writeObject ( saveNames.get ( j ) );
            try {
                stream.writeObject ( saveValues.get ( j ) );
                if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
                    this.manager.getContext().getLogger().debug ( "  storing attribute '" + saveNames.get ( j ) + "' with value '" + saveValues.get ( j ) + "'" );
                }
            } catch ( NotSerializableException e ) {
                this.manager.getContext().getLogger().warn ( StandardSession.sm.getString ( "standardSession.notSerializable", saveNames.get ( j ), this.id ), e );
            }
        }
    }
    protected boolean exclude ( final String name, final Object value ) {
        if ( Constants.excludedAttributeNames.contains ( name ) ) {
            return true;
        }
        final Manager manager = this.getManager();
        return manager != null && !manager.willAttributeDistribute ( name, value );
    }
    public void fireSessionEvent ( final String type, final Object data ) {
        if ( this.listeners.size() < 1 ) {
            return;
        }
        final SessionEvent event = new SessionEvent ( this, type, data );
        SessionListener[] list = new SessionListener[0];
        synchronized ( this.listeners ) {
            list = this.listeners.toArray ( list );
        }
        for ( int i = 0; i < list.length; ++i ) {
            list[i].sessionEvent ( event );
        }
    }
    protected String[] keys() {
        return this.attributes.keySet().toArray ( StandardSession.EMPTY_ARRAY );
    }
    protected void removeAttributeInternal ( final String name, final boolean notify ) {
        if ( name == null ) {
            return;
        }
        final Object value = this.attributes.remove ( name );
        if ( !notify || value == null ) {
            return;
        }
        HttpSessionBindingEvent event = null;
        if ( value instanceof HttpSessionBindingListener ) {
            event = new HttpSessionBindingEvent ( this.getSession(), name, value );
            ( ( HttpSessionBindingListener ) value ).valueUnbound ( event );
        }
        final Context context = this.manager.getContext();
        final Object[] listeners = context.getApplicationEventListeners();
        if ( listeners == null ) {
            return;
        }
        for ( int i = 0; i < listeners.length; ++i ) {
            if ( listeners[i] instanceof HttpSessionAttributeListener ) {
                final HttpSessionAttributeListener listener = ( HttpSessionAttributeListener ) listeners[i];
                try {
                    context.fireContainerEvent ( "beforeSessionAttributeRemoved", listener );
                    if ( event == null ) {
                        event = new HttpSessionBindingEvent ( this.getSession(), name, value );
                    }
                    listener.attributeRemoved ( event );
                    context.fireContainerEvent ( "afterSessionAttributeRemoved", listener );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    try {
                        context.fireContainerEvent ( "afterSessionAttributeRemoved", listener );
                    } catch ( Exception ex ) {}
                    this.manager.getContext().getLogger().error ( StandardSession.sm.getString ( "standardSession.attributeEvent" ), t );
                }
            }
        }
    }
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        final String activityCheck = System.getProperty ( "org.apache.catalina.session.StandardSession.ACTIVITY_CHECK" );
        if ( activityCheck == null ) {
            ACTIVITY_CHECK = StandardSession.STRICT_SERVLET_COMPLIANCE;
        } else {
            ACTIVITY_CHECK = Boolean.parseBoolean ( activityCheck );
        }
        final String lastAccessAtStart = System.getProperty ( "org.apache.catalina.session.StandardSession.LAST_ACCESS_AT_START" );
        if ( lastAccessAtStart == null ) {
            LAST_ACCESS_AT_START = StandardSession.STRICT_SERVLET_COMPLIANCE;
        } else {
            LAST_ACCESS_AT_START = Boolean.parseBoolean ( lastAccessAtStart );
        }
        EMPTY_ARRAY = new String[0];
        sm = StringManager.getManager ( StandardSession.class );
        StandardSession.sessionContext = null;
    }
}

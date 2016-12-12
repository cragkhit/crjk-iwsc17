package org.apache.catalina.core;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.catalina.AccessLog;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.NullRealm;
import org.apache.catalina.util.ServerInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class StandardEngine extends ContainerBase implements Engine {
    private static final Log log = LogFactory.getLog ( StandardEngine.class );
    public StandardEngine() {
        super();
        pipeline.setBasic ( new StandardEngineValve() );
        try {
            setJvmRoute ( System.getProperty ( "jvmRoute" ) );
        } catch ( Exception ex ) {
            log.warn ( sm.getString ( "standardEngine.jvmRouteFail" ) );
        }
        backgroundProcessorDelay = 10;
    }
    private String defaultHost = null;
    private Service service = null;
    private String jvmRouteId;
    private final AtomicReference<AccessLog> defaultAccessLog =
        new AtomicReference<>();
    @Override
    public Realm getRealm() {
        Realm configured = super.getRealm();
        if ( configured == null ) {
            configured = new NullRealm();
            this.setRealm ( configured );
        }
        return configured;
    }
    @Override
    public String getDefaultHost() {
        return ( defaultHost );
    }
    @Override
    public void setDefaultHost ( String host ) {
        String oldDefaultHost = this.defaultHost;
        if ( host == null ) {
            this.defaultHost = null;
        } else {
            this.defaultHost = host.toLowerCase ( Locale.ENGLISH );
        }
        support.firePropertyChange ( "defaultHost", oldDefaultHost,
                                     this.defaultHost );
    }
    @Override
    public void setJvmRoute ( String routeId ) {
        jvmRouteId = routeId;
    }
    @Override
    public String getJvmRoute() {
        return jvmRouteId;
    }
    @Override
    public Service getService() {
        return ( this.service );
    }
    @Override
    public void setService ( Service service ) {
        this.service = service;
    }
    @Override
    public void addChild ( Container child ) {
        if ( ! ( child instanceof Host ) )
            throw new IllegalArgumentException
            ( sm.getString ( "standardEngine.notHost" ) );
        super.addChild ( child );
    }
    @Override
    public void setParent ( Container container ) {
        throw new IllegalArgumentException
        ( sm.getString ( "standardEngine.notParent" ) );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        getRealm();
        super.initInternal();
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( log.isInfoEnabled() ) {
            log.info ( "Starting Servlet Engine: " + ServerInfo.getServerInfo() );
        }
        super.startInternal();
    }
    @Override
    public void logAccess ( Request request, Response response, long time,
                            boolean useDefault ) {
        boolean logged = false;
        if ( getAccessLog() != null ) {
            accessLog.log ( request, response, time );
            logged = true;
        }
        if ( !logged && useDefault ) {
            AccessLog newDefaultAccessLog = defaultAccessLog.get();
            if ( newDefaultAccessLog == null ) {
                Host host = ( Host ) findChild ( getDefaultHost() );
                Context context = null;
                if ( host != null && host.getState().isAvailable() ) {
                    newDefaultAccessLog = host.getAccessLog();
                    if ( newDefaultAccessLog != null ) {
                        if ( defaultAccessLog.compareAndSet ( null,
                                                              newDefaultAccessLog ) ) {
                            AccessLogListener l = new AccessLogListener ( this,
                                    host, null );
                            l.install();
                        }
                    } else {
                        context = ( Context ) host.findChild ( "" );
                        if ( context != null &&
                                context.getState().isAvailable() ) {
                            newDefaultAccessLog = context.getAccessLog();
                            if ( newDefaultAccessLog != null ) {
                                if ( defaultAccessLog.compareAndSet ( null,
                                                                      newDefaultAccessLog ) ) {
                                    AccessLogListener l = new AccessLogListener (
                                        this, null, context );
                                    l.install();
                                }
                            }
                        }
                    }
                }
                if ( newDefaultAccessLog == null ) {
                    newDefaultAccessLog = new NoopAccessLog();
                    if ( defaultAccessLog.compareAndSet ( null,
                                                          newDefaultAccessLog ) ) {
                        AccessLogListener l = new AccessLogListener ( this, host,
                                context );
                        l.install();
                    }
                }
            }
            newDefaultAccessLog.log ( request, response, time );
        }
    }
    @Override
    public ClassLoader getParentClassLoader() {
        if ( parentClassLoader != null ) {
            return ( parentClassLoader );
        }
        if ( service != null ) {
            return ( service.getParentClassLoader() );
        }
        return ( ClassLoader.getSystemClassLoader() );
    }
    @Override
    public File getCatalinaBase() {
        if ( service != null ) {
            Server s = service.getServer();
            if ( s != null ) {
                File base = s.getCatalinaBase();
                if ( base != null ) {
                    return base;
                }
            }
        }
        return super.getCatalinaBase();
    }
    @Override
    public File getCatalinaHome() {
        if ( service != null ) {
            Server s = service.getServer();
            if ( s != null ) {
                File base = s.getCatalinaHome();
                if ( base != null ) {
                    return base;
                }
            }
        }
        return super.getCatalinaHome();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        return "type=Engine";
    }
    @Override
    protected String getDomainInternal() {
        return getName();
    }
    protected static final class NoopAccessLog implements AccessLog {
        @Override
        public void log ( Request request, Response response, long time ) {
        }
        @Override
        public void setRequestAttributesEnabled (
            boolean requestAttributesEnabled ) {
        }
        @Override
        public boolean getRequestAttributesEnabled() {
            return false;
        }
    }
    protected static final class AccessLogListener
        implements PropertyChangeListener, LifecycleListener,
        ContainerListener {
        private final StandardEngine engine;
        private final Host host;
        private final Context context;
        private volatile boolean disabled = false;
        public AccessLogListener ( StandardEngine engine, Host host,
                                   Context context ) {
            this.engine = engine;
            this.host = host;
            this.context = context;
        }
        public void install() {
            engine.addPropertyChangeListener ( this );
            if ( host != null ) {
                host.addContainerListener ( this );
                host.addLifecycleListener ( this );
            }
            if ( context != null ) {
                context.addLifecycleListener ( this );
            }
        }
        private void uninstall() {
            disabled = true;
            if ( context != null ) {
                context.removeLifecycleListener ( this );
            }
            if ( host != null ) {
                host.removeLifecycleListener ( this );
                host.removeContainerListener ( this );
            }
            engine.removePropertyChangeListener ( this );
        }
        @Override
        public void lifecycleEvent ( LifecycleEvent event ) {
            if ( disabled ) {
                return;
            }
            String type = event.getType();
            if ( Lifecycle.AFTER_START_EVENT.equals ( type ) ||
                    Lifecycle.BEFORE_STOP_EVENT.equals ( type ) ||
                    Lifecycle.BEFORE_DESTROY_EVENT.equals ( type ) ) {
                engine.defaultAccessLog.set ( null );
                uninstall();
            }
        }
        @Override
        public void propertyChange ( PropertyChangeEvent evt ) {
            if ( disabled ) {
                return;
            }
            if ( "defaultHost".equals ( evt.getPropertyName() ) ) {
                engine.defaultAccessLog.set ( null );
                uninstall();
            }
        }
        @Override
        public void containerEvent ( ContainerEvent event ) {
            if ( disabled ) {
                return;
            }
            if ( Container.ADD_CHILD_EVENT.equals ( event.getType() ) ) {
                Context context = ( Context ) event.getData();
                if ( "".equals ( context.getPath() ) ) {
                    engine.defaultAccessLog.set ( null );
                    uninstall();
                }
            }
        }
    }
}

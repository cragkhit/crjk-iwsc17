package org.apache.catalina.core;
import org.apache.catalina.ContainerEvent;
import java.beans.PropertyChangeEvent;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.LifecycleListener;
import java.beans.PropertyChangeListener;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.Server;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Host;
import org.apache.catalina.Container;
import java.util.Locale;
import org.apache.catalina.realm.NullRealm;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.AccessLog;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.catalina.Service;
import org.apache.juli.logging.Log;
import org.apache.catalina.Engine;
public class StandardEngine extends ContainerBase implements Engine {
    private static final Log log;
    private String defaultHost;
    private Service service;
    private String jvmRouteId;
    private final AtomicReference<AccessLog> defaultAccessLog;
    public StandardEngine() {
        this.defaultHost = null;
        this.service = null;
        this.defaultAccessLog = new AtomicReference<AccessLog>();
        this.pipeline.setBasic ( new StandardEngineValve() );
        try {
            this.setJvmRoute ( System.getProperty ( "jvmRoute" ) );
        } catch ( Exception ex ) {
            StandardEngine.log.warn ( StandardEngine.sm.getString ( "standardEngine.jvmRouteFail" ) );
        }
        this.backgroundProcessorDelay = 10;
    }
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
        return this.defaultHost;
    }
    @Override
    public void setDefaultHost ( final String host ) {
        final String oldDefaultHost = this.defaultHost;
        if ( host == null ) {
            this.defaultHost = null;
        } else {
            this.defaultHost = host.toLowerCase ( Locale.ENGLISH );
        }
        this.support.firePropertyChange ( "defaultHost", oldDefaultHost, this.defaultHost );
    }
    @Override
    public void setJvmRoute ( final String routeId ) {
        this.jvmRouteId = routeId;
    }
    @Override
    public String getJvmRoute() {
        return this.jvmRouteId;
    }
    @Override
    public Service getService() {
        return this.service;
    }
    @Override
    public void setService ( final Service service ) {
        this.service = service;
    }
    @Override
    public void addChild ( final Container child ) {
        if ( ! ( child instanceof Host ) ) {
            throw new IllegalArgumentException ( StandardEngine.sm.getString ( "standardEngine.notHost" ) );
        }
        super.addChild ( child );
    }
    @Override
    public void setParent ( final Container container ) {
        throw new IllegalArgumentException ( StandardEngine.sm.getString ( "standardEngine.notParent" ) );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        this.getRealm();
        super.initInternal();
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( StandardEngine.log.isInfoEnabled() ) {
            StandardEngine.log.info ( "Starting Servlet Engine: " + ServerInfo.getServerInfo() );
        }
        super.startInternal();
    }
    @Override
    public void logAccess ( final Request request, final Response response, final long time, final boolean useDefault ) {
        boolean logged = false;
        if ( this.getAccessLog() != null ) {
            this.accessLog.log ( request, response, time );
            logged = true;
        }
        if ( !logged && useDefault ) {
            AccessLog newDefaultAccessLog = this.defaultAccessLog.get();
            if ( newDefaultAccessLog == null ) {
                final Host host = ( Host ) this.findChild ( this.getDefaultHost() );
                Context context = null;
                if ( host != null && host.getState().isAvailable() ) {
                    newDefaultAccessLog = host.getAccessLog();
                    if ( newDefaultAccessLog != null ) {
                        if ( this.defaultAccessLog.compareAndSet ( null, newDefaultAccessLog ) ) {
                            final AccessLogListener l = new AccessLogListener ( this, host, null );
                            l.install();
                        }
                    } else {
                        context = ( Context ) host.findChild ( "" );
                        if ( context != null && context.getState().isAvailable() ) {
                            newDefaultAccessLog = context.getAccessLog();
                            if ( newDefaultAccessLog != null && this.defaultAccessLog.compareAndSet ( null, newDefaultAccessLog ) ) {
                                final AccessLogListener l = new AccessLogListener ( this, null, context );
                                l.install();
                            }
                        }
                    }
                }
                if ( newDefaultAccessLog == null ) {
                    newDefaultAccessLog = new NoopAccessLog();
                    if ( this.defaultAccessLog.compareAndSet ( null, newDefaultAccessLog ) ) {
                        final AccessLogListener l = new AccessLogListener ( this, host, context );
                        l.install();
                    }
                }
            }
            newDefaultAccessLog.log ( request, response, time );
        }
    }
    @Override
    public ClassLoader getParentClassLoader() {
        if ( this.parentClassLoader != null ) {
            return this.parentClassLoader;
        }
        if ( this.service != null ) {
            return this.service.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }
    @Override
    public File getCatalinaBase() {
        if ( this.service != null ) {
            final Server s = this.service.getServer();
            if ( s != null ) {
                final File base = s.getCatalinaBase();
                if ( base != null ) {
                    return base;
                }
            }
        }
        return super.getCatalinaBase();
    }
    @Override
    public File getCatalinaHome() {
        if ( this.service != null ) {
            final Server s = this.service.getServer();
            if ( s != null ) {
                final File base = s.getCatalinaHome();
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
        return this.getName();
    }
    static {
        log = LogFactory.getLog ( StandardEngine.class );
    }
    protected static final class NoopAccessLog implements AccessLog {
        @Override
        public void log ( final Request request, final Response response, final long time ) {
        }
        @Override
        public void setRequestAttributesEnabled ( final boolean requestAttributesEnabled ) {
        }
        @Override
        public boolean getRequestAttributesEnabled() {
            return false;
        }
    }
    protected static final class AccessLogListener implements PropertyChangeListener, LifecycleListener, ContainerListener {
        private final StandardEngine engine;
        private final Host host;
        private final Context context;
        private volatile boolean disabled;
        public AccessLogListener ( final StandardEngine engine, final Host host, final Context context ) {
            this.disabled = false;
            this.engine = engine;
            this.host = host;
            this.context = context;
        }
        public void install() {
            this.engine.addPropertyChangeListener ( this );
            if ( this.host != null ) {
                this.host.addContainerListener ( this );
                this.host.addLifecycleListener ( this );
            }
            if ( this.context != null ) {
                this.context.addLifecycleListener ( this );
            }
        }
        private void uninstall() {
            this.disabled = true;
            if ( this.context != null ) {
                this.context.removeLifecycleListener ( this );
            }
            if ( this.host != null ) {
                this.host.removeLifecycleListener ( this );
                this.host.removeContainerListener ( this );
            }
            this.engine.removePropertyChangeListener ( this );
        }
        @Override
        public void lifecycleEvent ( final LifecycleEvent event ) {
            if ( this.disabled ) {
                return;
            }
            final String type = event.getType();
            if ( "after_start".equals ( type ) || "before_stop".equals ( type ) || "before_destroy".equals ( type ) ) {
                this.engine.defaultAccessLog.set ( null );
                this.uninstall();
            }
        }
        @Override
        public void propertyChange ( final PropertyChangeEvent evt ) {
            if ( this.disabled ) {
                return;
            }
            if ( "defaultHost".equals ( evt.getPropertyName() ) ) {
                this.engine.defaultAccessLog.set ( null );
                this.uninstall();
            }
        }
        @Override
        public void containerEvent ( final ContainerEvent event ) {
            if ( this.disabled ) {
                return;
            }
            if ( "addChild".equals ( event.getType() ) ) {
                final Context context = ( Context ) event.getData();
                if ( "".equals ( context.getPath() ) ) {
                    this.engine.defaultAccessLog.set ( null );
                    this.uninstall();
                }
            }
        }
    }
}

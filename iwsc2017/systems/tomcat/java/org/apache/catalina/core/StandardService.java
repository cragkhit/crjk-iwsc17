package org.apache.catalina.core;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Executor;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.mapper.Mapper;
import org.apache.catalina.mapper.MapperListener;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class StandardService extends LifecycleMBeanBase implements Service {
    private static final Log log = LogFactory.getLog ( StandardService.class );
    private String name = null;
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private Server server = null;
    protected final PropertyChangeSupport support = new PropertyChangeSupport ( this );
    protected Connector connectors[] = new Connector[0];
    private final Object connectorsLock = new Object();
    protected final ArrayList<Executor> executors = new ArrayList<>();
    private Engine engine = null;
    private ClassLoader parentClassLoader = null;
    protected final Mapper mapper = new Mapper();
    protected final MapperListener mapperListener = new MapperListener ( this );
    @Override
    public Mapper getMapper() {
        return mapper;
    }
    @Override
    public Engine getContainer() {
        return engine;
    }
    @Override
    public void setContainer ( Engine engine ) {
        Engine oldEngine = this.engine;
        if ( oldEngine != null ) {
            oldEngine.setService ( null );
        }
        this.engine = engine;
        if ( this.engine != null ) {
            this.engine.setService ( this );
        }
        if ( getState().isAvailable() ) {
            if ( this.engine != null ) {
                try {
                    this.engine.start();
                } catch ( LifecycleException e ) {
                    log.warn ( sm.getString ( "standardService.engine.startFailed" ), e );
                }
            }
            try {
                mapperListener.stop();
            } catch ( LifecycleException e ) {
                log.warn ( sm.getString ( "standardService.mapperListener.stopFailed" ), e );
            }
            try {
                mapperListener.start();
            } catch ( LifecycleException e ) {
                log.warn ( sm.getString ( "standardService.mapperListener.startFailed" ), e );
            }
            if ( oldEngine != null ) {
                try {
                    oldEngine.stop();
                } catch ( LifecycleException e ) {
                    log.warn ( sm.getString ( "standardService.engine.stopFailed" ), e );
                }
            }
        }
        support.firePropertyChange ( "container", oldEngine, this.engine );
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName ( String name ) {
        this.name = name;
    }
    @Override
    public Server getServer() {
        return this.server;
    }
    @Override
    public void setServer ( Server server ) {
        this.server = server;
    }
    @Override
    public void addConnector ( Connector connector ) {
        synchronized ( connectorsLock ) {
            connector.setService ( this );
            Connector results[] = new Connector[connectors.length + 1];
            System.arraycopy ( connectors, 0, results, 0, connectors.length );
            results[connectors.length] = connector;
            connectors = results;
            if ( getState().isAvailable() ) {
                try {
                    connector.start();
                } catch ( LifecycleException e ) {
                    log.error ( sm.getString (
                                    "standardService.connector.startFailed",
                                    connector ), e );
                }
            }
            support.firePropertyChange ( "connector", null, connector );
        }
    }
    public ObjectName[] getConnectorNames() {
        ObjectName results[] = new ObjectName[connectors.length];
        for ( int i = 0; i < results.length; i++ ) {
            results[i] = connectors[i].getObjectName();
        }
        return results;
    }
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    @Override
    public Connector[] findConnectors() {
        return connectors;
    }
    @Override
    public void removeConnector ( Connector connector ) {
        synchronized ( connectorsLock ) {
            int j = -1;
            for ( int i = 0; i < connectors.length; i++ ) {
                if ( connector == connectors[i] ) {
                    j = i;
                    break;
                }
            }
            if ( j < 0 ) {
                return;
            }
            if ( connectors[j].getState().isAvailable() ) {
                try {
                    connectors[j].stop();
                } catch ( LifecycleException e ) {
                    log.error ( sm.getString (
                                    "standardService.connector.stopFailed",
                                    connectors[j] ), e );
                }
            }
            connector.setService ( null );
            int k = 0;
            Connector results[] = new Connector[connectors.length - 1];
            for ( int i = 0; i < connectors.length; i++ ) {
                if ( i != j ) {
                    results[k++] = connectors[i];
                }
            }
            connectors = results;
            support.firePropertyChange ( "connector", connector, null );
        }
    }
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "StandardService[" );
        sb.append ( getName() );
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    public void addExecutor ( Executor ex ) {
        synchronized ( executors ) {
            if ( !executors.contains ( ex ) ) {
                executors.add ( ex );
                if ( getState().isAvailable() ) {
                    try {
                        ex.start();
                    } catch ( LifecycleException x ) {
                        log.error ( "Executor.start", x );
                    }
                }
            }
        }
    }
    @Override
    public Executor[] findExecutors() {
        synchronized ( executors ) {
            Executor[] arr = new Executor[executors.size()];
            executors.toArray ( arr );
            return arr;
        }
    }
    @Override
    public Executor getExecutor ( String executorName ) {
        synchronized ( executors ) {
            for ( Executor executor : executors ) {
                if ( executorName.equals ( executor.getName() ) ) {
                    return executor;
                }
            }
        }
        return null;
    }
    @Override
    public void removeExecutor ( Executor ex ) {
        synchronized ( executors ) {
            if ( executors.remove ( ex ) && getState().isAvailable() ) {
                try {
                    ex.stop();
                } catch ( LifecycleException e ) {
                    log.error ( "Executor.stop", e );
                }
            }
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "standardService.start.name", this.name ) );
        }
        setState ( LifecycleState.STARTING );
        if ( engine != null ) {
            synchronized ( engine ) {
                engine.start();
            }
        }
        synchronized ( executors ) {
            for ( Executor executor : executors ) {
                executor.start();
            }
        }
        mapperListener.start();
        synchronized ( connectorsLock ) {
            for ( Connector connector : connectors ) {
                if ( connector.getState() != LifecycleState.FAILED ) {
                    connector.start();
                }
            }
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        synchronized ( connectorsLock ) {
            for ( Connector connector : connectors ) {
                connector.pause();
            }
        }
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "standardService.stop.name", this.name ) );
        }
        setState ( LifecycleState.STOPPING );
        if ( engine != null ) {
            synchronized ( engine ) {
                engine.stop();
            }
        }
        synchronized ( connectorsLock ) {
            for ( Connector connector : connectors ) {
                if ( !LifecycleState.STARTED.equals (
                            connector.getState() ) ) {
                    continue;
                }
                connector.stop();
            }
        }
        if ( mapperListener.getState() != LifecycleState.INITIALIZED ) {
            mapperListener.stop();
        }
        synchronized ( executors ) {
            for ( Executor executor : executors ) {
                executor.stop();
            }
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( engine != null ) {
            engine.init();
        }
        for ( Executor executor : findExecutors() ) {
            if ( executor instanceof JmxEnabled ) {
                ( ( JmxEnabled ) executor ).setDomain ( getDomain() );
            }
            executor.init();
        }
        mapperListener.init();
        synchronized ( connectorsLock ) {
            for ( Connector connector : connectors ) {
                connector.init();
            }
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        mapperListener.destroy();
        synchronized ( connectorsLock ) {
            for ( Connector connector : connectors ) {
                connector.destroy();
            }
        }
        for ( Executor executor : findExecutors() ) {
            executor.destroy();
        }
        if ( engine != null ) {
            engine.destroy();
        }
        super.destroyInternal();
    }
    @Override
    public ClassLoader getParentClassLoader() {
        if ( parentClassLoader != null ) {
            return parentClassLoader;
        }
        if ( server != null ) {
            return server.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }
    @Override
    public void setParentClassLoader ( ClassLoader parent ) {
        ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
        support.firePropertyChange ( "parentClassLoader", oldParentClassLoader,
                                     this.parentClassLoader );
    }
    @Override
    protected String getDomainInternal() {
        String domain = null;
        Container engine = getContainer();
        if ( engine != null ) {
            domain = engine.getName();
        }
        if ( domain == null ) {
            domain = getName();
        }
        return domain;
    }
    @Override
    public final String getObjectNameKeyProperties() {
        return "type=Service";
    }
}

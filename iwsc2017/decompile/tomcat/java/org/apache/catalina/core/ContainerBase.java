package org.apache.catalina.core;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.Loader;
import org.apache.tomcat.util.ExceptionUtils;
import javax.management.ObjectName;
import org.apache.catalina.Host;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.Engine;
import org.apache.catalina.ContainerEvent;
import java.io.File;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.beans.PropertyChangeListener;
import org.apache.catalina.LifecycleState;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Lifecycle;
import java.util.concurrent.locks.Lock;
import org.apache.juli.logging.LogFactory;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.catalina.AccessLog;
import java.beans.PropertyChangeSupport;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Realm;
import org.apache.catalina.Pipeline;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.catalina.Cluster;
import org.apache.catalina.ContainerListener;
import java.util.List;
import java.util.HashMap;
import org.apache.juli.logging.Log;
import org.apache.catalina.Container;
import org.apache.catalina.util.LifecycleMBeanBase;
public abstract class ContainerBase extends LifecycleMBeanBase implements Container {
    private static final Log log;
    protected final HashMap<String, Container> children;
    protected int backgroundProcessorDelay;
    protected final List<ContainerListener> listeners;
    protected Log logger;
    protected String logName;
    protected Cluster cluster;
    private final ReadWriteLock clusterLock;
    protected String name;
    protected Container parent;
    protected ClassLoader parentClassLoader;
    protected final Pipeline pipeline;
    private volatile Realm realm;
    private final ReadWriteLock realmLock;
    protected static final StringManager sm;
    protected boolean startChildren;
    protected final PropertyChangeSupport support;
    private Thread thread;
    private volatile boolean threadDone;
    protected volatile AccessLog accessLog;
    private volatile boolean accessLogScanComplete;
    private int startStopThreads;
    protected ThreadPoolExecutor startStopExecutor;
    public ContainerBase() {
        this.children = new HashMap<String, Container>();
        this.backgroundProcessorDelay = -1;
        this.listeners = new CopyOnWriteArrayList<ContainerListener>();
        this.logger = null;
        this.logName = null;
        this.cluster = null;
        this.clusterLock = new ReentrantReadWriteLock();
        this.name = null;
        this.parent = null;
        this.parentClassLoader = null;
        this.pipeline = new StandardPipeline ( this );
        this.realm = null;
        this.realmLock = new ReentrantReadWriteLock();
        this.startChildren = true;
        this.support = new PropertyChangeSupport ( this );
        this.thread = null;
        this.threadDone = false;
        this.accessLog = null;
        this.accessLogScanComplete = false;
        this.startStopThreads = 1;
    }
    @Override
    public int getStartStopThreads() {
        return this.startStopThreads;
    }
    private int getStartStopThreadsInternal() {
        int result = this.getStartStopThreads();
        if ( result > 0 ) {
            return result;
        }
        result += Runtime.getRuntime().availableProcessors();
        if ( result < 1 ) {
            result = 1;
        }
        return result;
    }
    @Override
    public void setStartStopThreads ( final int startStopThreads ) {
        this.startStopThreads = startStopThreads;
        final ThreadPoolExecutor executor = this.startStopExecutor;
        if ( executor != null ) {
            final int newThreads = this.getStartStopThreadsInternal();
            executor.setMaximumPoolSize ( newThreads );
            executor.setCorePoolSize ( newThreads );
        }
    }
    @Override
    public int getBackgroundProcessorDelay() {
        return this.backgroundProcessorDelay;
    }
    @Override
    public void setBackgroundProcessorDelay ( final int delay ) {
        this.backgroundProcessorDelay = delay;
    }
    @Override
    public Log getLogger() {
        if ( this.logger != null ) {
            return this.logger;
        }
        return this.logger = LogFactory.getLog ( this.getLogName() );
    }
    @Override
    public String getLogName() {
        if ( this.logName != null ) {
            return this.logName;
        }
        String loggerName = null;
        for ( Container current = this; current != null; current = current.getParent() ) {
            String name = current.getName();
            if ( name == null || name.equals ( "" ) ) {
                name = "/";
            } else if ( name.startsWith ( "##" ) ) {
                name = "/" + name;
            }
            loggerName = "[" + name + "]" + ( ( loggerName != null ) ? ( "." + loggerName ) : "" );
        }
        return this.logName = ContainerBase.class.getName() + "." + loggerName;
    }
    @Override
    public Cluster getCluster() {
        final Lock readLock = this.clusterLock.readLock();
        readLock.lock();
        try {
            if ( this.cluster != null ) {
                return this.cluster;
            }
            if ( this.parent != null ) {
                return this.parent.getCluster();
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }
    protected Cluster getClusterInternal() {
        final Lock readLock = this.clusterLock.readLock();
        readLock.lock();
        try {
            return this.cluster;
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public void setCluster ( final Cluster cluster ) {
        Cluster oldCluster = null;
        final Lock writeLock = this.clusterLock.writeLock();
        writeLock.lock();
        try {
            oldCluster = this.cluster;
            if ( oldCluster == cluster ) {
                return;
            }
            this.cluster = cluster;
            if ( this.getState().isAvailable() && oldCluster != null && oldCluster instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) oldCluster ).stop();
                } catch ( LifecycleException e ) {
                    ContainerBase.log.error ( "ContainerBase.setCluster: stop: ", e );
                }
            }
            if ( cluster != null ) {
                cluster.setContainer ( this );
            }
            if ( this.getState().isAvailable() && cluster != null && cluster instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) cluster ).start();
                } catch ( LifecycleException e ) {
                    ContainerBase.log.error ( "ContainerBase.setCluster: start: ", e );
                }
            }
        } finally {
            writeLock.unlock();
        }
        this.support.firePropertyChange ( "cluster", oldCluster, cluster );
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public void setName ( final String name ) {
        final String oldName = this.name;
        this.name = name;
        this.support.firePropertyChange ( "name", oldName, this.name );
    }
    public boolean getStartChildren() {
        return this.startChildren;
    }
    public void setStartChildren ( final boolean startChildren ) {
        final boolean oldStartChildren = this.startChildren;
        this.startChildren = startChildren;
        this.support.firePropertyChange ( "startChildren", oldStartChildren, this.startChildren );
    }
    @Override
    public Container getParent() {
        return this.parent;
    }
    @Override
    public void setParent ( final Container container ) {
        final Container oldParent = this.parent;
        this.parent = container;
        this.support.firePropertyChange ( "parent", oldParent, this.parent );
    }
    @Override
    public ClassLoader getParentClassLoader() {
        if ( this.parentClassLoader != null ) {
            return this.parentClassLoader;
        }
        if ( this.parent != null ) {
            return this.parent.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }
    @Override
    public void setParentClassLoader ( final ClassLoader parent ) {
        final ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
        this.support.firePropertyChange ( "parentClassLoader", oldParentClassLoader, this.parentClassLoader );
    }
    @Override
    public Pipeline getPipeline() {
        return this.pipeline;
    }
    @Override
    public Realm getRealm() {
        final Lock l = this.realmLock.readLock();
        l.lock();
        try {
            if ( this.realm != null ) {
                return this.realm;
            }
            if ( this.parent != null ) {
                return this.parent.getRealm();
            }
            return null;
        } finally {
            l.unlock();
        }
    }
    protected Realm getRealmInternal() {
        final Lock l = this.realmLock.readLock();
        l.lock();
        try {
            return this.realm;
        } finally {
            l.unlock();
        }
    }
    @Override
    public void setRealm ( final Realm realm ) {
        final Lock l = this.realmLock.writeLock();
        l.lock();
        try {
            final Realm oldRealm = this.realm;
            if ( oldRealm == realm ) {
                return;
            }
            this.realm = realm;
            if ( this.getState().isAvailable() && oldRealm != null && oldRealm instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) oldRealm ).stop();
                } catch ( LifecycleException e ) {
                    ContainerBase.log.error ( "ContainerBase.setRealm: stop: ", e );
                }
            }
            if ( realm != null ) {
                realm.setContainer ( this );
            }
            if ( this.getState().isAvailable() && realm != null && realm instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) realm ).start();
                } catch ( LifecycleException e ) {
                    ContainerBase.log.error ( "ContainerBase.setRealm: start: ", e );
                }
            }
            this.support.firePropertyChange ( "realm", oldRealm, this.realm );
        } finally {
            l.unlock();
        }
    }
    @Override
    public void addChild ( final Container child ) {
        if ( Globals.IS_SECURITY_ENABLED ) {
            final PrivilegedAction<Void> dp = new PrivilegedAddChild ( child );
            AccessController.doPrivileged ( dp );
        } else {
            this.addChildInternal ( child );
        }
    }
    private void addChildInternal ( final Container child ) {
        if ( ContainerBase.log.isDebugEnabled() ) {
            ContainerBase.log.debug ( "Add child " + child + " " + this );
        }
        synchronized ( this.children ) {
            if ( this.children.get ( child.getName() ) != null ) {
                throw new IllegalArgumentException ( "addChild:  Child name '" + child.getName() + "' is not unique" );
            }
            child.setParent ( this );
            this.children.put ( child.getName(), child );
        }
        try {
            if ( ( this.getState().isAvailable() || LifecycleState.STARTING_PREP.equals ( this.getState() ) ) && this.startChildren ) {
                child.start();
            }
        } catch ( LifecycleException e ) {
            ContainerBase.log.error ( "ContainerBase.addChild: start: ", e );
            throw new IllegalStateException ( "ContainerBase.addChild: start: " + e );
        } finally {
            this.fireContainerEvent ( "addChild", child );
        }
    }
    @Override
    public void addContainerListener ( final ContainerListener listener ) {
        this.listeners.add ( listener );
    }
    @Override
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.addPropertyChangeListener ( listener );
    }
    @Override
    public Container findChild ( final String name ) {
        if ( name == null ) {
            return null;
        }
        synchronized ( this.children ) {
            return this.children.get ( name );
        }
    }
    @Override
    public Container[] findChildren() {
        synchronized ( this.children ) {
            final Container[] results = new Container[this.children.size()];
            return this.children.values().toArray ( results );
        }
    }
    @Override
    public ContainerListener[] findContainerListeners() {
        final ContainerListener[] results = new ContainerListener[0];
        return this.listeners.toArray ( results );
    }
    @Override
    public void removeChild ( final Container child ) {
        if ( child == null ) {
            return;
        }
        try {
            if ( child.getState().isAvailable() ) {
                child.stop();
            }
        } catch ( LifecycleException e ) {
            ContainerBase.log.error ( "ContainerBase.removeChild: stop: ", e );
        }
        try {
            if ( !LifecycleState.DESTROYING.equals ( child.getState() ) ) {
                child.destroy();
            }
        } catch ( LifecycleException e ) {
            ContainerBase.log.error ( "ContainerBase.removeChild: destroy: ", e );
        }
        synchronized ( this.children ) {
            if ( this.children.get ( child.getName() ) == null ) {
                return;
            }
            this.children.remove ( child.getName() );
        }
        this.fireContainerEvent ( "removeChild", child );
    }
    @Override
    public void removeContainerListener ( final ContainerListener listener ) {
        this.listeners.remove ( listener );
    }
    @Override
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.removePropertyChangeListener ( listener );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        final BlockingQueue<Runnable> startStopQueue = new LinkedBlockingQueue<Runnable>();
        ( this.startStopExecutor = new ThreadPoolExecutor ( this.getStartStopThreadsInternal(), this.getStartStopThreadsInternal(), 10L, TimeUnit.SECONDS, startStopQueue, new StartStopThreadFactory ( this.getName() + "-startStop-" ) ) ).allowCoreThreadTimeOut ( true );
        super.initInternal();
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        this.logger = null;
        this.getLogger();
        final Cluster cluster = this.getClusterInternal();
        if ( cluster instanceof Lifecycle ) {
            ( ( Lifecycle ) cluster ).start();
        }
        final Realm realm = this.getRealmInternal();
        if ( realm instanceof Lifecycle ) {
            ( ( Lifecycle ) realm ).start();
        }
        final Container[] children = this.findChildren();
        final List<Future<Void>> results = new ArrayList<Future<Void>>();
        for ( int i = 0; i < children.length; ++i ) {
            results.add ( this.startStopExecutor.submit ( ( Callable<Void> ) new StartChild ( children[i] ) ) );
        }
        boolean fail = false;
        for ( final Future<Void> result : results ) {
            try {
                result.get();
            } catch ( Exception e ) {
                ContainerBase.log.error ( ContainerBase.sm.getString ( "containerBase.threadedStartFailed" ), e );
                fail = true;
            }
        }
        if ( fail ) {
            throw new LifecycleException ( ContainerBase.sm.getString ( "containerBase.threadedStartFailed" ) );
        }
        if ( this.pipeline instanceof Lifecycle ) {
            ( ( Lifecycle ) this.pipeline ).start();
        }
        this.setState ( LifecycleState.STARTING );
        this.threadStart();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        this.threadStop();
        this.setState ( LifecycleState.STOPPING );
        if ( this.pipeline instanceof Lifecycle && ( ( Lifecycle ) this.pipeline ).getState().isAvailable() ) {
            ( ( Lifecycle ) this.pipeline ).stop();
        }
        final Container[] children = this.findChildren();
        final List<Future<Void>> results = new ArrayList<Future<Void>>();
        for ( int i = 0; i < children.length; ++i ) {
            results.add ( this.startStopExecutor.submit ( ( Callable<Void> ) new StopChild ( children[i] ) ) );
        }
        boolean fail = false;
        for ( final Future<Void> result : results ) {
            try {
                result.get();
            } catch ( Exception e ) {
                ContainerBase.log.error ( ContainerBase.sm.getString ( "containerBase.threadedStopFailed" ), e );
                fail = true;
            }
        }
        if ( fail ) {
            throw new LifecycleException ( ContainerBase.sm.getString ( "containerBase.threadedStopFailed" ) );
        }
        final Realm realm = this.getRealmInternal();
        if ( realm instanceof Lifecycle ) {
            ( ( Lifecycle ) realm ).stop();
        }
        final Cluster cluster = this.getClusterInternal();
        if ( cluster instanceof Lifecycle ) {
            ( ( Lifecycle ) cluster ).stop();
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        final Realm realm = this.getRealmInternal();
        if ( realm instanceof Lifecycle ) {
            ( ( Lifecycle ) realm ).destroy();
        }
        final Cluster cluster = this.getClusterInternal();
        if ( cluster instanceof Lifecycle ) {
            ( ( Lifecycle ) cluster ).destroy();
        }
        if ( this.pipeline instanceof Lifecycle ) {
            ( ( Lifecycle ) this.pipeline ).destroy();
        }
        for ( final Container child : this.findChildren() ) {
            this.removeChild ( child );
        }
        if ( this.parent != null ) {
            this.parent.removeChild ( this );
        }
        if ( this.startStopExecutor != null ) {
            this.startStopExecutor.shutdownNow();
        }
        super.destroyInternal();
    }
    @Override
    public void logAccess ( final Request request, final Response response, final long time, final boolean useDefault ) {
        boolean logged = false;
        if ( this.getAccessLog() != null ) {
            this.getAccessLog().log ( request, response, time );
            logged = true;
        }
        if ( this.getParent() != null ) {
            this.getParent().logAccess ( request, response, time, useDefault && !logged );
        }
    }
    @Override
    public AccessLog getAccessLog() {
        if ( this.accessLogScanComplete ) {
            return this.accessLog;
        }
        AccessLogAdapter adapter = null;
        final Valve[] valves2;
        final Valve[] valves = valves2 = this.getPipeline().getValves();
        for ( final Valve valve : valves2 ) {
            if ( valve instanceof AccessLog ) {
                if ( adapter == null ) {
                    adapter = new AccessLogAdapter ( ( AccessLog ) valve );
                } else {
                    adapter.add ( ( AccessLog ) valve );
                }
            }
        }
        if ( adapter != null ) {
            this.accessLog = adapter;
        }
        this.accessLogScanComplete = true;
        return this.accessLog;
    }
    public synchronized void addValve ( final Valve valve ) {
        this.pipeline.addValve ( valve );
    }
    @Override
    public void backgroundProcess() {
        if ( !this.getState().isAvailable() ) {
            return;
        }
        final Cluster cluster = this.getClusterInternal();
        if ( cluster != null ) {
            try {
                cluster.backgroundProcess();
            } catch ( Exception e ) {
                ContainerBase.log.warn ( ContainerBase.sm.getString ( "containerBase.backgroundProcess.cluster", cluster ), e );
            }
        }
        final Realm realm = this.getRealmInternal();
        if ( realm != null ) {
            try {
                realm.backgroundProcess();
            } catch ( Exception e2 ) {
                ContainerBase.log.warn ( ContainerBase.sm.getString ( "containerBase.backgroundProcess.realm", realm ), e2 );
            }
        }
        for ( Valve current = this.pipeline.getFirst(); current != null; current = current.getNext() ) {
            try {
                current.backgroundProcess();
            } catch ( Exception e3 ) {
                ContainerBase.log.warn ( ContainerBase.sm.getString ( "containerBase.backgroundProcess.valve", current ), e3 );
            }
        }
        this.fireLifecycleEvent ( "periodic", null );
    }
    @Override
    public File getCatalinaBase() {
        if ( this.parent == null ) {
            return null;
        }
        return this.parent.getCatalinaBase();
    }
    @Override
    public File getCatalinaHome() {
        if ( this.parent == null ) {
            return null;
        }
        return this.parent.getCatalinaHome();
    }
    @Override
    public void fireContainerEvent ( final String type, final Object data ) {
        if ( this.listeners.size() < 1 ) {
            return;
        }
        final ContainerEvent event = new ContainerEvent ( this, type, data );
        for ( final ContainerListener listener : this.listeners ) {
            listener.containerEvent ( event );
        }
    }
    @Override
    protected String getDomainInternal() {
        final Container p = this.getParent();
        if ( p == null ) {
            return null;
        }
        return p.getDomain();
    }
    @Override
    public String getMBeanKeyProperties() {
        Container c = this;
        final StringBuilder keyProperties = new StringBuilder();
        int containerCount = 0;
        while ( ! ( c instanceof Engine ) ) {
            if ( c instanceof Wrapper ) {
                keyProperties.insert ( 0, ",servlet=" );
                keyProperties.insert ( 9, c.getName() );
            } else if ( c instanceof Context ) {
                keyProperties.insert ( 0, ",context=" );
                final ContextName cn = new ContextName ( c.getName(), false );
                keyProperties.insert ( 9, cn.getDisplayName() );
            } else if ( c instanceof Host ) {
                keyProperties.insert ( 0, ",host=" );
                keyProperties.insert ( 6, c.getName() );
            } else {
                if ( c == null ) {
                    keyProperties.append ( ",container" );
                    keyProperties.append ( containerCount++ );
                    keyProperties.append ( "=null" );
                    break;
                }
                keyProperties.append ( ",container" );
                keyProperties.append ( containerCount++ );
                keyProperties.append ( '=' );
                keyProperties.append ( c.getName() );
            }
            c = c.getParent();
        }
        return keyProperties.toString();
    }
    public ObjectName[] getChildren() {
        final List<ObjectName> names = new ArrayList<ObjectName> ( this.children.size() );
        for ( final Object next : this.children.values() ) {
            if ( next instanceof ContainerBase ) {
                names.add ( ( ( ContainerBase ) next ).getObjectName() );
            }
        }
        return names.toArray ( new ObjectName[names.size()] );
    }
    protected void threadStart() {
        if ( this.thread != null ) {
            return;
        }
        if ( this.backgroundProcessorDelay <= 0 ) {
            return;
        }
        this.threadDone = false;
        final String threadName = "ContainerBackgroundProcessor[" + this.toString() + "]";
        ( this.thread = new Thread ( new ContainerBackgroundProcessor(), threadName ) ).setDaemon ( true );
        this.thread.start();
    }
    protected void threadStop() {
        if ( this.thread == null ) {
            return;
        }
        this.threadDone = true;
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch ( InterruptedException ex ) {}
        this.thread = null;
    }
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        final Container parent = this.getParent();
        if ( parent != null ) {
            sb.append ( parent.toString() );
            sb.append ( '.' );
        }
        sb.append ( this.getClass().getSimpleName() );
        sb.append ( '[' );
        sb.append ( this.getName() );
        sb.append ( ']' );
        return sb.toString();
    }
    static {
        log = LogFactory.getLog ( ContainerBase.class );
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
    protected class PrivilegedAddChild implements PrivilegedAction<Void> {
        private final Container child;
        PrivilegedAddChild ( final Container child ) {
            this.child = child;
        }
        @Override
        public Void run() {
            ContainerBase.this.addChildInternal ( this.child );
            return null;
        }
    }
    protected class ContainerBackgroundProcessor implements Runnable {
        @Override
        public void run() {
            Throwable t = null;
            final String unexpectedDeathMessage = ContainerBase.sm.getString ( "containerBase.backgroundProcess.unexpectedThreadDeath", Thread.currentThread().getName() );
            try {
                while ( !ContainerBase.this.threadDone ) {
                    try {
                        Thread.sleep ( ContainerBase.this.backgroundProcessorDelay * 1000L );
                    } catch ( InterruptedException ex ) {}
                    if ( !ContainerBase.this.threadDone ) {
                        this.processChildren ( ContainerBase.this );
                    }
                }
            } catch ( RuntimeException ) {}
            catch ( Error e ) {
                t = e;
                throw e;
            } finally {
                if ( !ContainerBase.this.threadDone ) {
                    ContainerBase.log.error ( unexpectedDeathMessage, t );
                }
            }
        }
        protected void processChildren ( final Container container ) {
            ClassLoader originalClassLoader = null;
            try {
                if ( container instanceof Context ) {
                    final Loader loader = ( ( Context ) container ).getLoader();
                    if ( loader == null ) {
                        return;
                    }
                    originalClassLoader = ( ( Context ) container ).bind ( false, null );
                }
                container.backgroundProcess();
                final Container[] children = container.findChildren();
                for ( int i = 0; i < children.length; ++i ) {
                    if ( children[i].getBackgroundProcessorDelay() <= 0 ) {
                        this.processChildren ( children[i] );
                    }
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                ContainerBase.log.error ( "Exception invoking periodic operation: ", t );
            } finally {
                if ( container instanceof Context ) {
                    ( ( Context ) container ).unbind ( false, originalClassLoader );
                }
            }
        }
    }
    private static class StartChild implements Callable<Void> {
        private Container child;
        public StartChild ( final Container child ) {
            this.child = child;
        }
        @Override
        public Void call() throws LifecycleException {
            this.child.start();
            return null;
        }
    }
    private static class StopChild implements Callable<Void> {
        private Container child;
        public StopChild ( final Container child ) {
            this.child = child;
        }
        @Override
        public Void call() throws LifecycleException {
            if ( this.child.getState().isAvailable() ) {
                this.child.stop();
            }
            return null;
        }
    }
    private static class StartStopThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber;
        private final String namePrefix;
        public StartStopThreadFactory ( final String namePrefix ) {
            this.threadNumber = new AtomicInteger ( 1 );
            final SecurityManager s = System.getSecurityManager();
            this.group = ( ( s != null ) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup() );
            this.namePrefix = namePrefix;
        }
        @Override
        public Thread newThread ( final Runnable r ) {
            final Thread thread = new Thread ( this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() );
            thread.setDaemon ( true );
            return thread;
        }
    }
}

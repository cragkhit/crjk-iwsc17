package org.apache.catalina.core;
import org.apache.juli.logging.LogFactory;
import java.util.concurrent.Executor;
import org.apache.coyote.ProtocolHandler;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.apache.catalina.Host;
import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.LifecycleEvent;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.LifecycleListener;
public class ThreadLocalLeakPreventionListener implements LifecycleListener, ContainerListener {
    private static final Log log;
    private volatile boolean serverStopping;
    protected static final StringManager sm;
    public ThreadLocalLeakPreventionListener() {
        this.serverStopping = false;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        try {
            final Lifecycle lifecycle = event.getLifecycle();
            if ( "after_start".equals ( event.getType() ) && lifecycle instanceof Server ) {
                final Server server = ( Server ) lifecycle;
                this.registerListenersForServer ( server );
            }
            if ( "before_stop".equals ( event.getType() ) && lifecycle instanceof Server ) {
                this.serverStopping = true;
            }
            if ( "after_stop".equals ( event.getType() ) && lifecycle instanceof Context ) {
                this.stopIdleThreads ( ( Context ) lifecycle );
            }
        } catch ( Exception e ) {
            final String msg = ThreadLocalLeakPreventionListener.sm.getString ( "threadLocalLeakPreventionListener.lifecycleEvent.error", event );
            ThreadLocalLeakPreventionListener.log.error ( msg, e );
        }
    }
    @Override
    public void containerEvent ( final ContainerEvent event ) {
        try {
            final String type = event.getType();
            if ( "addChild".equals ( type ) ) {
                this.processContainerAddChild ( event.getContainer(), ( Container ) event.getData() );
            } else if ( "removeChild".equals ( type ) ) {
                this.processContainerRemoveChild ( event.getContainer(), ( Container ) event.getData() );
            }
        } catch ( Exception e ) {
            final String msg = ThreadLocalLeakPreventionListener.sm.getString ( "threadLocalLeakPreventionListener.containerEvent.error", event );
            ThreadLocalLeakPreventionListener.log.error ( msg, e );
        }
    }
    private void registerListenersForServer ( final Server server ) {
        for ( final Service service : server.findServices() ) {
            final Engine engine = service.getContainer();
            engine.addContainerListener ( this );
            this.registerListenersForEngine ( engine );
        }
    }
    private void registerListenersForEngine ( final Engine engine ) {
        for ( final Container hostContainer : engine.findChildren() ) {
            final Host host = ( Host ) hostContainer;
            host.addContainerListener ( this );
            this.registerListenersForHost ( host );
        }
    }
    private void registerListenersForHost ( final Host host ) {
        for ( final Container contextContainer : host.findChildren() ) {
            final Context context = ( Context ) contextContainer;
            this.registerContextListener ( context );
        }
    }
    private void registerContextListener ( final Context context ) {
        context.addLifecycleListener ( this );
    }
    protected void processContainerAddChild ( final Container parent, final Container child ) {
        if ( ThreadLocalLeakPreventionListener.log.isDebugEnabled() ) {
            ThreadLocalLeakPreventionListener.log.debug ( "Process addChild[parent=" + parent + ",child=" + child + "]" );
        }
        if ( child instanceof Context ) {
            this.registerContextListener ( ( Context ) child );
        } else if ( child instanceof Engine ) {
            this.registerListenersForEngine ( ( Engine ) child );
        } else if ( child instanceof Host ) {
            this.registerListenersForHost ( ( Host ) child );
        }
    }
    protected void processContainerRemoveChild ( final Container parent, final Container child ) {
        if ( ThreadLocalLeakPreventionListener.log.isDebugEnabled() ) {
            ThreadLocalLeakPreventionListener.log.debug ( "Process removeChild[parent=" + parent + ",child=" + child + "]" );
        }
        if ( child instanceof Context ) {
            final Context context = ( Context ) child;
            context.removeLifecycleListener ( this );
        } else if ( child instanceof Host || child instanceof Engine ) {
            child.removeContainerListener ( this );
        }
    }
    private void stopIdleThreads ( final Context context ) {
        if ( this.serverStopping ) {
            return;
        }
        if ( ! ( context instanceof StandardContext ) || ! ( ( StandardContext ) context ).getRenewThreadsWhenStoppingContext() ) {
            ThreadLocalLeakPreventionListener.log.debug ( "Not renewing threads when the context is stopping. It is not configured to do it." );
            return;
        }
        final Engine engine = ( Engine ) context.getParent().getParent();
        final Service service = engine.getService();
        final Connector[] connectors = service.findConnectors();
        if ( connectors != null ) {
            for ( final Connector connector : connectors ) {
                final ProtocolHandler handler = connector.getProtocolHandler();
                Executor executor = null;
                if ( handler != null ) {
                    executor = handler.getExecutor();
                }
                if ( executor instanceof ThreadPoolExecutor ) {
                    final ThreadPoolExecutor threadPoolExecutor = ( ThreadPoolExecutor ) executor;
                    threadPoolExecutor.contextStopping();
                } else if ( executor instanceof StandardThreadExecutor ) {
                    final StandardThreadExecutor stdThreadExecutor = ( StandardThreadExecutor ) executor;
                    stdThreadExecutor.contextStopping();
                }
            }
        }
    }
    static {
        log = LogFactory.getLog ( ThreadLocalLeakPreventionListener.class );
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}

package org.apache.catalina.core;
import org.apache.catalina.ContainerEvent;
import java.beans.PropertyChangeEvent;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.LifecycleListener;
import java.beans.PropertyChangeListener;
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
            StandardEngine.access$000 ( this.engine ).set ( null );
            this.uninstall();
        }
    }
    @Override
    public void propertyChange ( final PropertyChangeEvent evt ) {
        if ( this.disabled ) {
            return;
        }
        if ( "defaultHost".equals ( evt.getPropertyName() ) ) {
            StandardEngine.access$000 ( this.engine ).set ( null );
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
                StandardEngine.access$000 ( this.engine ).set ( null );
                this.uninstall();
            }
        }
    }
}

package org.jfree.chart.panel;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.event.OverlayChangeListener;
import javax.swing.event.EventListenerList;
public class AbstractOverlay {
    private transient EventListenerList changeListeners;
    public AbstractOverlay() {
        this.changeListeners = new EventListenerList();
    }
    public void addChangeListener ( final OverlayChangeListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.changeListeners.add ( OverlayChangeListener.class, listener );
    }
    public void removeChangeListener ( final OverlayChangeListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.changeListeners.remove ( OverlayChangeListener.class, listener );
    }
    public void fireOverlayChanged() {
        final OverlayChangeEvent event = new OverlayChangeEvent ( this );
        this.notifyListeners ( event );
    }
    protected void notifyListeners ( final OverlayChangeEvent event ) {
        final Object[] listeners = this.changeListeners.getListenerList();
        for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if ( listeners[i] == OverlayChangeListener.class ) {
                ( ( OverlayChangeListener ) listeners[i + 1] ).overlayChanged ( event );
            }
        }
    }
}

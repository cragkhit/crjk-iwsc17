package org.jfree.chart.plot.dial;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Arrays;
import java.util.EventListener;
import org.jfree.chart.HashUtilities;
import javax.swing.event.EventListenerList;
public abstract class AbstractDialLayer implements DialLayer {
    private boolean visible;
    private transient EventListenerList listenerList;
    protected AbstractDialLayer() {
        this.visible = true;
        this.listenerList = new EventListenerList();
    }
    @Override
    public boolean isVisible() {
        return this.visible;
    }
    public void setVisible ( final boolean visible ) {
        this.visible = visible;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractDialLayer ) ) {
            return false;
        }
        final AbstractDialLayer that = ( AbstractDialLayer ) obj;
        return this.visible == that.visible;
    }
    @Override
    public int hashCode() {
        int result = 23;
        result = HashUtilities.hashCode ( result, this.visible );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final AbstractDialLayer clone = ( AbstractDialLayer ) super.clone();
        clone.listenerList = new EventListenerList();
        return clone;
    }
    @Override
    public void addChangeListener ( final DialLayerChangeListener listener ) {
        this.listenerList.add ( DialLayerChangeListener.class, listener );
    }
    @Override
    public void removeChangeListener ( final DialLayerChangeListener listener ) {
        this.listenerList.remove ( DialLayerChangeListener.class, listener );
    }
    @Override
    public boolean hasListener ( final EventListener listener ) {
        final List list = Arrays.asList ( this.listenerList.getListenerList() );
        return list.contains ( listener );
    }
    protected void notifyListeners ( final DialLayerChangeEvent event ) {
        final Object[] listeners = this.listenerList.getListenerList();
        for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if ( listeners[i] == DialLayerChangeListener.class ) {
                ( ( DialLayerChangeListener ) listeners[i + 1] ).dialLayerChanged ( event );
            }
        }
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.listenerList = new EventListenerList();
    }
}

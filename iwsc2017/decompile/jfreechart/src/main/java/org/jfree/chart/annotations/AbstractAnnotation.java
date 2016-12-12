package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jfree.chart.event.AnnotationChangeEvent;
import java.util.List;
import java.util.Arrays;
import java.util.EventListener;
import org.jfree.chart.event.AnnotationChangeListener;
import javax.swing.event.EventListenerList;
import java.io.Serializable;
public abstract class AbstractAnnotation implements Annotation, Cloneable, Serializable {
    private transient EventListenerList listenerList;
    private boolean notify;
    protected AbstractAnnotation() {
        this.notify = true;
        this.listenerList = new EventListenerList();
    }
    @Override
    public void addChangeListener ( final AnnotationChangeListener listener ) {
        this.listenerList.add ( AnnotationChangeListener.class, listener );
    }
    @Override
    public void removeChangeListener ( final AnnotationChangeListener listener ) {
        this.listenerList.remove ( AnnotationChangeListener.class, listener );
    }
    public boolean hasListener ( final EventListener listener ) {
        final List list = Arrays.asList ( this.listenerList.getListenerList() );
        return list.contains ( listener );
    }
    protected void fireAnnotationChanged() {
        if ( this.notify ) {
            this.notifyListeners ( new AnnotationChangeEvent ( this, this ) );
        }
    }
    protected void notifyListeners ( final AnnotationChangeEvent event ) {
        final Object[] listeners = this.listenerList.getListenerList();
        for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if ( listeners[i] == AnnotationChangeListener.class ) {
                ( ( AnnotationChangeListener ) listeners[i + 1] ).annotationChanged ( event );
            }
        }
    }
    public boolean getNotify() {
        return this.notify;
    }
    public void setNotify ( final boolean flag ) {
        this.notify = flag;
        if ( this.notify ) {
            this.fireAnnotationChanged();
        }
    }
    public Object clone() throws CloneNotSupportedException {
        final AbstractAnnotation clone = ( AbstractAnnotation ) super.clone();
        clone.listenerList = new EventListenerList();
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.listenerList = new EventListenerList();
    }
}

package org.jfree.data.general;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeListener;
import org.jfree.util.ObjectUtilities;
import java.beans.PropertyVetoException;
import org.jfree.chart.util.ParamChecks;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyChangeSupport;
import javax.swing.event.EventListenerList;
import java.io.Serializable;
public abstract class Series implements Cloneable, Serializable {
    private static final long serialVersionUID = -6906561437538683581L;
    private Comparable key;
    private String description;
    private EventListenerList listeners;
    private PropertyChangeSupport propertyChangeSupport;
    private VetoableChangeSupport vetoableChangeSupport;
    private boolean notify;
    protected Series ( final Comparable key ) {
        this ( key, null );
    }
    protected Series ( final Comparable key, final String description ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
        this.description = description;
        this.listeners = new EventListenerList();
        this.propertyChangeSupport = new PropertyChangeSupport ( this );
        this.vetoableChangeSupport = new VetoableChangeSupport ( this );
        this.notify = true;
    }
    public Comparable getKey() {
        return this.key;
    }
    public void setKey ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        final Comparable old = this.key;
        try {
            this.vetoableChangeSupport.fireVetoableChange ( "Key", old, key );
            this.key = key;
            this.propertyChangeSupport.firePropertyChange ( "Key", old, key );
        } catch ( PropertyVetoException e ) {
            throw new IllegalArgumentException ( e.getMessage() );
        }
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        final String old = this.description;
        this.description = description;
        this.propertyChangeSupport.firePropertyChange ( "Description", old, description );
    }
    public boolean getNotify() {
        return this.notify;
    }
    public void setNotify ( final boolean notify ) {
        if ( this.notify != notify ) {
            this.notify = notify;
            this.fireSeriesChanged();
        }
    }
    public boolean isEmpty() {
        return this.getItemCount() == 0;
    }
    public abstract int getItemCount();
    public Object clone() throws CloneNotSupportedException {
        final Series clone = ( Series ) super.clone();
        clone.listeners = new EventListenerList();
        clone.propertyChangeSupport = new PropertyChangeSupport ( clone );
        clone.vetoableChangeSupport = new VetoableChangeSupport ( clone );
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Series ) ) {
            return false;
        }
        final Series that = ( Series ) obj;
        return this.getKey().equals ( that.getKey() ) && ObjectUtilities.equal ( ( Object ) this.getDescription(), ( Object ) that.getDescription() );
    }
    @Override
    public int hashCode() {
        int result = this.key.hashCode();
        result = 29 * result + ( ( this.description != null ) ? this.description.hashCode() : 0 );
        return result;
    }
    public void addChangeListener ( final SeriesChangeListener listener ) {
        this.listeners.add ( SeriesChangeListener.class, listener );
    }
    public void removeChangeListener ( final SeriesChangeListener listener ) {
        this.listeners.remove ( SeriesChangeListener.class, listener );
    }
    public void fireSeriesChanged() {
        if ( this.notify ) {
            this.notifyListeners ( new SeriesChangeEvent ( this ) );
        }
    }
    protected void notifyListeners ( final SeriesChangeEvent event ) {
        final Object[] listenerList = this.listeners.getListenerList();
        for ( int i = listenerList.length - 2; i >= 0; i -= 2 ) {
            if ( listenerList[i] == SeriesChangeListener.class ) {
                ( ( SeriesChangeListener ) listenerList[i + 1] ).seriesChanged ( event );
            }
        }
    }
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.propertyChangeSupport.addPropertyChangeListener ( listener );
    }
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.propertyChangeSupport.removePropertyChangeListener ( listener );
    }
    protected void firePropertyChange ( final String property, final Object oldValue, final Object newValue ) {
        this.propertyChangeSupport.firePropertyChange ( property, oldValue, newValue );
    }
    public void addVetoableChangeListener ( final VetoableChangeListener listener ) {
        this.vetoableChangeSupport.addVetoableChangeListener ( listener );
    }
    public void removeVetoableChangeListener ( final VetoableChangeListener listener ) {
        this.vetoableChangeSupport.removeVetoableChangeListener ( listener );
    }
    protected void fireVetoableChange ( final String property, final Object oldValue, final Object newValue ) throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange ( property, oldValue, newValue );
    }
}

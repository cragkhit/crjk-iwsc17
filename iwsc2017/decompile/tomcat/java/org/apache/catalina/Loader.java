package org.apache.catalina;
import java.beans.PropertyChangeListener;
public interface Loader {
    void backgroundProcess();
    ClassLoader getClassLoader();
    Context getContext();
    void setContext ( Context p0 );
    boolean getDelegate();
    void setDelegate ( boolean p0 );
    boolean getReloadable();
    void setReloadable ( boolean p0 );
    void addPropertyChangeListener ( PropertyChangeListener p0 );
    boolean modified();
    void removePropertyChangeListener ( PropertyChangeListener p0 );
}

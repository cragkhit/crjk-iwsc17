package org.apache.catalina;
import java.beans.PropertyChangeListener;
public interface Loader {
    public void backgroundProcess();
    public ClassLoader getClassLoader();
    public Context getContext();
    public void setContext ( Context context );
    public boolean getDelegate();
    public void setDelegate ( boolean delegate );
    public boolean getReloadable();
    public void setReloadable ( boolean reloadable );
    public void addPropertyChangeListener ( PropertyChangeListener listener );
    public boolean modified();
    public void removePropertyChangeListener ( PropertyChangeListener listener );
}

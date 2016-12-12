package org.apache.catalina;
import java.beans.PropertyChangeListener;
import java.io.IOException;
public interface Store {
    public Manager getManager();
    public void setManager ( Manager manager );
    public int getSize() throws IOException;
    public void addPropertyChangeListener ( PropertyChangeListener listener );
    public String[] keys() throws IOException;
    public Session load ( String id )
    throws ClassNotFoundException, IOException;
    public void remove ( String id ) throws IOException;
    public void clear() throws IOException;
    public void removePropertyChangeListener ( PropertyChangeListener listener );
    public void save ( Session session ) throws IOException;
}

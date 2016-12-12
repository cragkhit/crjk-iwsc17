package org.apache.catalina;
import java.beans.PropertyChangeListener;
import java.io.IOException;
public interface Store {
    Manager getManager();
    void setManager ( Manager p0 );
    int getSize() throws IOException;
    void addPropertyChangeListener ( PropertyChangeListener p0 );
    String[] keys() throws IOException;
    Session load ( String p0 ) throws ClassNotFoundException, IOException;
    void remove ( String p0 ) throws IOException;
    void clear() throws IOException;
    void removePropertyChangeListener ( PropertyChangeListener p0 );
    void save ( Session p0 ) throws IOException;
}

package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
public class ContextTransaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final HashMap<String, Object> properties = new HashMap<>();
    public Object getProperty ( String name ) {
        return properties.get ( name );
    }
    public void setProperty ( String name, Object value ) {
        properties.put ( name, value );
    }
    public void removeProperty ( String name ) {
        properties.remove ( name );
    }
    public Iterator<String> listProperties() {
        return properties.keySet().iterator();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "Transaction[" );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}

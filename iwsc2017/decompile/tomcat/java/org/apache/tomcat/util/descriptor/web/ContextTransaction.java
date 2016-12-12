package org.apache.tomcat.util.descriptor.web;
import java.util.Iterator;
import java.util.HashMap;
import java.io.Serializable;
public class ContextTransaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final HashMap<String, Object> properties;
    public ContextTransaction() {
        this.properties = new HashMap<String, Object>();
    }
    public Object getProperty ( final String name ) {
        return this.properties.get ( name );
    }
    public void setProperty ( final String name, final Object value ) {
        this.properties.put ( name, value );
    }
    public void removeProperty ( final String name ) {
        this.properties.remove ( name );
    }
    public Iterator<String> listProperties() {
        return this.properties.keySet().iterator();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "Transaction[" );
        sb.append ( "]" );
        return sb.toString();
    }
}

package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
public class SecurityRoleRef implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name = null;
    public String getName() {
        return ( this.name );
    }
    public void setName ( String name ) {
        this.name = name;
    }
    private String link = null;
    public String getLink() {
        return ( this.link );
    }
    public void setLink ( String link ) {
        this.link = link;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "SecurityRoleRef[" );
        sb.append ( "name=" );
        sb.append ( name );
        if ( link != null ) {
            sb.append ( ", link=" );
            sb.append ( link );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
}

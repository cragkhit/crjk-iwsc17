package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
public class SecurityRoleRef implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String link;
    public SecurityRoleRef() {
        this.name = null;
        this.link = null;
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getLink() {
        return this.link;
    }
    public void setLink ( final String link ) {
        this.link = link;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "SecurityRoleRef[" );
        sb.append ( "name=" );
        sb.append ( this.name );
        if ( this.link != null ) {
            sb.append ( ", link=" );
            sb.append ( this.link );
        }
        sb.append ( "]" );
        return sb.toString();
    }
}

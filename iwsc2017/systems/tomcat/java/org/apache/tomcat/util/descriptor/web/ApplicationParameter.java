package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
public class ApplicationParameter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description = null;
    public String getDescription() {
        return ( this.description );
    }
    public void setDescription ( String description ) {
        this.description = description;
    }
    private String name = null;
    public String getName() {
        return ( this.name );
    }
    public void setName ( String name ) {
        this.name = name;
    }
    private boolean override = true;
    public boolean getOverride() {
        return ( this.override );
    }
    public void setOverride ( boolean override ) {
        this.override = override;
    }
    private String value = null;
    public String getValue() {
        return ( this.value );
    }
    public void setValue ( String value ) {
        this.value = value;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ApplicationParameter[" );
        sb.append ( "name=" );
        sb.append ( name );
        if ( description != null ) {
            sb.append ( ", description=" );
            sb.append ( description );
        }
        sb.append ( ", value=" );
        sb.append ( value );
        sb.append ( ", override=" );
        sb.append ( override );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}

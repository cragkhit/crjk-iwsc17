package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
public class ApplicationParameter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private String name;
    private boolean override;
    private String value;
    public ApplicationParameter() {
        this.description = null;
        this.name = null;
        this.override = true;
        this.value = null;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        this.description = description;
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public boolean getOverride() {
        return this.override;
    }
    public void setOverride ( final boolean override ) {
        this.override = override;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue ( final String value ) {
        this.value = value;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ApplicationParameter[" );
        sb.append ( "name=" );
        sb.append ( this.name );
        if ( this.description != null ) {
            sb.append ( ", description=" );
            sb.append ( this.description );
        }
        sb.append ( ", value=" );
        sb.append ( this.value );
        sb.append ( ", override=" );
        sb.append ( this.override );
        sb.append ( "]" );
        return sb.toString();
    }
}

package org.apache.tomcat.util.descriptor.web;
public class ContextEnvironment extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private boolean override;
    private String value;
    public ContextEnvironment() {
        this.override = true;
        this.value = null;
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
        final StringBuilder sb = new StringBuilder ( "ContextEnvironment[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.getDescription() != null ) {
            sb.append ( ", description=" );
            sb.append ( this.getDescription() );
        }
        if ( this.getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( this.getType() );
        }
        if ( this.value != null ) {
            sb.append ( ", value=" );
            sb.append ( this.value );
        }
        sb.append ( ", override=" );
        sb.append ( this.override );
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( this.override ? 1231 : 1237 );
        result = 31 * result + ( ( this.value == null ) ? 0 : this.value.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        final ContextEnvironment other = ( ContextEnvironment ) obj;
        if ( this.override != other.override ) {
            return false;
        }
        if ( this.value == null ) {
            if ( other.value != null ) {
                return false;
            }
        } else if ( !this.value.equals ( other.value ) ) {
            return false;
        }
        return true;
    }
}

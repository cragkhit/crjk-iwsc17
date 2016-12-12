package org.apache.tomcat.util.descriptor.web;
public class ContextResourceEnvRef extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private boolean override;
    public ContextResourceEnvRef() {
        this.override = true;
    }
    public boolean getOverride() {
        return this.override;
    }
    public void setOverride ( final boolean override ) {
        this.override = override;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ContextResourceEnvRef[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( this.getType() );
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
        final ContextResourceEnvRef other = ( ContextResourceEnvRef ) obj;
        return this.override == other.override;
    }
}

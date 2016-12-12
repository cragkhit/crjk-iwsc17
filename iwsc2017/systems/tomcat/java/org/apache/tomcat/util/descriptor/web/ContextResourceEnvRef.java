package org.apache.tomcat.util.descriptor.web;
public class ContextResourceEnvRef extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private boolean override = true;
    public boolean getOverride() {
        return ( this.override );
    }
    public void setOverride ( boolean override ) {
        this.override = override;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ContextResourceEnvRef[" );
        sb.append ( "name=" );
        sb.append ( getName() );
        if ( getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( getType() );
        }
        sb.append ( ", override=" );
        sb.append ( override );
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( override ? 1231 : 1237 );
        return result;
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        ContextResourceEnvRef other = ( ContextResourceEnvRef ) obj;
        if ( override != other.override ) {
            return false;
        }
        return true;
    }
}

package org.apache.tomcat.util.descriptor.web;
public class ContextResource extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String auth;
    private String scope;
    private boolean singleton;
    private String closeMethod;
    public ContextResource() {
        this.auth = null;
        this.scope = "Shareable";
        this.singleton = true;
        this.closeMethod = null;
    }
    public String getAuth() {
        return this.auth;
    }
    public void setAuth ( final String auth ) {
        this.auth = auth;
    }
    public String getScope() {
        return this.scope;
    }
    public void setScope ( final String scope ) {
        this.scope = scope;
    }
    public boolean getSingleton() {
        return this.singleton;
    }
    public void setSingleton ( final boolean singleton ) {
        this.singleton = singleton;
    }
    public String getCloseMethod() {
        return this.closeMethod;
    }
    public void setCloseMethod ( final String closeMethod ) {
        this.closeMethod = closeMethod;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ContextResource[" );
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
        if ( this.auth != null ) {
            sb.append ( ", auth=" );
            sb.append ( this.auth );
        }
        if ( this.scope != null ) {
            sb.append ( ", scope=" );
            sb.append ( this.scope );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( ( this.auth == null ) ? 0 : this.auth.hashCode() );
        result = 31 * result + ( ( this.closeMethod == null ) ? 0 : this.closeMethod.hashCode() );
        result = 31 * result + ( ( this.scope == null ) ? 0 : this.scope.hashCode() );
        result = 31 * result + ( this.singleton ? 1231 : 1237 );
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
        final ContextResource other = ( ContextResource ) obj;
        if ( this.auth == null ) {
            if ( other.auth != null ) {
                return false;
            }
        } else if ( !this.auth.equals ( other.auth ) ) {
            return false;
        }
        if ( this.closeMethod == null ) {
            if ( other.closeMethod != null ) {
                return false;
            }
        } else if ( !this.closeMethod.equals ( other.closeMethod ) ) {
            return false;
        }
        if ( this.scope == null ) {
            if ( other.scope != null ) {
                return false;
            }
        } else if ( !this.scope.equals ( other.scope ) ) {
            return false;
        }
        return this.singleton == other.singleton;
    }
}

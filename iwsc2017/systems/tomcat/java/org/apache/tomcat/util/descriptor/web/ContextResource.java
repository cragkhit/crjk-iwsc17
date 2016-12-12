package org.apache.tomcat.util.descriptor.web;
public class ContextResource extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String auth = null;
    public String getAuth() {
        return ( this.auth );
    }
    public void setAuth ( String auth ) {
        this.auth = auth;
    }
    private String scope = "Shareable";
    public String getScope() {
        return ( this.scope );
    }
    public void setScope ( String scope ) {
        this.scope = scope;
    }
    private boolean singleton = true;
    public boolean getSingleton() {
        return singleton;
    }
    public void setSingleton ( boolean singleton ) {
        this.singleton = singleton;
    }
    private String closeMethod = null;
    public String getCloseMethod() {
        return closeMethod;
    }
    public void setCloseMethod ( String closeMethod ) {
        this.closeMethod = closeMethod;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ContextResource[" );
        sb.append ( "name=" );
        sb.append ( getName() );
        if ( getDescription() != null ) {
            sb.append ( ", description=" );
            sb.append ( getDescription() );
        }
        if ( getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( getType() );
        }
        if ( auth != null ) {
            sb.append ( ", auth=" );
            sb.append ( auth );
        }
        if ( scope != null ) {
            sb.append ( ", scope=" );
            sb.append ( scope );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( auth == null ) ? 0 : auth.hashCode() );
        result = prime * result +
                 ( ( closeMethod == null ) ? 0 : closeMethod.hashCode() );
        result = prime * result + ( ( scope == null ) ? 0 : scope.hashCode() );
        result = prime * result + ( singleton ? 1231 : 1237 );
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
        ContextResource other = ( ContextResource ) obj;
        if ( auth == null ) {
            if ( other.auth != null ) {
                return false;
            }
        } else if ( !auth.equals ( other.auth ) ) {
            return false;
        }
        if ( closeMethod == null ) {
            if ( other.closeMethod != null ) {
                return false;
            }
        } else if ( !closeMethod.equals ( other.closeMethod ) ) {
            return false;
        }
        if ( scope == null ) {
            if ( other.scope != null ) {
                return false;
            }
        } else if ( !scope.equals ( other.scope ) ) {
            return false;
        }
        if ( singleton != other.singleton ) {
            return false;
        }
        return true;
    }
}

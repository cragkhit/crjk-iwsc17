package org.apache.tomcat.util.descriptor.web;
public class ContextResourceLink extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String global;
    private String factory;
    public ContextResourceLink() {
        this.global = null;
        this.factory = null;
    }
    public String getGlobal() {
        return this.global;
    }
    public void setGlobal ( final String global ) {
        this.global = global;
    }
    public String getFactory() {
        return this.factory;
    }
    public void setFactory ( final String factory ) {
        this.factory = factory;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ContextResourceLink[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( this.getType() );
        }
        if ( this.getGlobal() != null ) {
            sb.append ( ", global=" );
            sb.append ( this.getGlobal() );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( ( this.factory == null ) ? 0 : this.factory.hashCode() );
        result = 31 * result + ( ( this.global == null ) ? 0 : this.global.hashCode() );
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
        final ContextResourceLink other = ( ContextResourceLink ) obj;
        if ( this.factory == null ) {
            if ( other.factory != null ) {
                return false;
            }
        } else if ( !this.factory.equals ( other.factory ) ) {
            return false;
        }
        if ( this.global == null ) {
            if ( other.global != null ) {
                return false;
            }
        } else if ( !this.global.equals ( other.global ) ) {
            return false;
        }
        return true;
    }
}

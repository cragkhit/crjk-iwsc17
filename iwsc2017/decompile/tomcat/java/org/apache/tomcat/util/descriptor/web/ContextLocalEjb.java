package org.apache.tomcat.util.descriptor.web;
public class ContextLocalEjb extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String home;
    private String link;
    private String local;
    public ContextLocalEjb() {
        this.home = null;
        this.link = null;
        this.local = null;
    }
    public String getHome() {
        return this.home;
    }
    public void setHome ( final String home ) {
        this.home = home;
    }
    public String getLink() {
        return this.link;
    }
    public void setLink ( final String link ) {
        this.link = link;
    }
    public String getLocal() {
        return this.local;
    }
    public void setLocal ( final String local ) {
        this.local = local;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ContextLocalEjb[" );
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
        if ( this.home != null ) {
            sb.append ( ", home=" );
            sb.append ( this.home );
        }
        if ( this.link != null ) {
            sb.append ( ", link=" );
            sb.append ( this.link );
        }
        if ( this.local != null ) {
            sb.append ( ", local=" );
            sb.append ( this.local );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( ( this.home == null ) ? 0 : this.home.hashCode() );
        result = 31 * result + ( ( this.link == null ) ? 0 : this.link.hashCode() );
        result = 31 * result + ( ( this.local == null ) ? 0 : this.local.hashCode() );
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
        final ContextLocalEjb other = ( ContextLocalEjb ) obj;
        if ( this.home == null ) {
            if ( other.home != null ) {
                return false;
            }
        } else if ( !this.home.equals ( other.home ) ) {
            return false;
        }
        if ( this.link == null ) {
            if ( other.link != null ) {
                return false;
            }
        } else if ( !this.link.equals ( other.link ) ) {
            return false;
        }
        if ( this.local == null ) {
            if ( other.local != null ) {
                return false;
            }
        } else if ( !this.local.equals ( other.local ) ) {
            return false;
        }
        return true;
    }
}

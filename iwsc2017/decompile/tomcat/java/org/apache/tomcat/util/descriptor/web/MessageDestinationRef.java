package org.apache.tomcat.util.descriptor.web;
public class MessageDestinationRef extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String link;
    private String usage;
    public MessageDestinationRef() {
        this.link = null;
        this.usage = null;
    }
    public String getLink() {
        return this.link;
    }
    public void setLink ( final String link ) {
        this.link = link;
    }
    public String getUsage() {
        return this.usage;
    }
    public void setUsage ( final String usage ) {
        this.usage = usage;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "MessageDestination[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.link != null ) {
            sb.append ( ", link=" );
            sb.append ( this.link );
        }
        if ( this.getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( this.getType() );
        }
        if ( this.usage != null ) {
            sb.append ( ", usage=" );
            sb.append ( this.usage );
        }
        if ( this.getDescription() != null ) {
            sb.append ( ", description=" );
            sb.append ( this.getDescription() );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( ( this.link == null ) ? 0 : this.link.hashCode() );
        result = 31 * result + ( ( this.usage == null ) ? 0 : this.usage.hashCode() );
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
        final MessageDestinationRef other = ( MessageDestinationRef ) obj;
        if ( this.link == null ) {
            if ( other.link != null ) {
                return false;
            }
        } else if ( !this.link.equals ( other.link ) ) {
            return false;
        }
        if ( this.usage == null ) {
            if ( other.usage != null ) {
                return false;
            }
        } else if ( !this.usage.equals ( other.usage ) ) {
            return false;
        }
        return true;
    }
}

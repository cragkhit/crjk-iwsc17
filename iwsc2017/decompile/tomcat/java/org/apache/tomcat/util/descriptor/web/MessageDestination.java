package org.apache.tomcat.util.descriptor.web;
public class MessageDestination extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String displayName;
    private String largeIcon;
    private String smallIcon;
    public MessageDestination() {
        this.displayName = null;
        this.largeIcon = null;
        this.smallIcon = null;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public void setDisplayName ( final String displayName ) {
        this.displayName = displayName;
    }
    public String getLargeIcon() {
        return this.largeIcon;
    }
    public void setLargeIcon ( final String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    public String getSmallIcon() {
        return this.smallIcon;
    }
    public void setSmallIcon ( final String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "MessageDestination[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.displayName != null ) {
            sb.append ( ", displayName=" );
            sb.append ( this.displayName );
        }
        if ( this.largeIcon != null ) {
            sb.append ( ", largeIcon=" );
            sb.append ( this.largeIcon );
        }
        if ( this.smallIcon != null ) {
            sb.append ( ", smallIcon=" );
            sb.append ( this.smallIcon );
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
        result = 31 * result + ( ( this.displayName == null ) ? 0 : this.displayName.hashCode() );
        result = 31 * result + ( ( this.largeIcon == null ) ? 0 : this.largeIcon.hashCode() );
        result = 31 * result + ( ( this.smallIcon == null ) ? 0 : this.smallIcon.hashCode() );
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
        final MessageDestination other = ( MessageDestination ) obj;
        if ( this.displayName == null ) {
            if ( other.displayName != null ) {
                return false;
            }
        } else if ( !this.displayName.equals ( other.displayName ) ) {
            return false;
        }
        if ( this.largeIcon == null ) {
            if ( other.largeIcon != null ) {
                return false;
            }
        } else if ( !this.largeIcon.equals ( other.largeIcon ) ) {
            return false;
        }
        if ( this.smallIcon == null ) {
            if ( other.smallIcon != null ) {
                return false;
            }
        } else if ( !this.smallIcon.equals ( other.smallIcon ) ) {
            return false;
        }
        return true;
    }
}

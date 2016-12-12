package org.apache.catalina.authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import java.io.Serializable;
public class SingleSignOnSessionKey implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String sessionId;
    private final String contextName;
    private final String hostName;
    public SingleSignOnSessionKey ( final Session session ) {
        this.sessionId = session.getId();
        final Context context = session.getManager().getContext();
        this.contextName = context.getName();
        this.hostName = context.getParent().getName();
    }
    public String getSessionId() {
        return this.sessionId;
    }
    public String getContextName() {
        return this.contextName;
    }
    public String getHostName() {
        return this.hostName;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ( ( this.sessionId == null ) ? 0 : this.sessionId.hashCode() );
        result = 31 * result + ( ( this.contextName == null ) ? 0 : this.contextName.hashCode() );
        result = 31 * result + ( ( this.hostName == null ) ? 0 : this.hostName.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        final SingleSignOnSessionKey other = ( SingleSignOnSessionKey ) obj;
        if ( this.sessionId == null ) {
            if ( other.sessionId != null ) {
                return false;
            }
        } else if ( !this.sessionId.equals ( other.sessionId ) ) {
            return false;
        }
        if ( this.contextName == null ) {
            if ( other.contextName != null ) {
                return false;
            }
        } else if ( !this.contextName.equals ( other.contextName ) ) {
            return false;
        }
        if ( this.hostName == null ) {
            if ( other.hostName != null ) {
                return false;
            }
        } else if ( !this.hostName.equals ( other.hostName ) ) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( 128 );
        sb.append ( "Host: [" );
        sb.append ( this.hostName );
        sb.append ( "], Context: [" );
        sb.append ( this.contextName );
        sb.append ( "], SessionID: [" );
        sb.append ( this.sessionId );
        sb.append ( "]" );
        return sb.toString();
    }
}

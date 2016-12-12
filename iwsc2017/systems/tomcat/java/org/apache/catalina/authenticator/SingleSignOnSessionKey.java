package org.apache.catalina.authenticator;
import java.io.Serializable;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
public class SingleSignOnSessionKey implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String sessionId;
    private final String contextName;
    private final String hostName;
    public SingleSignOnSessionKey ( Session session ) {
        this.sessionId = session.getId();
        Context context = session.getManager().getContext();
        this.contextName = context.getName();
        this.hostName = context.getParent().getName();
    }
    public String getSessionId() {
        return sessionId;
    }
    public String getContextName() {
        return contextName;
    }
    public String getHostName() {
        return hostName;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
                 ( ( sessionId == null ) ? 0 : sessionId.hashCode() );
        result = prime * result +
                 ( ( contextName == null ) ? 0 : contextName.hashCode() );
        result = prime * result +
                 ( ( hostName == null ) ? 0 : hostName.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        SingleSignOnSessionKey other = ( SingleSignOnSessionKey ) obj;
        if ( sessionId == null ) {
            if ( other.sessionId != null ) {
                return false;
            }
        } else if ( !sessionId.equals ( other.sessionId ) ) {
            return false;
        }
        if ( contextName == null ) {
            if ( other.contextName != null ) {
                return false;
            }
        } else if ( !contextName.equals ( other.contextName ) ) {
            return false;
        }
        if ( hostName == null ) {
            if ( other.hostName != null ) {
                return false;
            }
        } else if ( !hostName.equals ( other.hostName ) ) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( 128 );
        sb.append ( "Host: [" );
        sb.append ( hostName );
        sb.append ( "], Context: [" );
        sb.append ( contextName );
        sb.append ( "], SessionID: [" );
        sb.append ( sessionId );
        sb.append ( "]" );
        return sb.toString();
    }
}

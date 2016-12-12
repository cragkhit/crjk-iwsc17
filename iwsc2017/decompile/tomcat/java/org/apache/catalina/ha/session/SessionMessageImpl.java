package org.apache.catalina.ha.session;
import org.apache.catalina.ha.ClusterMessageBase;
public class SessionMessageImpl extends ClusterMessageBase implements SessionMessage {
    private static final long serialVersionUID = 2L;
    private final int mEvtType;
    private final byte[] mSession;
    private final String mSessionID;
    private final String mContextName;
    private long serializationTimestamp;
    private boolean timestampSet;
    private String uniqueId;
    private SessionMessageImpl ( final String contextName, final int eventtype, final byte[] session, final String sessionID ) {
        this.timestampSet = false;
        this.mEvtType = eventtype;
        this.mSession = session;
        this.mSessionID = sessionID;
        this.mContextName = contextName;
        this.uniqueId = sessionID;
    }
    public SessionMessageImpl ( final String contextName, final int eventtype, final byte[] session, final String sessionID, final String uniqueID ) {
        this ( contextName, eventtype, session, sessionID );
        this.uniqueId = uniqueID;
    }
    @Override
    public int getEventType() {
        return this.mEvtType;
    }
    @Override
    public byte[] getSession() {
        return this.mSession;
    }
    @Override
    public String getSessionID() {
        return this.mSessionID;
    }
    @Override
    public void setTimestamp ( final long time ) {
        synchronized ( this ) {
            if ( !this.timestampSet ) {
                this.serializationTimestamp = time;
                this.timestampSet = true;
            }
        }
    }
    @Override
    public long getTimestamp() {
        return this.serializationTimestamp;
    }
    @Override
    public String getEventTypeString() {
        switch ( this.mEvtType ) {
        case 1: {
            return "SESSION-MODIFIED";
        }
        case 2: {
            return "SESSION-EXPIRED";
        }
        case 3: {
            return "SESSION-ACCESSED";
        }
        case 4: {
            return "SESSION-GET-ALL";
        }
        case 13: {
            return "SESSION-DELTA";
        }
        case 12: {
            return "ALL-SESSION-DATA";
        }
        case 14: {
            return "SESSION-STATE-TRANSFERED";
        }
        case 15: {
            return "SESSION-ID-CHANGED";
        }
        case 16: {
            return "NO-CONTEXT-MANAGER";
        }
        default: {
            return "UNKNOWN-EVENT-TYPE";
        }
        }
    }
    @Override
    public String getContextName() {
        return this.mContextName;
    }
    @Override
    public String getUniqueId() {
        return this.uniqueId;
    }
    @Override
    public String toString() {
        return this.getEventTypeString() + "#" + this.getContextName() + "#" + this.getSessionID();
    }
}

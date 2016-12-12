package org.apache.catalina.ha.session;
import org.apache.catalina.ha.ClusterMessageBase;
public class SessionMessageImpl extends ClusterMessageBase implements SessionMessage {
    private static final long serialVersionUID = 2L;
    private final int mEvtType;
    private final byte[] mSession;
    private final String mSessionID;
    private final String mContextName;
    private long serializationTimestamp;
    private boolean timestampSet = false ;
    private String uniqueId;
    private SessionMessageImpl ( String contextName,
                                 int eventtype,
                                 byte[] session,
                                 String sessionID ) {
        mEvtType = eventtype;
        mSession = session;
        mSessionID = sessionID;
        mContextName = contextName;
        uniqueId = sessionID;
    }
    public SessionMessageImpl ( String contextName,
                                int eventtype,
                                byte[] session,
                                String sessionID,
                                String uniqueID ) {
        this ( contextName, eventtype, session, sessionID );
        uniqueId = uniqueID;
    }
    @Override
    public int getEventType() {
        return mEvtType;
    }
    @Override
    public byte[] getSession() {
        return mSession;
    }
    @Override
    public String getSessionID() {
        return mSessionID;
    }
    @Override
    public void setTimestamp ( long time ) {
        synchronized ( this ) {
            if ( !timestampSet ) {
                serializationTimestamp = time;
                timestampSet = true ;
            }
        }
    }
    @Override
    public long getTimestamp() {
        return serializationTimestamp;
    }
    @Override
    public String getEventTypeString() {
        switch ( mEvtType ) {
        case EVT_SESSION_CREATED :
            return "SESSION-MODIFIED";
        case EVT_SESSION_EXPIRED :
            return "SESSION-EXPIRED";
        case EVT_SESSION_ACCESSED :
            return "SESSION-ACCESSED";
        case EVT_GET_ALL_SESSIONS :
            return "SESSION-GET-ALL";
        case EVT_SESSION_DELTA :
            return "SESSION-DELTA";
        case EVT_ALL_SESSION_DATA :
            return "ALL-SESSION-DATA";
        case EVT_ALL_SESSION_TRANSFERCOMPLETE :
            return "SESSION-STATE-TRANSFERED";
        case EVT_CHANGE_SESSION_ID :
            return "SESSION-ID-CHANGED";
        case EVT_ALL_SESSION_NOCONTEXTMANAGER :
            return "NO-CONTEXT-MANAGER";
        default :
            return "UNKNOWN-EVENT-TYPE";
        }
    }
    @Override
    public String getContextName() {
        return mContextName;
    }
    @Override
    public String getUniqueId() {
        return uniqueId;
    }
    @Override
    public String toString() {
        return getEventTypeString() + "#" + getContextName() + "#" + getSessionID() ;
    }
}

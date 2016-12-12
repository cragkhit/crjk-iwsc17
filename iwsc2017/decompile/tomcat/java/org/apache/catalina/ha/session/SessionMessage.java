package org.apache.catalina.ha.session;
import org.apache.catalina.ha.ClusterMessage;
public interface SessionMessage extends ClusterMessage {
    public static final int EVT_SESSION_CREATED = 1;
    public static final int EVT_SESSION_EXPIRED = 2;
    public static final int EVT_SESSION_ACCESSED = 3;
    public static final int EVT_GET_ALL_SESSIONS = 4;
    public static final int EVT_SESSION_DELTA = 13;
    public static final int EVT_ALL_SESSION_DATA = 12;
    public static final int EVT_ALL_SESSION_TRANSFERCOMPLETE = 14;
    public static final int EVT_CHANGE_SESSION_ID = 15;
    public static final int EVT_ALL_SESSION_NOCONTEXTMANAGER = 16;
    String getContextName();
    String getEventTypeString();
    int getEventType();
    byte[] getSession();
    String getSessionID();
}

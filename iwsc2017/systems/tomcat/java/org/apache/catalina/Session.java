package org.apache.catalina;
import java.security.Principal;
import java.util.Iterator;
import javax.servlet.http.HttpSession;
public interface Session {
    public static final String SESSION_CREATED_EVENT = "createSession";
    public static final String SESSION_DESTROYED_EVENT = "destroySession";
    public static final String SESSION_ACTIVATED_EVENT = "activateSession";
    public static final String SESSION_PASSIVATED_EVENT = "passivateSession";
    public String getAuthType();
    public void setAuthType ( String authType );
    public long getCreationTime();
    public long getCreationTimeInternal();
    public void setCreationTime ( long time );
    public String getId();
    public String getIdInternal();
    public void setId ( String id );
    public void setId ( String id, boolean notify );
    public long getThisAccessedTime();
    public long getThisAccessedTimeInternal();
    public long getLastAccessedTime();
    public long getLastAccessedTimeInternal();
    public long getIdleTime();
    public long getIdleTimeInternal();
    public Manager getManager();
    public void setManager ( Manager manager );
    public int getMaxInactiveInterval();
    public void setMaxInactiveInterval ( int interval );
    public void setNew ( boolean isNew );
    public Principal getPrincipal();
    public void setPrincipal ( Principal principal );
    public HttpSession getSession();
    public void setValid ( boolean isValid );
    public boolean isValid();
    public void access();
    public void addSessionListener ( SessionListener listener );
    public void endAccess();
    public void expire();
    public Object getNote ( String name );
    public Iterator<String> getNoteNames();
    public void recycle();
    public void removeNote ( String name );
    public void removeSessionListener ( SessionListener listener );
    public void setNote ( String name, Object value );
    public void tellChangedSessionId ( String newId, String oldId,
                                       boolean notifySessionListeners, boolean notifyContainerListeners );
    public boolean isAttributeDistributable ( String name, Object value );
}

package org.apache.catalina.manager;
import java.security.Principal;
import java.util.Iterator;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionListener;
public class DummyProxySession implements Session {
    private String sessionId;
    public DummyProxySession ( String sessionId ) {
        this.sessionId = sessionId;
    }
    @Override
    public void access() {
    }
    @Override
    public void addSessionListener ( SessionListener listener ) {
    }
    @Override
    public void endAccess() {
    }
    @Override
    public void expire() {
    }
    @Override
    public String getAuthType() {
        return null;
    }
    @Override
    public long getCreationTime() {
        return 0;
    }
    @Override
    public long getCreationTimeInternal() {
        return 0;
    }
    @Override
    public String getId() {
        return sessionId;
    }
    @Override
    public String getIdInternal() {
        return sessionId;
    }
    @Override
    public long getLastAccessedTime() {
        return 0;
    }
    @Override
    public long getLastAccessedTimeInternal() {
        return 0;
    }
    @Override
    public long getIdleTime() {
        return 0;
    }
    @Override
    public long getIdleTimeInternal() {
        return 0;
    }
    @Override
    public Manager getManager() {
        return null;
    }
    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }
    @Override
    public Object getNote ( String name ) {
        return null;
    }
    @Override
    public Iterator<String> getNoteNames() {
        return null;
    }
    @Override
    public Principal getPrincipal() {
        return null;
    }
    @Override
    public HttpSession getSession() {
        return null;
    }
    @Override
    public long getThisAccessedTime() {
        return 0;
    }
    @Override
    public long getThisAccessedTimeInternal() {
        return 0;
    }
    @Override
    public boolean isValid() {
        return false;
    }
    @Override
    public void recycle() {
    }
    @Override
    public void removeNote ( String name ) {
    }
    @Override
    public void removeSessionListener ( SessionListener listener ) {
    }
    @Override
    public void setAuthType ( String authType ) {
    }
    @Override
    public void setCreationTime ( long time ) {
    }
    @Override
    public void setId ( String id ) {
        this.sessionId = id;
    }
    @Override
    public void setId ( String id, boolean notify ) {
        this.sessionId = id;
    }
    @Override
    public void setManager ( Manager manager ) {
    }
    @Override
    public void setMaxInactiveInterval ( int interval ) {
    }
    @Override
    public void setNew ( boolean isNew ) {
    }
    @Override
    public void setNote ( String name, Object value ) {
    }
    @Override
    public void setPrincipal ( Principal principal ) {
    }
    @Override
    public void setValid ( boolean isValid ) {
    }
    @Override
    public void tellChangedSessionId ( String newId, String oldId,
                                       boolean notifySessionListeners, boolean notifyContainerListeners ) {
    }
    @Override
    public boolean isAttributeDistributable ( String name, Object value ) {
        return false;
    }
}

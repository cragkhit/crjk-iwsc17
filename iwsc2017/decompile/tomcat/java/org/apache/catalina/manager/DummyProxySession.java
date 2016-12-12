package org.apache.catalina.manager;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Iterator;
import org.apache.catalina.Manager;
import org.apache.catalina.SessionListener;
import org.apache.catalina.Session;
public class DummyProxySession implements Session {
    private String sessionId;
    public DummyProxySession ( final String sessionId ) {
        this.sessionId = sessionId;
    }
    @Override
    public void access() {
    }
    @Override
    public void addSessionListener ( final SessionListener listener ) {
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
        return 0L;
    }
    @Override
    public long getCreationTimeInternal() {
        return 0L;
    }
    @Override
    public String getId() {
        return this.sessionId;
    }
    @Override
    public String getIdInternal() {
        return this.sessionId;
    }
    @Override
    public long getLastAccessedTime() {
        return 0L;
    }
    @Override
    public long getLastAccessedTimeInternal() {
        return 0L;
    }
    @Override
    public long getIdleTime() {
        return 0L;
    }
    @Override
    public long getIdleTimeInternal() {
        return 0L;
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
    public Object getNote ( final String name ) {
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
        return 0L;
    }
    @Override
    public long getThisAccessedTimeInternal() {
        return 0L;
    }
    @Override
    public boolean isValid() {
        return false;
    }
    @Override
    public void recycle() {
    }
    @Override
    public void removeNote ( final String name ) {
    }
    @Override
    public void removeSessionListener ( final SessionListener listener ) {
    }
    @Override
    public void setAuthType ( final String authType ) {
    }
    @Override
    public void setCreationTime ( final long time ) {
    }
    @Override
    public void setId ( final String id ) {
        this.sessionId = id;
    }
    @Override
    public void setId ( final String id, final boolean notify ) {
        this.sessionId = id;
    }
    @Override
    public void setManager ( final Manager manager ) {
    }
    @Override
    public void setMaxInactiveInterval ( final int interval ) {
    }
    @Override
    public void setNew ( final boolean isNew ) {
    }
    @Override
    public void setNote ( final String name, final Object value ) {
    }
    @Override
    public void setPrincipal ( final Principal principal ) {
    }
    @Override
    public void setValid ( final boolean isValid ) {
    }
    @Override
    public void tellChangedSessionId ( final String newId, final String oldId, final boolean notifySessionListeners, final boolean notifyContainerListeners ) {
    }
    @Override
    public boolean isAttributeDistributable ( final String name, final Object value ) {
        return false;
    }
}

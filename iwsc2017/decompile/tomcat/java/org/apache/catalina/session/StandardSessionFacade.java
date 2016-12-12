package org.apache.catalina.session;
import java.util.Enumeration;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
public class StandardSessionFacade implements HttpSession {
    private final HttpSession session;
    public StandardSessionFacade ( final HttpSession session ) {
        this.session = session;
    }
    public long getCreationTime() {
        return this.session.getCreationTime();
    }
    public String getId() {
        return this.session.getId();
    }
    public long getLastAccessedTime() {
        return this.session.getLastAccessedTime();
    }
    public ServletContext getServletContext() {
        return this.session.getServletContext();
    }
    public void setMaxInactiveInterval ( final int interval ) {
        this.session.setMaxInactiveInterval ( interval );
    }
    public int getMaxInactiveInterval() {
        return this.session.getMaxInactiveInterval();
    }
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return this.session.getSessionContext();
    }
    public Object getAttribute ( final String name ) {
        return this.session.getAttribute ( name );
    }
    @Deprecated
    public Object getValue ( final String name ) {
        return this.session.getAttribute ( name );
    }
    public Enumeration<String> getAttributeNames() {
        return ( Enumeration<String> ) this.session.getAttributeNames();
    }
    @Deprecated
    public String[] getValueNames() {
        return this.session.getValueNames();
    }
    public void setAttribute ( final String name, final Object value ) {
        this.session.setAttribute ( name, value );
    }
    @Deprecated
    public void putValue ( final String name, final Object value ) {
        this.session.setAttribute ( name, value );
    }
    public void removeAttribute ( final String name ) {
        this.session.removeAttribute ( name );
    }
    @Deprecated
    public void removeValue ( final String name ) {
        this.session.removeAttribute ( name );
    }
    public void invalidate() {
        this.session.invalidate();
    }
    public boolean isNew() {
        return this.session.isNew();
    }
}

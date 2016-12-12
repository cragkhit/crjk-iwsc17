package javax.servlet.http;
import java.util.Enumeration;
import javax.servlet.ServletContext;
public interface HttpSession {
    public long getCreationTime();
    public String getId();
    public long getLastAccessedTime();
    public ServletContext getServletContext();
    public void setMaxInactiveInterval ( int interval );
    public int getMaxInactiveInterval();
    @Deprecated
    public HttpSessionContext getSessionContext();
    public Object getAttribute ( String name );
    @Deprecated
    public Object getValue ( String name );
    public Enumeration<String> getAttributeNames();
    @Deprecated
    public String[] getValueNames();
    public void setAttribute ( String name, Object value );
    @Deprecated
    public void putValue ( String name, Object value );
    public void removeAttribute ( String name );
    @Deprecated
    public void removeValue ( String name );
    public void invalidate();
    public boolean isNew();
}

package javax.servlet.http;
import java.util.Enumeration;
import javax.servlet.ServletContext;
public interface HttpSession {
    long getCreationTime();
    String getId();
    long getLastAccessedTime();
    ServletContext getServletContext();
    void setMaxInactiveInterval ( int p0 );
    int getMaxInactiveInterval();
    @Deprecated
    HttpSessionContext getSessionContext();
    Object getAttribute ( String p0 );
    @Deprecated
    Object getValue ( String p0 );
    Enumeration<String> getAttributeNames();
    @Deprecated
    String[] getValueNames();
    void setAttribute ( String p0, Object p1 );
    @Deprecated
    void putValue ( String p0, Object p1 );
    void removeAttribute ( String p0 );
    @Deprecated
    void removeValue ( String p0 );
    void invalidate();
    boolean isNew();
}

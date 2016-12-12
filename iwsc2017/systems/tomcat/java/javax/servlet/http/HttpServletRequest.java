package javax.servlet.http;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
public interface HttpServletRequest extends ServletRequest {
    public static final String BASIC_AUTH = "BASIC";
    public static final String FORM_AUTH = "FORM";
    public static final String CLIENT_CERT_AUTH = "CLIENT_CERT";
    public static final String DIGEST_AUTH = "DIGEST";
    public String getAuthType();
    public Cookie[] getCookies();
    public long getDateHeader ( String name );
    public String getHeader ( String name );
    public Enumeration<String> getHeaders ( String name );
    public Enumeration<String> getHeaderNames();
    public int getIntHeader ( String name );
public default Mapping getMapping() {
        return new Mapping() {
            @Override
            public String getMatchValue() {
                return "";
            }
            @Override
            public String getPattern() {
                return "";
            }
            @Override
            public MappingMatch getMappingMatch() {
                return MappingMatch.UNKNOWN;
            }
            @Override
            public String getServletName() {
                return "";
            }
        };
    }
    public String getMethod();
    public String getPathInfo();
    public String getPathTranslated();
public default boolean isPushSupported() {
        return false;
    }
public default PushBuilder getPushBuilder() {
        return null;
    }
    public String getContextPath();
    public String getQueryString();
    public String getRemoteUser();
    public boolean isUserInRole ( String role );
    public java.security.Principal getUserPrincipal();
    public String getRequestedSessionId();
    public String getRequestURI();
    public StringBuffer getRequestURL();
    public String getServletPath();
    public HttpSession getSession ( boolean create );
    public HttpSession getSession();
    public String changeSessionId();
    public boolean isRequestedSessionIdValid();
    public boolean isRequestedSessionIdFromCookie();
    public boolean isRequestedSessionIdFromURL();
    @Deprecated
    public boolean isRequestedSessionIdFromUrl();
    public boolean authenticate ( HttpServletResponse response )
    throws IOException, ServletException;
    public void login ( String username, String password ) throws ServletException;
    public void logout() throws ServletException;
    public Collection<Part> getParts() throws IOException,
               ServletException;
    public Part getPart ( String name ) throws IOException,
               ServletException;
    public <T extends HttpUpgradeHandler> T upgrade (
        Class<T> httpUpgradeHandlerClass ) throws java.io.IOException, ServletException;
}

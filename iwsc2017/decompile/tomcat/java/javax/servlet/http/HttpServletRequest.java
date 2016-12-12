package javax.servlet.http;
import java.util.Collection;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
public interface HttpServletRequest extends ServletRequest {
    public static final String BASIC_AUTH = "BASIC";
    public static final String FORM_AUTH = "FORM";
    public static final String CLIENT_CERT_AUTH = "CLIENT_CERT";
    public static final String DIGEST_AUTH = "DIGEST";
    String getAuthType();
    Cookie[] getCookies();
    long getDateHeader ( String p0 );
    String getHeader ( String p0 );
    Enumeration<String> getHeaders ( String p0 );
    Enumeration<String> getHeaderNames();
    int getIntHeader ( String p0 );
default Mapping getMapping() {
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
    String getMethod();
    String getPathInfo();
    String getPathTranslated();
default boolean isPushSupported() {
        return false;
    }
default PushBuilder getPushBuilder() {
        return null;
    }
    String getContextPath();
    String getQueryString();
    String getRemoteUser();
    boolean isUserInRole ( String p0 );
    Principal getUserPrincipal();
    String getRequestedSessionId();
    String getRequestURI();
    StringBuffer getRequestURL();
    String getServletPath();
    HttpSession getSession ( boolean p0 );
    HttpSession getSession();
    String changeSessionId();
    boolean isRequestedSessionIdValid();
    boolean isRequestedSessionIdFromCookie();
    boolean isRequestedSessionIdFromURL();
    @Deprecated
    boolean isRequestedSessionIdFromUrl();
    boolean authenticate ( HttpServletResponse p0 ) throws IOException, ServletException;
    void login ( String p0, String p1 ) throws ServletException;
    void logout() throws ServletException;
    Collection<Part> getParts() throws IOException, ServletException;
    Part getPart ( String p0 ) throws IOException, ServletException;
    <T extends HttpUpgradeHandler> T upgrade ( Class<T> p0 ) throws IOException, ServletException;
}

package org.apache.catalina.util;
import javax.servlet.SessionCookieConfig;
import org.apache.catalina.Context;
public class SessionConfig {
    private static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    private static final String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";
    public static String getSessionCookieName ( Context context ) {
        String result = getConfiguredSessionCookieName ( context );
        if ( result == null ) {
            result = DEFAULT_SESSION_COOKIE_NAME;
        }
        return result;
    }
    public static String getSessionUriParamName ( Context context ) {
        String result = getConfiguredSessionCookieName ( context );
        if ( result == null ) {
            result = DEFAULT_SESSION_PARAMETER_NAME;
        }
        return result;
    }
    private static String getConfiguredSessionCookieName ( Context context ) {
        if ( context != null ) {
            String cookieName = context.getSessionCookieName();
            if ( cookieName != null && cookieName.length() > 0 ) {
                return cookieName;
            }
            SessionCookieConfig scc =
                context.getServletContext().getSessionCookieConfig();
            cookieName = scc.getName();
            if ( cookieName != null && cookieName.length() > 0 ) {
                return cookieName;
            }
        }
        return null;
    }
    private SessionConfig() {
    }
}

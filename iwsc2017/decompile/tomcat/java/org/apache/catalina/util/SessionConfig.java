package org.apache.catalina.util;
import javax.servlet.SessionCookieConfig;
import org.apache.catalina.Context;
public class SessionConfig {
    private static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    private static final String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";
    public static String getSessionCookieName ( final Context context ) {
        String result = getConfiguredSessionCookieName ( context );
        if ( result == null ) {
            result = "JSESSIONID";
        }
        return result;
    }
    public static String getSessionUriParamName ( final Context context ) {
        String result = getConfiguredSessionCookieName ( context );
        if ( result == null ) {
            result = "jsessionid";
        }
        return result;
    }
    private static String getConfiguredSessionCookieName ( final Context context ) {
        if ( context != null ) {
            String cookieName = context.getSessionCookieName();
            if ( cookieName != null && cookieName.length() > 0 ) {
                return cookieName;
            }
            final SessionCookieConfig scc = context.getServletContext().getSessionCookieConfig();
            cookieName = scc.getName();
            if ( cookieName != null && cookieName.length() > 0 ) {
                return cookieName;
            }
        }
        return null;
    }
}

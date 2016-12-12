package org.apache.catalina.core;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.util.SessionConfig;
import org.apache.tomcat.util.res.StringManager;
public class ApplicationSessionCookieConfig implements SessionCookieConfig {
    private static final StringManager sm = StringManager
                                            .getManager ( Constants.Package );
    private boolean httpOnly;
    private boolean secure;
    private int maxAge = -1;
    private String comment;
    private String domain;
    private String name;
    private String path;
    private StandardContext context;
    public ApplicationSessionCookieConfig ( StandardContext context ) {
        this.context = context;
    }
    @Override
    public String getComment() {
        return comment;
    }
    @Override
    public String getDomain() {
        return domain;
    }
    @Override
    public int getMaxAge() {
        return maxAge;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getPath() {
        return path;
    }
    @Override
    public boolean isHttpOnly() {
        return httpOnly;
    }
    @Override
    public boolean isSecure() {
        return secure;
    }
    @Override
    public void setComment ( String comment ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "comment",
                                                  context.getPath() ) );
        }
        this.comment = comment;
    }
    @Override
    public void setDomain ( String domain ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "domain name",
                                                  context.getPath() ) );
        }
        this.domain = domain;
    }
    @Override
    public void setHttpOnly ( boolean httpOnly ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "HttpOnly",
                                                  context.getPath() ) );
        }
        this.httpOnly = httpOnly;
    }
    @Override
    public void setMaxAge ( int maxAge ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "max age",
                                                  context.getPath() ) );
        }
        this.maxAge = maxAge;
    }
    @Override
    public void setName ( String name ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "name",
                                                  context.getPath() ) );
        }
        this.name = name;
    }
    @Override
    public void setPath ( String path ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "path",
                                                  context.getPath() ) );
        }
        this.path = path;
    }
    @Override
    public void setSecure ( boolean secure ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( sm.getString (
                                                  "applicationSessionCookieConfig.ise", "secure",
                                                  context.getPath() ) );
        }
        this.secure = secure;
    }
    public static Cookie createSessionCookie ( Context context,
            String sessionId, boolean secure ) {
        SessionCookieConfig scc =
            context.getServletContext().getSessionCookieConfig();
        Cookie cookie = new Cookie (
            SessionConfig.getSessionCookieName ( context ), sessionId );
        cookie.setMaxAge ( scc.getMaxAge() );
        cookie.setComment ( scc.getComment() );
        if ( context.getSessionCookieDomain() == null ) {
            if ( scc.getDomain() != null ) {
                cookie.setDomain ( scc.getDomain() );
            }
        } else {
            cookie.setDomain ( context.getSessionCookieDomain() );
        }
        if ( scc.isSecure() || secure ) {
            cookie.setSecure ( true );
        }
        if ( scc.isHttpOnly() || context.getUseHttpOnly() ) {
            cookie.setHttpOnly ( true );
        }
        String contextPath = context.getSessionCookiePath();
        if ( contextPath == null || contextPath.length() == 0 ) {
            contextPath = scc.getPath();
        }
        if ( contextPath == null || contextPath.length() == 0 ) {
            contextPath = context.getEncodedPath();
        }
        if ( context.getSessionCookiePathUsesTrailingSlash() ) {
            if ( !contextPath.endsWith ( "/" ) ) {
                contextPath = contextPath + "/";
            }
        } else {
            if ( contextPath.length() == 0 ) {
                contextPath = "/";
            }
        }
        cookie.setPath ( contextPath );
        return cookie;
    }
}

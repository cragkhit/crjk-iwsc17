package org.apache.tomcat.util.descriptor.web;
import javax.servlet.SessionTrackingMode;
import java.util.EnumSet;
public class SessionConfig {
    private Integer sessionTimeout;
    private String cookieName;
    private String cookieDomain;
    private String cookiePath;
    private String cookieComment;
    private Boolean cookieHttpOnly;
    private Boolean cookieSecure;
    private Integer cookieMaxAge;
    private final EnumSet<SessionTrackingMode> sessionTrackingModes;
    public SessionConfig() {
        this.sessionTrackingModes = EnumSet.noneOf ( SessionTrackingMode.class );
    }
    public Integer getSessionTimeout() {
        return this.sessionTimeout;
    }
    public void setSessionTimeout ( final String sessionTimeout ) {
        this.sessionTimeout = Integer.valueOf ( sessionTimeout );
    }
    public String getCookieName() {
        return this.cookieName;
    }
    public void setCookieName ( final String cookieName ) {
        this.cookieName = cookieName;
    }
    public String getCookieDomain() {
        return this.cookieDomain;
    }
    public void setCookieDomain ( final String cookieDomain ) {
        this.cookieDomain = cookieDomain;
    }
    public String getCookiePath() {
        return this.cookiePath;
    }
    public void setCookiePath ( final String cookiePath ) {
        this.cookiePath = cookiePath;
    }
    public String getCookieComment() {
        return this.cookieComment;
    }
    public void setCookieComment ( final String cookieComment ) {
        this.cookieComment = cookieComment;
    }
    public Boolean getCookieHttpOnly() {
        return this.cookieHttpOnly;
    }
    public void setCookieHttpOnly ( final String cookieHttpOnly ) {
        this.cookieHttpOnly = Boolean.valueOf ( cookieHttpOnly );
    }
    public Boolean getCookieSecure() {
        return this.cookieSecure;
    }
    public void setCookieSecure ( final String cookieSecure ) {
        this.cookieSecure = Boolean.valueOf ( cookieSecure );
    }
    public Integer getCookieMaxAge() {
        return this.cookieMaxAge;
    }
    public void setCookieMaxAge ( final String cookieMaxAge ) {
        this.cookieMaxAge = Integer.valueOf ( cookieMaxAge );
    }
    public EnumSet<SessionTrackingMode> getSessionTrackingModes() {
        return this.sessionTrackingModes;
    }
    public void addSessionTrackingMode ( final String sessionTrackingMode ) {
        this.sessionTrackingModes.add ( SessionTrackingMode.valueOf ( sessionTrackingMode ) );
    }
}

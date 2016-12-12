package org.apache.catalina.valves;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class CrawlerSessionManagerValve extends ValveBase
    implements HttpSessionBindingListener {
    private static final Log log =
        LogFactory.getLog ( CrawlerSessionManagerValve.class );
    private final Map<String, String> clientIpSessionId =
        new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdClientIp =
        new ConcurrentHashMap<>();
    private String crawlerUserAgents =
        ".*[bB]ot.*|.*Yahoo! Slurp.*|.*Feedfetcher-Google.*";
    private Pattern uaPattern = null;
    private int sessionInactiveInterval = 60;
    public CrawlerSessionManagerValve() {
        super ( true );
    }
    public void setCrawlerUserAgents ( String crawlerUserAgents ) {
        this.crawlerUserAgents = crawlerUserAgents;
        if ( crawlerUserAgents == null || crawlerUserAgents.length() == 0 ) {
            uaPattern = null;
        } else {
            uaPattern = Pattern.compile ( crawlerUserAgents );
        }
    }
    public String getCrawlerUserAgents() {
        return crawlerUserAgents;
    }
    public void setSessionInactiveInterval ( int sessionInactiveInterval ) {
        this.sessionInactiveInterval = sessionInactiveInterval;
    }
    public int getSessionInactiveInterval() {
        return sessionInactiveInterval;
    }
    public Map<String, String> getClientIpSessionId() {
        return clientIpSessionId;
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        uaPattern = Pattern.compile ( crawlerUserAgents );
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException,
        ServletException {
        boolean isBot = false;
        String sessionId = null;
        String clientIp = null;
        if ( log.isDebugEnabled() ) {
            log.debug ( request.hashCode() + ": ClientIp=" +
                        request.getRemoteAddr() + ", RequestedSessionId=" +
                        request.getRequestedSessionId() );
        }
        if ( request.getSession ( false ) == null ) {
            Enumeration<String> uaHeaders = request.getHeaders ( "user-agent" );
            String uaHeader = null;
            if ( uaHeaders.hasMoreElements() ) {
                uaHeader = uaHeaders.nextElement();
            }
            if ( uaHeader != null && !uaHeaders.hasMoreElements() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( request.hashCode() + ": UserAgent=" + uaHeader );
                }
                if ( uaPattern.matcher ( uaHeader ).matches() ) {
                    isBot = true;
                    if ( log.isDebugEnabled() ) {
                        log.debug ( request.hashCode() +
                                    ": Bot found. UserAgent=" + uaHeader );
                    }
                }
            }
            if ( isBot ) {
                clientIp = request.getRemoteAddr();
                sessionId = clientIpSessionId.get ( clientIp );
                if ( sessionId != null ) {
                    request.setRequestedSessionId ( sessionId );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( request.hashCode() + ": SessionID=" +
                                    sessionId );
                    }
                }
            }
        }
        getNext().invoke ( request, response );
        if ( isBot ) {
            if ( sessionId == null ) {
                HttpSession s = request.getSession ( false );
                if ( s != null ) {
                    clientIpSessionId.put ( clientIp, s.getId() );
                    sessionIdClientIp.put ( s.getId(), clientIp );
                    s.setAttribute ( this.getClass().getName(), this );
                    s.setMaxInactiveInterval ( sessionInactiveInterval );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( request.hashCode() +
                                    ": New bot session. SessionID=" + s.getId() );
                    }
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( request.hashCode() +
                                ": Bot session accessed. SessionID=" + sessionId );
                }
            }
        }
    }
    @Override
    public void valueUnbound ( HttpSessionBindingEvent event ) {
        String clientIp = sessionIdClientIp.remove ( event.getSession().getId() );
        if ( clientIp != null ) {
            clientIpSessionId.remove ( clientIp );
        }
    }
}

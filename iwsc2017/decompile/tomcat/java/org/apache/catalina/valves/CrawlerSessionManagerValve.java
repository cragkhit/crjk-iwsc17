package org.apache.catalina.valves;
import org.apache.juli.logging.LogFactory;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.LifecycleException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.Map;
import org.apache.juli.logging.Log;
import javax.servlet.http.HttpSessionBindingListener;
public class CrawlerSessionManagerValve extends ValveBase implements HttpSessionBindingListener {
    private static final Log log;
    private final Map<String, String> clientIpSessionId;
    private final Map<String, String> sessionIdClientIp;
    private String crawlerUserAgents;
    private Pattern uaPattern;
    private int sessionInactiveInterval;
    public CrawlerSessionManagerValve() {
        super ( true );
        this.clientIpSessionId = new ConcurrentHashMap<String, String>();
        this.sessionIdClientIp = new ConcurrentHashMap<String, String>();
        this.crawlerUserAgents = ".*[bB]ot.*|.*Yahoo! Slurp.*|.*Feedfetcher-Google.*";
        this.uaPattern = null;
        this.sessionInactiveInterval = 60;
    }
    public void setCrawlerUserAgents ( final String crawlerUserAgents ) {
        this.crawlerUserAgents = crawlerUserAgents;
        if ( crawlerUserAgents == null || crawlerUserAgents.length() == 0 ) {
            this.uaPattern = null;
        } else {
            this.uaPattern = Pattern.compile ( crawlerUserAgents );
        }
    }
    public String getCrawlerUserAgents() {
        return this.crawlerUserAgents;
    }
    public void setSessionInactiveInterval ( final int sessionInactiveInterval ) {
        this.sessionInactiveInterval = sessionInactiveInterval;
    }
    public int getSessionInactiveInterval() {
        return this.sessionInactiveInterval;
    }
    public Map<String, String> getClientIpSessionId() {
        return this.clientIpSessionId;
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.uaPattern = Pattern.compile ( this.crawlerUserAgents );
    }
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        boolean isBot = false;
        String sessionId = null;
        String clientIp = null;
        if ( CrawlerSessionManagerValve.log.isDebugEnabled() ) {
            CrawlerSessionManagerValve.log.debug ( request.hashCode() + ": ClientIp=" + request.getRemoteAddr() + ", RequestedSessionId=" + request.getRequestedSessionId() );
        }
        if ( request.getSession ( false ) == null ) {
            final Enumeration<String> uaHeaders = request.getHeaders ( "user-agent" );
            String uaHeader = null;
            if ( uaHeaders.hasMoreElements() ) {
                uaHeader = uaHeaders.nextElement();
            }
            if ( uaHeader != null && !uaHeaders.hasMoreElements() ) {
                if ( CrawlerSessionManagerValve.log.isDebugEnabled() ) {
                    CrawlerSessionManagerValve.log.debug ( request.hashCode() + ": UserAgent=" + uaHeader );
                }
                if ( this.uaPattern.matcher ( uaHeader ).matches() ) {
                    isBot = true;
                    if ( CrawlerSessionManagerValve.log.isDebugEnabled() ) {
                        CrawlerSessionManagerValve.log.debug ( request.hashCode() + ": Bot found. UserAgent=" + uaHeader );
                    }
                }
            }
            if ( isBot ) {
                clientIp = request.getRemoteAddr();
                sessionId = this.clientIpSessionId.get ( clientIp );
                if ( sessionId != null ) {
                    request.setRequestedSessionId ( sessionId );
                    if ( CrawlerSessionManagerValve.log.isDebugEnabled() ) {
                        CrawlerSessionManagerValve.log.debug ( request.hashCode() + ": SessionID=" + sessionId );
                    }
                }
            }
        }
        this.getNext().invoke ( request, response );
        if ( isBot ) {
            if ( sessionId == null ) {
                final HttpSession s = request.getSession ( false );
                if ( s != null ) {
                    this.clientIpSessionId.put ( clientIp, s.getId() );
                    this.sessionIdClientIp.put ( s.getId(), clientIp );
                    s.setAttribute ( this.getClass().getName(), ( Object ) this );
                    s.setMaxInactiveInterval ( this.sessionInactiveInterval );
                    if ( CrawlerSessionManagerValve.log.isDebugEnabled() ) {
                        CrawlerSessionManagerValve.log.debug ( request.hashCode() + ": New bot session. SessionID=" + s.getId() );
                    }
                }
            } else if ( CrawlerSessionManagerValve.log.isDebugEnabled() ) {
                CrawlerSessionManagerValve.log.debug ( request.hashCode() + ": Bot session accessed. SessionID=" + sessionId );
            }
        }
    }
    public void valueUnbound ( final HttpSessionBindingEvent event ) {
        final String clientIp = this.sessionIdClientIp.remove ( event.getSession().getId() );
        if ( clientIp != null ) {
            this.clientIpSessionId.remove ( clientIp );
        }
    }
    static {
        log = LogFactory.getLog ( CrawlerSessionManagerValve.class );
    }
}

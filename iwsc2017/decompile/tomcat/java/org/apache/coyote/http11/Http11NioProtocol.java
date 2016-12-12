package org.apache.coyote.http11;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractJsseEndpoint;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.NioChannel;
public class Http11NioProtocol extends AbstractHttp11JsseProtocol<NioChannel> {
    private static final Log log;
    public Http11NioProtocol() {
        super ( new NioEndpoint() );
    }
    @Override
    protected Log getLog() {
        return Http11NioProtocol.log;
    }
    public void setPollerThreadCount ( final int count ) {
        ( ( NioEndpoint ) this.getEndpoint() ).setPollerThreadCount ( count );
    }
    public int getPollerThreadCount() {
        return ( ( NioEndpoint ) this.getEndpoint() ).getPollerThreadCount();
    }
    public void setSelectorTimeout ( final long timeout ) {
        ( ( NioEndpoint ) this.getEndpoint() ).setSelectorTimeout ( timeout );
    }
    public long getSelectorTimeout() {
        return ( ( NioEndpoint ) this.getEndpoint() ).getSelectorTimeout();
    }
    public void setPollerThreadPriority ( final int threadPriority ) {
        ( ( NioEndpoint ) this.getEndpoint() ).setPollerThreadPriority ( threadPriority );
    }
    public int getPollerThreadPriority() {
        return ( ( NioEndpoint ) this.getEndpoint() ).getPollerThreadPriority();
    }
    @Override
    protected String getNamePrefix() {
        if ( this.isSSLEnabled() ) {
            return "https-" + this.getSslImplemenationShortName() + "-nio";
        }
        return "http-nio";
    }
    static {
        log = LogFactory.getLog ( Http11NioProtocol.class );
    }
}

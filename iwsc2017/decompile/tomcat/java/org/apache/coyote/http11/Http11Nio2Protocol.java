package org.apache.coyote.http11;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractJsseEndpoint;
import org.apache.tomcat.util.net.Nio2Endpoint;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.Nio2Channel;
public class Http11Nio2Protocol extends AbstractHttp11JsseProtocol<Nio2Channel> {
    private static final Log log;
    public Http11Nio2Protocol() {
        super ( new Nio2Endpoint() );
    }
    @Override
    protected Log getLog() {
        return Http11Nio2Protocol.log;
    }
    @Override
    protected String getNamePrefix() {
        if ( this.isSSLEnabled() ) {
            return "https-" + this.getSslImplemenationShortName() + "-nio2";
        }
        return "http-nio2";
    }
    static {
        log = LogFactory.getLog ( Http11Nio2Protocol.class );
    }
}

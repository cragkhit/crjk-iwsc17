package org.apache.coyote.ajp;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.Nio2Endpoint;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.Nio2Channel;
public class AjpNio2Protocol extends AbstractAjpProtocol<Nio2Channel> {
    private static final Log log;
    @Override
    protected Log getLog() {
        return AjpNio2Protocol.log;
    }
    public AjpNio2Protocol() {
        super ( new Nio2Endpoint() );
    }
    @Override
    protected String getNamePrefix() {
        return "ajp-nio2";
    }
    static {
        log = LogFactory.getLog ( AjpNio2Protocol.class );
    }
}

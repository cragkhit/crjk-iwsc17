package org.apache.coyote.ajp;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.NioChannel;
public class AjpNioProtocol extends AbstractAjpProtocol<NioChannel> {
    private static final Log log;
    @Override
    protected Log getLog() {
        return AjpNioProtocol.log;
    }
    public AjpNioProtocol() {
        super ( new NioEndpoint() );
    }
    @Override
    protected String getNamePrefix() {
        return "ajp-nio";
    }
    static {
        log = LogFactory.getLog ( AjpNioProtocol.class );
    }
}

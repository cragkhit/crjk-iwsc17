package org.apache.coyote.ajp;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.juli.logging.Log;
public class AjpAprProtocol extends AbstractAjpProtocol<Long> {
    private static final Log log;
    @Override
    protected Log getLog() {
        return AjpAprProtocol.log;
    }
    @Override
    public boolean isAprRequired() {
        return true;
    }
    public AjpAprProtocol() {
        super ( new AprEndpoint() );
    }
    public int getPollTime() {
        return ( ( AprEndpoint ) this.getEndpoint() ).getPollTime();
    }
    public void setPollTime ( final int pollTime ) {
        ( ( AprEndpoint ) this.getEndpoint() ).setPollTime ( pollTime );
    }
    @Override
    protected String getNamePrefix() {
        return "ajp-apr";
    }
    static {
        log = LogFactory.getLog ( AjpAprProtocol.class );
    }
}

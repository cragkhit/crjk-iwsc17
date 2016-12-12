package org.apache.coyote.ajp;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AprEndpoint;
public class AjpAprProtocol extends AbstractAjpProtocol<Long> {
    private static final Log log = LogFactory.getLog ( AjpAprProtocol.class );
    @Override
    protected Log getLog() {
        return log;
    }
    @Override
    public boolean isAprRequired() {
        return true;
    }
    public AjpAprProtocol() {
        super ( new AprEndpoint() );
    }
    public int getPollTime() {
        return ( ( AprEndpoint ) getEndpoint() ).getPollTime();
    }
    public void setPollTime ( int pollTime ) {
        ( ( AprEndpoint ) getEndpoint() ).setPollTime ( pollTime );
    }
    @Override
    protected String getNamePrefix() {
        return ( "ajp-apr" );
    }
}

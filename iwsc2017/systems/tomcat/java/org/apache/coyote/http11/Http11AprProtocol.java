package org.apache.coyote.http11;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AprEndpoint;
public class Http11AprProtocol extends AbstractHttp11Protocol<Long> {
    private static final Log log = LogFactory.getLog ( Http11AprProtocol.class );
    public Http11AprProtocol() {
        super ( new AprEndpoint() );
    }
    @Override
    protected Log getLog() {
        return log;
    }
    @Override
    public boolean isAprRequired() {
        return true;
    }
    public int getPollTime() {
        return ( ( AprEndpoint ) getEndpoint() ).getPollTime();
    }
    public void setPollTime ( int pollTime ) {
        ( ( AprEndpoint ) getEndpoint() ).setPollTime ( pollTime );
    }
    public int getSendfileSize() {
        return ( ( AprEndpoint ) getEndpoint() ).getSendfileSize();
    }
    public void setSendfileSize ( int sendfileSize ) {
        ( ( AprEndpoint ) getEndpoint() ).setSendfileSize ( sendfileSize );
    }
    public boolean getDeferAccept() {
        return ( ( AprEndpoint ) getEndpoint() ).getDeferAccept();
    }
    public void setDeferAccept ( boolean deferAccept ) {
        ( ( AprEndpoint ) getEndpoint() ).setDeferAccept ( deferAccept );
    }
    @Override
    protected String getNamePrefix() {
        if ( isSSLEnabled() ) {
            return ( "https-openssl-apr" );
        } else {
            return ( "http-apr" );
        }
    }
}

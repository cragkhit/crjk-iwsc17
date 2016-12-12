package org.apache.coyote.http11;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.juli.logging.Log;
public class Http11AprProtocol extends AbstractHttp11Protocol<Long> {
    private static final Log log;
    public Http11AprProtocol() {
        super ( new AprEndpoint() );
    }
    @Override
    protected Log getLog() {
        return Http11AprProtocol.log;
    }
    @Override
    public boolean isAprRequired() {
        return true;
    }
    public int getPollTime() {
        return ( ( AprEndpoint ) this.getEndpoint() ).getPollTime();
    }
    public void setPollTime ( final int pollTime ) {
        ( ( AprEndpoint ) this.getEndpoint() ).setPollTime ( pollTime );
    }
    public int getSendfileSize() {
        return ( ( AprEndpoint ) this.getEndpoint() ).getSendfileSize();
    }
    public void setSendfileSize ( final int sendfileSize ) {
        ( ( AprEndpoint ) this.getEndpoint() ).setSendfileSize ( sendfileSize );
    }
    public boolean getDeferAccept() {
        return ( ( AprEndpoint ) this.getEndpoint() ).getDeferAccept();
    }
    public void setDeferAccept ( final boolean deferAccept ) {
        ( ( AprEndpoint ) this.getEndpoint() ).setDeferAccept ( deferAccept );
    }
    @Override
    protected String getNamePrefix() {
        if ( this.isSSLEnabled() ) {
            return "https-openssl-apr";
        }
        return "http-apr";
    }
    static {
        log = LogFactory.getLog ( Http11AprProtocol.class );
    }
}

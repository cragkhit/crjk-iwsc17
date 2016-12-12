package org.apache.coyote.http11;
import org.apache.tomcat.util.net.openssl.OpenSSLImplementation;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AbstractJsseEndpoint;
public abstract class AbstractHttp11JsseProtocol<S> extends AbstractHttp11Protocol<S> {
    public AbstractHttp11JsseProtocol ( final AbstractJsseEndpoint<S> endpoint ) {
        super ( endpoint );
    }
    @Override
    protected AbstractJsseEndpoint<S> getEndpoint() {
        return ( AbstractJsseEndpoint<S> ) ( AbstractJsseEndpoint ) super.getEndpoint();
    }
    protected String getSslImplemenationShortName() {
        if ( OpenSSLImplementation.class.getName().equals ( this.getSslImplementationName() ) ) {
            return "openssl";
        }
        return "jsse";
    }
    public String getSslImplementationName() {
        return this.getEndpoint().getSslImplementationName();
    }
    public void setSslImplementationName ( final String s ) {
        this.getEndpoint().setSslImplementationName ( s );
    }
    public int getSniParseLimit() {
        return this.getEndpoint().getSniParseLimit();
    }
    public void setSniParseLimit ( final int sniParseLimit ) {
        this.getEndpoint().setSniParseLimit ( sniParseLimit );
    }
}

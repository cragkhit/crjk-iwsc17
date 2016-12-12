package org.apache.catalina.core;
import java.util.Locale;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
class ApplicationResponse extends ServletResponseWrapper {
    protected boolean included;
    public ApplicationResponse ( final ServletResponse response, final boolean included ) {
        super ( response );
        this.included = false;
        this.setIncluded ( included );
    }
    public void reset() {
        if ( !this.included || this.getResponse().isCommitted() ) {
            this.getResponse().reset();
        }
    }
    public void setContentLength ( final int len ) {
        if ( !this.included ) {
            this.getResponse().setContentLength ( len );
        }
    }
    public void setContentLengthLong ( final long len ) {
        if ( !this.included ) {
            this.getResponse().setContentLengthLong ( len );
        }
    }
    public void setContentType ( final String type ) {
        if ( !this.included ) {
            this.getResponse().setContentType ( type );
        }
    }
    public void setLocale ( final Locale loc ) {
        if ( !this.included ) {
            this.getResponse().setLocale ( loc );
        }
    }
    public void setBufferSize ( final int size ) {
        if ( !this.included ) {
            this.getResponse().setBufferSize ( size );
        }
    }
    public void setResponse ( final ServletResponse response ) {
        super.setResponse ( response );
    }
    void setIncluded ( final boolean included ) {
        this.included = included;
    }
}

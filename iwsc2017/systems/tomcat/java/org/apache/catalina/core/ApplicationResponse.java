package org.apache.catalina.core;
import java.util.Locale;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
class ApplicationResponse extends ServletResponseWrapper {
    public ApplicationResponse ( ServletResponse response, boolean included ) {
        super ( response );
        setIncluded ( included );
    }
    protected boolean included = false;
    @Override
    public void reset() {
        if ( !included || getResponse().isCommitted() ) {
            getResponse().reset();
        }
    }
    @Override
    public void setContentLength ( int len ) {
        if ( !included ) {
            getResponse().setContentLength ( len );
        }
    }
    @Override
    public void setContentLengthLong ( long len ) {
        if ( !included ) {
            getResponse().setContentLengthLong ( len );
        }
    }
    @Override
    public void setContentType ( String type ) {
        if ( !included ) {
            getResponse().setContentType ( type );
        }
    }
    @Override
    public void setLocale ( Locale loc ) {
        if ( !included ) {
            getResponse().setLocale ( loc );
        }
    }
    @Override
    public void setBufferSize ( int size ) {
        if ( !included ) {
            getResponse().setBufferSize ( size );
        }
    }
    @Override
    public void setResponse ( ServletResponse response ) {
        super.setResponse ( response );
    }
    void setIncluded ( boolean included ) {
        this.included = included;
    }
}

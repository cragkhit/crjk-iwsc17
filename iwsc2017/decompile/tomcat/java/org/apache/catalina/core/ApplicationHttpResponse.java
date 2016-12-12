package org.apache.catalina.core;
import javax.servlet.ServletResponse;
import java.io.IOException;
import javax.servlet.http.Cookie;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
class ApplicationHttpResponse extends HttpServletResponseWrapper {
    protected boolean included;
    public ApplicationHttpResponse ( final HttpServletResponse response, final boolean included ) {
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
    public void addCookie ( final Cookie cookie ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).addCookie ( cookie );
        }
    }
    public void addDateHeader ( final String name, final long value ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).addDateHeader ( name, value );
        }
    }
    public void addHeader ( final String name, final String value ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).addHeader ( name, value );
        }
    }
    public void addIntHeader ( final String name, final int value ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).addIntHeader ( name, value );
        }
    }
    public void sendError ( final int sc ) throws IOException {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).sendError ( sc );
        }
    }
    public void sendError ( final int sc, final String msg ) throws IOException {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).sendError ( sc, msg );
        }
    }
    public void sendRedirect ( final String location ) throws IOException {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).sendRedirect ( location );
        }
    }
    public void setDateHeader ( final String name, final long value ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).setDateHeader ( name, value );
        }
    }
    public void setHeader ( final String name, final String value ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).setHeader ( name, value );
        }
    }
    public void setIntHeader ( final String name, final int value ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).setIntHeader ( name, value );
        }
    }
    public void setStatus ( final int sc ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).setStatus ( sc );
        }
    }
    @Deprecated
    public void setStatus ( final int sc, final String msg ) {
        if ( !this.included ) {
            ( ( HttpServletResponse ) this.getResponse() ).setStatus ( sc, msg );
        }
    }
    void setIncluded ( final boolean included ) {
        this.included = included;
    }
    void setResponse ( final HttpServletResponse response ) {
        super.setResponse ( ( ServletResponse ) response );
    }
}

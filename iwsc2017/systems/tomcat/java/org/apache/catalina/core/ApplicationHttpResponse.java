package org.apache.catalina.core;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
class ApplicationHttpResponse extends HttpServletResponseWrapper {
    public ApplicationHttpResponse ( HttpServletResponse response,
                                     boolean included ) {
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
    public void addCookie ( Cookie cookie ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).addCookie ( cookie );
        }
    }
    @Override
    public void addDateHeader ( String name, long value ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).addDateHeader ( name, value );
        }
    }
    @Override
    public void addHeader ( String name, String value ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).addHeader ( name, value );
        }
    }
    @Override
    public void addIntHeader ( String name, int value ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).addIntHeader ( name, value );
        }
    }
    @Override
    public void sendError ( int sc ) throws IOException {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).sendError ( sc );
        }
    }
    @Override
    public void sendError ( int sc, String msg ) throws IOException {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).sendError ( sc, msg );
        }
    }
    @Override
    public void sendRedirect ( String location ) throws IOException {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).sendRedirect ( location );
        }
    }
    @Override
    public void setDateHeader ( String name, long value ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).setDateHeader ( name, value );
        }
    }
    @Override
    public void setHeader ( String name, String value ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).setHeader ( name, value );
        }
    }
    @Override
    public void setIntHeader ( String name, int value ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).setIntHeader ( name, value );
        }
    }
    @Override
    public void setStatus ( int sc ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).setStatus ( sc );
        }
    }
    @Deprecated
    @Override
    public void setStatus ( int sc, String msg ) {
        if ( !included ) {
            ( ( HttpServletResponse ) getResponse() ).setStatus ( sc, msg );
        }
    }
    void setIncluded ( boolean included ) {
        this.included = included;
    }
    void setResponse ( HttpServletResponse response ) {
        super.setResponse ( response );
    }
}

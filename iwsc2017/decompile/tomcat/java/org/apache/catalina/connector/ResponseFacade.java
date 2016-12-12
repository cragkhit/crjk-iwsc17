package org.apache.catalina.connector;
import java.util.Collection;
import org.apache.catalina.Globals;
import javax.servlet.http.Cookie;
import java.util.Locale;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.catalina.security.SecurityUtil;
import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.http.HttpServletResponse;
public class ResponseFacade implements HttpServletResponse {
    protected static final StringManager sm;
    protected Response response;
    public ResponseFacade ( final Response response ) {
        this.response = null;
        this.response = response;
    }
    public void clear() {
        this.response = null;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    public void finish() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        this.response.setSuspended ( true );
    }
    public boolean isFinished() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.isSuspended();
    }
    public long getContentWritten() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.getContentWritten();
    }
    public String getCharacterEncoding() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.getCharacterEncoding();
    }
    public ServletOutputStream getOutputStream() throws IOException {
        final ServletOutputStream sos = this.response.getOutputStream();
        if ( this.isFinished() ) {
            this.response.setSuspended ( true );
        }
        return sos;
    }
    public PrintWriter getWriter() throws IOException {
        final PrintWriter writer = this.response.getWriter();
        if ( this.isFinished() ) {
            this.response.setSuspended ( true );
        }
        return writer;
    }
    public void setContentLength ( final int len ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setContentLength ( len );
    }
    public void setContentLengthLong ( final long length ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setContentLengthLong ( length );
    }
    public void setContentType ( final String type ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new SetContentTypePrivilegedAction ( type ) );
        } else {
            this.response.setContentType ( type );
        }
    }
    public void setBufferSize ( final int size ) {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "coyoteResponse.setBufferSize.ise" ) );
        }
        this.response.setBufferSize ( size );
    }
    public int getBufferSize() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.getBufferSize();
    }
    public void flushBuffer() throws IOException {
        if ( this.isFinished() ) {
            return;
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws IOException {
                        ResponseFacade.this.response.setAppCommitted ( true );
                        ResponseFacade.this.response.flushBuffer();
                        return null;
                    }
                } );
            } catch ( PrivilegedActionException e ) {
                final Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                }
            }
        } else {
            this.response.setAppCommitted ( true );
            this.response.flushBuffer();
        }
    }
    public void resetBuffer() {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "coyoteResponse.resetBuffer.ise" ) );
        }
        this.response.resetBuffer();
    }
    public boolean isCommitted() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.isAppCommitted();
    }
    public void reset() {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "coyoteResponse.reset.ise" ) );
        }
        this.response.reset();
    }
    public void setLocale ( final Locale loc ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setLocale ( loc );
    }
    public Locale getLocale() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.getLocale();
    }
    public void addCookie ( final Cookie cookie ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.addCookie ( cookie );
    }
    public boolean containsHeader ( final String name ) {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.containsHeader ( name );
    }
    public String encodeURL ( final String url ) {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.encodeURL ( url );
    }
    public String encodeRedirectURL ( final String url ) {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.encodeRedirectURL ( url );
    }
    public String encodeUrl ( final String url ) {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.encodeURL ( url );
    }
    public String encodeRedirectUrl ( final String url ) {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.encodeRedirectURL ( url );
    }
    public void sendError ( final int sc, final String msg ) throws IOException {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "coyoteResponse.sendError.ise" ) );
        }
        this.response.setAppCommitted ( true );
        this.response.sendError ( sc, msg );
    }
    public void sendError ( final int sc ) throws IOException {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "coyoteResponse.sendError.ise" ) );
        }
        this.response.setAppCommitted ( true );
        this.response.sendError ( sc );
    }
    public void sendRedirect ( final String location ) throws IOException {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "coyoteResponse.sendRedirect.ise" ) );
        }
        this.response.setAppCommitted ( true );
        this.response.sendRedirect ( location );
    }
    public void setDateHeader ( final String name, final long date ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new DateHeaderPrivilegedAction ( name, date, false ) );
        } else {
            this.response.setDateHeader ( name, date );
        }
    }
    public void addDateHeader ( final String name, final long date ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new DateHeaderPrivilegedAction ( name, date, true ) );
        } else {
            this.response.addDateHeader ( name, date );
        }
    }
    public void setHeader ( final String name, final String value ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setHeader ( name, value );
    }
    public void addHeader ( final String name, final String value ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.addHeader ( name, value );
    }
    public void setIntHeader ( final String name, final int value ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setIntHeader ( name, value );
    }
    public void addIntHeader ( final String name, final int value ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.addIntHeader ( name, value );
    }
    public void setStatus ( final int sc ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setStatus ( sc );
    }
    public void setStatus ( final int sc, final String sm ) {
        if ( this.isCommitted() ) {
            return;
        }
        this.response.setStatus ( sc, sm );
    }
    public String getContentType() {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        return this.response.getContentType();
    }
    public void setCharacterEncoding ( final String arg0 ) {
        if ( this.response == null ) {
            throw new IllegalStateException ( ResponseFacade.sm.getString ( "responseFacade.nullResponse" ) );
        }
        this.response.setCharacterEncoding ( arg0 );
    }
    public int getStatus() {
        return this.response.getStatus();
    }
    public String getHeader ( final String name ) {
        return this.response.getHeader ( name );
    }
    public Collection<String> getHeaderNames() {
        return this.response.getHeaderNames();
    }
    public Collection<String> getHeaders ( final String name ) {
        return this.response.getHeaders ( name );
    }
    static {
        sm = StringManager.getManager ( ResponseFacade.class );
    }
    private final class SetContentTypePrivilegedAction implements PrivilegedAction<Void> {
        private final String contentType;
        public SetContentTypePrivilegedAction ( final String contentType ) {
            this.contentType = contentType;
        }
        @Override
        public Void run() {
            ResponseFacade.this.response.setContentType ( this.contentType );
            return null;
        }
    }
    private final class DateHeaderPrivilegedAction implements PrivilegedAction<Void> {
        private final String name;
        private final long value;
        private final boolean add;
        DateHeaderPrivilegedAction ( final String name, final long value, final boolean add ) {
            this.name = name;
            this.value = value;
            this.add = add;
        }
        @Override
        public Void run() {
            if ( this.add ) {
                ResponseFacade.this.response.addDateHeader ( this.name, this.value );
            } else {
                ResponseFacade.this.response.setDateHeader ( this.name, this.value );
            }
            return null;
        }
    }
}

package javax.servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
public class ServletResponseWrapper implements ServletResponse {
    private ServletResponse response;
    public ServletResponseWrapper ( ServletResponse response ) {
        if ( response == null ) {
            throw new IllegalArgumentException ( "Response cannot be null" );
        }
        this.response = response;
    }
    public ServletResponse getResponse() {
        return this.response;
    }
    public void setResponse ( ServletResponse response ) {
        if ( response == null ) {
            throw new IllegalArgumentException ( "Response cannot be null" );
        }
        this.response = response;
    }
    @Override
    public void setCharacterEncoding ( String charset ) {
        this.response.setCharacterEncoding ( charset );
    }
    @Override
    public String getCharacterEncoding() {
        return this.response.getCharacterEncoding();
    }
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.response.getOutputStream();
    }
    @Override
    public PrintWriter getWriter() throws IOException {
        return this.response.getWriter();
    }
    @Override
    public void setContentLength ( int len ) {
        this.response.setContentLength ( len );
    }
    @Override
    public void setContentLengthLong ( long length ) {
        this.response.setContentLengthLong ( length );
    }
    @Override
    public void setContentType ( String type ) {
        this.response.setContentType ( type );
    }
    @Override
    public String getContentType() {
        return this.response.getContentType();
    }
    @Override
    public void setBufferSize ( int size ) {
        this.response.setBufferSize ( size );
    }
    @Override
    public int getBufferSize() {
        return this.response.getBufferSize();
    }
    @Override
    public void flushBuffer() throws IOException {
        this.response.flushBuffer();
    }
    @Override
    public boolean isCommitted() {
        return this.response.isCommitted();
    }
    @Override
    public void reset() {
        this.response.reset();
    }
    @Override
    public void resetBuffer() {
        this.response.resetBuffer();
    }
    @Override
    public void setLocale ( Locale loc ) {
        this.response.setLocale ( loc );
    }
    @Override
    public Locale getLocale() {
        return this.response.getLocale();
    }
    public boolean isWrapperFor ( ServletResponse wrapped ) {
        if ( response == wrapped ) {
            return true;
        }
        if ( response instanceof ServletResponseWrapper ) {
            return ( ( ServletResponseWrapper ) response ).isWrapperFor ( wrapped );
        }
        return false;
    }
    public boolean isWrapperFor ( Class<?> wrappedType ) {
        if ( wrappedType.isAssignableFrom ( response.getClass() ) ) {
            return true;
        }
        if ( response instanceof ServletResponseWrapper ) {
            return ( ( ServletResponseWrapper ) response ).isWrapperFor ( wrappedType );
        }
        return false;
    }
}

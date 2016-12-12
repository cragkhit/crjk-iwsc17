package javax.servlet;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;
public class ServletResponseWrapper implements ServletResponse {
    private ServletResponse response;
    public ServletResponseWrapper ( final ServletResponse response ) {
        if ( response == null ) {
            throw new IllegalArgumentException ( "Response cannot be null" );
        }
        this.response = response;
    }
    public ServletResponse getResponse() {
        return this.response;
    }
    public void setResponse ( final ServletResponse response ) {
        if ( response == null ) {
            throw new IllegalArgumentException ( "Response cannot be null" );
        }
        this.response = response;
    }
    @Override
    public void setCharacterEncoding ( final String charset ) {
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
    public void setContentLength ( final int len ) {
        this.response.setContentLength ( len );
    }
    @Override
    public void setContentLengthLong ( final long length ) {
        this.response.setContentLengthLong ( length );
    }
    @Override
    public void setContentType ( final String type ) {
        this.response.setContentType ( type );
    }
    @Override
    public String getContentType() {
        return this.response.getContentType();
    }
    @Override
    public void setBufferSize ( final int size ) {
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
    public void setLocale ( final Locale loc ) {
        this.response.setLocale ( loc );
    }
    @Override
    public Locale getLocale() {
        return this.response.getLocale();
    }
    public boolean isWrapperFor ( final ServletResponse wrapped ) {
        return this.response == wrapped || ( this.response instanceof ServletResponseWrapper && ( ( ServletResponseWrapper ) this.response ).isWrapperFor ( wrapped ) );
    }
    public boolean isWrapperFor ( final Class<?> wrappedType ) {
        return wrappedType.isAssignableFrom ( this.response.getClass() ) || ( this.response instanceof ServletResponseWrapper && ( ( ServletResponseWrapper ) this.response ).isWrapperFor ( wrappedType ) );
    }
}

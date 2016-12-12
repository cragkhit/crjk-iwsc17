package javax.servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
public interface ServletResponse {
    public String getCharacterEncoding();
    public String getContentType();
    public ServletOutputStream getOutputStream() throws IOException;
    public PrintWriter getWriter() throws IOException;
    public void setCharacterEncoding ( String charset );
    public void setContentLength ( int len );
    public void setContentLengthLong ( long length );
    public void setContentType ( String type );
    public void setBufferSize ( int size );
    public int getBufferSize();
    public void flushBuffer() throws IOException;
    public void resetBuffer();
    public boolean isCommitted();
    public void reset();
    public void setLocale ( Locale loc );
    public Locale getLocale();
}

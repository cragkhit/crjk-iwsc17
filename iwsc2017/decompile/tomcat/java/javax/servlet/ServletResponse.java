package javax.servlet;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;
public interface ServletResponse {
    String getCharacterEncoding();
    String getContentType();
    ServletOutputStream getOutputStream() throws IOException;
    PrintWriter getWriter() throws IOException;
    void setCharacterEncoding ( String p0 );
    void setContentLength ( int p0 );
    void setContentLengthLong ( long p0 );
    void setContentType ( String p0 );
    void setBufferSize ( int p0 );
    int getBufferSize();
    void flushBuffer() throws IOException;
    void resetBuffer();
    boolean isCommitted();
    void reset();
    void setLocale ( Locale p0 );
    Locale getLocale();
}

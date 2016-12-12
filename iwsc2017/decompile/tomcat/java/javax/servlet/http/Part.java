package javax.servlet.http;
import java.util.Collection;
import java.io.IOException;
import java.io.InputStream;
public interface Part {
    InputStream getInputStream() throws IOException;
    String getContentType();
    String getName();
    String getSubmittedFileName();
    long getSize();
    void write ( String p0 ) throws IOException;
    void delete() throws IOException;
    String getHeader ( String p0 );
    Collection<String> getHeaders ( String p0 );
    Collection<String> getHeaderNames();
}

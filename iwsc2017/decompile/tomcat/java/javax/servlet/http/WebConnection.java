package javax.servlet.http;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;
public interface WebConnection extends AutoCloseable {
    ServletInputStream getInputStream() throws IOException;
    ServletOutputStream getOutputStream() throws IOException;
}

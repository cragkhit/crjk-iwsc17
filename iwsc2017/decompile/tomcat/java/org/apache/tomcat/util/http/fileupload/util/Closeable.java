package org.apache.tomcat.util.http.fileupload.util;
import java.io.IOException;
public interface Closeable {
    void close() throws IOException;
    boolean isClosed() throws IOException;
}

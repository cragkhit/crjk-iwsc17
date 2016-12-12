package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
import java.io.InputStream;
public interface FileItemStream extends FileItemHeadersSupport {
    public static class ItemSkippedException extends IOException {
        private static final long serialVersionUID = -7280778431581963740L;
    }
    InputStream openStream() throws IOException;
    String getContentType();
    String getName();
    String getFieldName();
    boolean isFormField();
}

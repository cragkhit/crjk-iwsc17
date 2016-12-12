package org.apache.tomcat.util.http.fileupload.servlet;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.UploadContext;
public class ServletRequestContext implements UploadContext {
    private final HttpServletRequest request;
    public ServletRequestContext ( HttpServletRequest request ) {
        this.request = request;
    }
    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }
    @Override
    public String getContentType() {
        return request.getContentType();
    }
    @Override
    public long contentLength() {
        long size;
        try {
            size = Long.parseLong ( request.getHeader ( FileUploadBase.CONTENT_LENGTH ) );
        } catch ( NumberFormatException e ) {
            size = request.getContentLength();
        }
        return size;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }
    @Override
    public String toString() {
        return String.format ( "ContentLength=%s, ContentType=%s",
                               Long.valueOf ( this.contentLength() ),
                               this.getContentType() );
    }
}

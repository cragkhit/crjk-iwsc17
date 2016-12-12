package org.apache.tomcat.util.http.fileupload;
import java.util.Iterator;
public interface FileItemHeaders {
    String getHeader ( String p0 );
    Iterator<String> getHeaders ( String p0 );
    Iterator<String> getHeaderNames();
}

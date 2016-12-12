package org.apache.tomcat.util.http.fileupload;
import java.util.Iterator;
public interface FileItemHeaders {
    String getHeader ( String name );
    Iterator<String> getHeaders ( String name );
    Iterator<String> getHeaderNames();
}

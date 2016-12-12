package org.apache.tomcat.util.http.fileupload;
public interface ProgressListener {
    void update ( long pBytesRead, long pContentLength, int pItems );
}

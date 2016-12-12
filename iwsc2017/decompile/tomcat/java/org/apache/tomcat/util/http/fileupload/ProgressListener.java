package org.apache.tomcat.util.http.fileupload;
public interface ProgressListener {
    void update ( long p0, long p1, int p2 );
}

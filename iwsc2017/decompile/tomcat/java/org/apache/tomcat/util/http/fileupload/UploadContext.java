package org.apache.tomcat.util.http.fileupload;
public interface UploadContext extends RequestContext {
    long contentLength();
}

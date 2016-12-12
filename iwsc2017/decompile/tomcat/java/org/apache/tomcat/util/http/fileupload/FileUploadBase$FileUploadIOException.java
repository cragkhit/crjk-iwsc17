package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
public static class FileUploadIOException extends IOException {
    private static final long serialVersionUID = -3082868232248803474L;
    public FileUploadIOException() {
    }
    public FileUploadIOException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
    public FileUploadIOException ( final String message ) {
        super ( message );
    }
    public FileUploadIOException ( final Throwable cause ) {
        super ( cause );
    }
}

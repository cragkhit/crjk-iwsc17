package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
public static class IllegalBoundaryException extends IOException {
    private static final long serialVersionUID = -161533165102632918L;
    public IllegalBoundaryException() {
    }
    public IllegalBoundaryException ( final String message ) {
        super ( message );
    }
}

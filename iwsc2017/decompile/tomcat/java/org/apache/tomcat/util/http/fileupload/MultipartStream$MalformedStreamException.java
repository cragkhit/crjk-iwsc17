package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
public static class MalformedStreamException extends IOException {
    private static final long serialVersionUID = 6466926458059796677L;
    public MalformedStreamException() {
    }
    public MalformedStreamException ( final String message ) {
        super ( message );
    }
}

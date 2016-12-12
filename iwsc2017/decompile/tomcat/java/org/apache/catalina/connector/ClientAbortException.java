package org.apache.catalina.connector;
import java.io.IOException;
public final class ClientAbortException extends IOException {
    private static final long serialVersionUID = 1L;
    public ClientAbortException() {
    }
    public ClientAbortException ( final String message ) {
        super ( message );
    }
    public ClientAbortException ( final Throwable throwable ) {
        super ( throwable );
    }
    public ClientAbortException ( final String message, final Throwable throwable ) {
        super ( message, throwable );
    }
}

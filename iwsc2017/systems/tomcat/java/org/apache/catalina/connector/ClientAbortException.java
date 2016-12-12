package org.apache.catalina.connector;
import java.io.IOException;
public final class ClientAbortException extends IOException {
    private static final long serialVersionUID = 1L;
    public ClientAbortException() {
        super();
    }
    public ClientAbortException ( String message ) {
        super ( message );
    }
    public ClientAbortException ( Throwable throwable ) {
        super ( throwable );
    }
    public ClientAbortException ( String message, Throwable throwable ) {
        super ( message, throwable );
    }
}

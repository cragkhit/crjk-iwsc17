package org.apache.coyote;
import java.io.IOException;
public class CloseNowException extends IOException {
    private static final long serialVersionUID = 1L;
    public CloseNowException() {
    }
    public CloseNowException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
    public CloseNowException ( final String message ) {
        super ( message );
    }
    public CloseNowException ( final Throwable cause ) {
        super ( cause );
    }
}

package org.apache.coyote;
import java.io.IOException;
public class CloseNowException extends IOException {
    private static final long serialVersionUID = 1L;
    public CloseNowException() {
        super();
    }
    public CloseNowException ( String message, Throwable cause ) {
        super ( message, cause );
    }
    public CloseNowException ( String message ) {
        super ( message );
    }
    public CloseNowException ( Throwable cause ) {
        super ( cause );
    }
}

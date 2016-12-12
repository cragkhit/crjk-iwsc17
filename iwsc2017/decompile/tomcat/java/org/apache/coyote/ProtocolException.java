package org.apache.coyote;
public class ProtocolException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ProtocolException() {
    }
    public ProtocolException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
    public ProtocolException ( final String message ) {
        super ( message );
    }
    public ProtocolException ( final Throwable cause ) {
        super ( cause );
    }
}

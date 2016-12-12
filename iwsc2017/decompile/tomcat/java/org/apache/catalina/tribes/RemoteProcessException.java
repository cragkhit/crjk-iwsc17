package org.apache.catalina.tribes;
public class RemoteProcessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public RemoteProcessException() {
    }
    public RemoteProcessException ( final String message ) {
        super ( message );
    }
    public RemoteProcessException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
    public RemoteProcessException ( final Throwable cause ) {
        super ( cause );
    }
}

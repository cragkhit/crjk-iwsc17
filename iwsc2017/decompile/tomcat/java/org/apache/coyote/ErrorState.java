package org.apache.coyote;
public enum ErrorState {
    NONE ( false, 0, true, true ),
    CLOSE_CLEAN ( true, 1, true, true ),
    CLOSE_NOW ( true, 2, false, true ),
    CLOSE_CONNECTION_NOW ( true, 3, false, false );
    private final boolean error;
    private final int severity;
    private final boolean ioAllowed;
    private final boolean connectionIoAllowed;
    private ErrorState ( final boolean error, final int severity, final boolean ioAllowed, final boolean connectionIoAllowed ) {
        this.error = error;
        this.severity = severity;
        this.ioAllowed = ioAllowed;
        this.connectionIoAllowed = connectionIoAllowed;
    }
    public boolean isError() {
        return this.error;
    }
    public ErrorState getMostSevere ( final ErrorState input ) {
        if ( input.severity > this.severity ) {
            return input;
        }
        return this;
    }
    public boolean isIoAllowed() {
        return this.ioAllowed;
    }
    public boolean isConnectionIoAllowed() {
        return this.connectionIoAllowed;
    }
}

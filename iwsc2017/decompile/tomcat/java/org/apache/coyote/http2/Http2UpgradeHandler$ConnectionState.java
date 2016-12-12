package org.apache.coyote.http2;
private enum ConnectionState {
    NEW ( true ),
    CONNECTED ( true ),
    PAUSING ( true ),
    PAUSED ( false ),
    CLOSED ( false );
    private final boolean newStreamsAllowed;
    private ConnectionState ( final boolean newStreamsAllowed ) {
        this.newStreamsAllowed = newStreamsAllowed;
    }
    public boolean isNewStreamAllowed() {
        return this.newStreamsAllowed;
    }
}

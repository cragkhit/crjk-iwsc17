package org.apache.coyote.http2;
class ConnectionException extends Http2Exception {
    private static final long serialVersionUID = 1L;
    ConnectionException ( final String msg, final Http2Error error ) {
        super ( msg, error );
    }
}

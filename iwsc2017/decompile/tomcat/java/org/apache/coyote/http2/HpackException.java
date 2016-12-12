package org.apache.coyote.http2;
class HpackException extends Exception {
    private static final long serialVersionUID = 1L;
    HpackException ( final String message ) {
        super ( message );
    }
    HpackException() {
    }
}

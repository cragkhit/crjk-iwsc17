package org.apache.coyote.http2;
class HpackException extends Exception {
    private static final long serialVersionUID = 1L;
    HpackException ( String message ) {
        super ( message );
    }
    HpackException() {
        super();
    }
}

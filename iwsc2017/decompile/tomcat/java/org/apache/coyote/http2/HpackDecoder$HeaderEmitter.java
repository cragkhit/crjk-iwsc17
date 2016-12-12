package org.apache.coyote.http2;
interface HeaderEmitter {
    void emitHeader ( String p0, String p1 );
    void validateHeaders() throws StreamException;
}

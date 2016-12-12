package org.apache.coyote.http2;
import org.apache.coyote.http2.HpackDecoder.HeaderEmitter;
class HeaderSink implements HeaderEmitter {
    @Override
    public void emitHeader ( String name, String value ) {
    }
    @Override
    public void validateHeaders() throws StreamException {
    }
}

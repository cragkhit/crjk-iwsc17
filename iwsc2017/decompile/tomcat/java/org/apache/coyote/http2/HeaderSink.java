package org.apache.coyote.http2;
class HeaderSink implements HpackDecoder.HeaderEmitter {
    @Override
    public void emitHeader ( final String name, final String value ) {
    }
    @Override
    public void validateHeaders() throws StreamException {
    }
}

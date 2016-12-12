package org.apache.coyote.http2;
class StreamException extends Http2Exception {
    private static final long serialVersionUID = 1L;
    private final int streamId;
    StreamException ( String msg, Http2Error error, int streamId ) {
        super ( msg, error );
        this.streamId = streamId;
    }
    int getStreamId() {
        return streamId;
    }
}

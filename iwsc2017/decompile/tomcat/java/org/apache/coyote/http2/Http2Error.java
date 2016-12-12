package org.apache.coyote.http2;
enum Http2Error {
    NO_ERROR ( 0L ),
    PROTOCOL_ERROR ( 1L ),
    INTERNAL_ERROR ( 2L ),
    FLOW_CONTROL_ERROR ( 3L ),
    SETTINGS_TIMEOUT ( 4L ),
    STREAM_CLOSED ( 5L ),
    FRAME_SIZE_ERROR ( 6L ),
    REFUSED_STREAM ( 7L ),
    CANCEL ( 8L ),
    COMPRESSION_ERROR ( 9L ),
    CONNECT_ERROR ( 10L ),
    ENHANCE_YOUR_CALM ( 11L ),
    INADEQUATE_SECURITY ( 12L ),
    HTTP_1_1_REQUIRED ( 13L );
    private final long code;
    private Http2Error ( final long code ) {
        this.code = code;
    }
    long getCode() {
        return this.code;
    }
    byte[] getCodeBytes() {
        final byte[] codeByte = new byte[4];
        ByteUtil.setFourBytes ( codeByte, 0, this.code );
        return codeByte;
    }
}

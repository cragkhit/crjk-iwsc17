package org.apache.coyote.http2;
class Flags {
    private Flags() {
    }
    static boolean isEndOfStream ( int flags ) {
        return ( flags & 0x01 ) > 0;
    }
    static boolean isAck ( int flags ) {
        return ( flags & 0x01 ) > 0;
    }
    static boolean isEndOfHeaders ( int flags ) {
        return ( flags & 0x04 ) > 0;
    }
    static boolean hasPadding ( int flags ) {
        return ( flags & 0x08 ) > 0;
    }
    static boolean hasPriority ( int flags ) {
        return ( flags & 0x20 ) > 0;
    }
}

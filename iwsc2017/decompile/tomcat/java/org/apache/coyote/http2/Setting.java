package org.apache.coyote.http2;
enum Setting {
    HEADER_TABLE_SIZE ( 1 ),
    ENABLE_PUSH ( 2 ),
    MAX_CONCURRENT_STREAMS ( 3 ),
    INITIAL_WINDOW_SIZE ( 4 ),
    MAX_FRAME_SIZE ( 5 ),
    MAX_HEADER_LIST_SIZE ( 6 ),
    UNKNOWN ( Integer.MAX_VALUE );
    private final int id;
    private Setting ( final int id ) {
        this.id = id;
    }
    final int getId() {
        return this.id;
    }
    @Override
    public final String toString() {
        return Integer.toString ( this.id );
    }
    static final Setting valueOf ( final int i ) {
        switch ( i ) {
        case 1: {
            return Setting.HEADER_TABLE_SIZE;
        }
        case 2: {
            return Setting.ENABLE_PUSH;
        }
        case 3: {
            return Setting.MAX_CONCURRENT_STREAMS;
        }
        case 4: {
            return Setting.INITIAL_WINDOW_SIZE;
        }
        case 5: {
            return Setting.MAX_FRAME_SIZE;
        }
        case 6: {
            return Setting.MAX_HEADER_LIST_SIZE;
        }
        default: {
            return Setting.UNKNOWN;
        }
        }
    }
}

package org.apache.coyote.http2;
import java.util.HashMap;
import org.apache.juli.logging.LogFactory;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
abstract class ConnectionSettingsBase<T extends Throwable> {
    private final Log log;
    private final StringManager sm;
    private final String connectionId;
    static final int MAX_WINDOW_SIZE = Integer.MAX_VALUE;
    static final int MIN_MAX_FRAME_SIZE = 16384;
    static final int MAX_MAX_FRAME_SIZE = 16777215;
    static final long UNLIMITED = 4294967296L;
    static final int MAX_HEADER_TABLE_SIZE = 65536;
    static final int DEFAULT_HEADER_TABLE_SIZE = 4096;
    static final boolean DEFAULT_ENABLE_PUSH = true;
    static final long DEFAULT_MAX_CONCURRENT_STREAMS = 4294967296L;
    static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;
    static final int DEFAULT_MAX_FRAME_SIZE = 16384;
    static final long DEFAULT_MAX_HEADER_LIST_SIZE = 4294967296L;
    Map<Setting, Long> current;
    Map<Setting, Long> pending;
    ConnectionSettingsBase ( final String connectionId ) {
        this.log = LogFactory.getLog ( ConnectionSettingsBase.class );
        this.sm = StringManager.getManager ( ConnectionSettingsBase.class );
        this.current = new HashMap<Setting, Long>();
        this.pending = new HashMap<Setting, Long>();
        this.connectionId = connectionId;
        this.current.put ( Setting.HEADER_TABLE_SIZE, 4096L );
        this.current.put ( Setting.ENABLE_PUSH, 1L );
        this.current.put ( Setting.MAX_CONCURRENT_STREAMS, 4294967296L );
        this.current.put ( Setting.INITIAL_WINDOW_SIZE, 65535L );
        this.current.put ( Setting.MAX_FRAME_SIZE, 16384L );
        this.current.put ( Setting.MAX_HEADER_LIST_SIZE, 4294967296L );
    }
    final void set ( final Setting setting, final long value ) throws T, Throwable {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( this.sm.getString ( "connectionSettings.debug", this.connectionId, setting, Long.toString ( value ) ) );
        }
        switch ( setting ) {
        case HEADER_TABLE_SIZE: {
            this.validateHeaderTableSize ( value );
            break;
        }
        case ENABLE_PUSH: {
            this.validateEnablePush ( value );
        }
        case INITIAL_WINDOW_SIZE: {
            this.validateInitialWindowSize ( value );
            break;
        }
        case MAX_FRAME_SIZE: {
            this.validateMaxFrameSize ( value );
        }
        case UNKNOWN: {
            this.log.warn ( this.sm.getString ( "connectionSettings.unknown", this.connectionId, setting, Long.toString ( value ) ) );
            return;
        }
        }
        this.set ( setting, Long.valueOf ( value ) );
    }
    synchronized void set ( final Setting setting, final Long value ) {
        this.current.put ( setting, value );
    }
    final int getHeaderTableSize() {
        return this.getMinInt ( Setting.HEADER_TABLE_SIZE );
    }
    final boolean getEnablePush() {
        final long result = this.getMin ( Setting.ENABLE_PUSH );
        return result != 0L;
    }
    final long getMaxConcurrentStreams() {
        return this.getMax ( Setting.MAX_CONCURRENT_STREAMS );
    }
    final int getInitialWindowSize() {
        return this.getMaxInt ( Setting.INITIAL_WINDOW_SIZE );
    }
    final int getMaxFrameSize() {
        return this.getMaxInt ( Setting.MAX_FRAME_SIZE );
    }
    final long getMaxHeaderListSize() {
        return this.getMax ( Setting.MAX_HEADER_LIST_SIZE );
    }
    private synchronized long getMin ( final Setting setting ) {
        final Long pendingValue = this.pending.get ( setting );
        final long currentValue = this.current.get ( setting );
        if ( pendingValue == null ) {
            return currentValue;
        }
        return Long.min ( pendingValue, currentValue );
    }
    private synchronized int getMinInt ( final Setting setting ) {
        final long result = this.getMin ( setting );
        if ( result > 2147483647L ) {
            return Integer.MAX_VALUE;
        }
        return ( int ) result;
    }
    private synchronized long getMax ( final Setting setting ) {
        final Long pendingValue = this.pending.get ( setting );
        final long currentValue = this.current.get ( setting );
        if ( pendingValue == null ) {
            return currentValue;
        }
        return Long.max ( pendingValue, currentValue );
    }
    private synchronized int getMaxInt ( final Setting setting ) {
        final long result = this.getMax ( setting );
        if ( result > 2147483647L ) {
            return Integer.MAX_VALUE;
        }
        return ( int ) result;
    }
    private void validateHeaderTableSize ( final long headerTableSize ) throws T, Throwable {
        if ( headerTableSize > 65536L ) {
            final String msg = this.sm.getString ( "connectionSettings.headerTableSizeLimit", this.connectionId, Long.toString ( headerTableSize ) );
            this.throwException ( msg, Http2Error.PROTOCOL_ERROR );
        }
    }
    private void validateEnablePush ( final long enablePush ) throws T, Throwable {
        if ( enablePush > 1L ) {
            final String msg = this.sm.getString ( "connectionSettings.enablePushInvalid", this.connectionId, Long.toString ( enablePush ) );
            this.throwException ( msg, Http2Error.PROTOCOL_ERROR );
        }
    }
    private void validateInitialWindowSize ( final long initialWindowSize ) throws T, Throwable {
        if ( initialWindowSize > 2147483647L ) {
            final String msg = this.sm.getString ( "connectionSettings.windowSizeTooBig", this.connectionId, Long.toString ( initialWindowSize ), Long.toString ( 2147483647L ) );
            this.throwException ( msg, Http2Error.FLOW_CONTROL_ERROR );
        }
    }
    private void validateMaxFrameSize ( final long maxFrameSize ) throws T, Throwable {
        if ( maxFrameSize < 16384L || maxFrameSize > 16777215L ) {
            final String msg = this.sm.getString ( "connectionSettings.maxFrameSizeInvalid", this.connectionId, Long.toString ( maxFrameSize ), Integer.toString ( 16384 ), Integer.toString ( 16777215 ) );
            this.throwException ( msg, Http2Error.PROTOCOL_ERROR );
        }
    }
    abstract void throwException ( final String p0, final Http2Error p1 ) throws T, Throwable;
}

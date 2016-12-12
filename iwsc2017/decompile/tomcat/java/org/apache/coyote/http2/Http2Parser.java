package org.apache.coyote.http2;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.ProtocolException;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
class Http2Parser {
    private static final Log log;
    private static final StringManager sm;
    static final byte[] CLIENT_PREFACE_START;
    private final String connectionId;
    private final Input input;
    private final Output output;
    private final byte[] frameHeaderBuffer;
    private volatile HpackDecoder hpackDecoder;
    private volatile ByteBuffer headerReadBuffer;
    private volatile int headersCurrentStream;
    private volatile boolean headersEndStream;
    private volatile boolean streamReset;
    Http2Parser ( final String connectionId, final Input input, final Output output ) {
        this.frameHeaderBuffer = new byte[9];
        this.headerReadBuffer = ByteBuffer.allocate ( 1024 );
        this.headersCurrentStream = -1;
        this.headersEndStream = false;
        this.streamReset = false;
        this.connectionId = connectionId;
        this.input = input;
        this.output = output;
    }
    boolean readFrame ( final boolean block ) throws Http2Exception, IOException {
        return this.readFrame ( block, null );
    }
    private boolean readFrame ( final boolean block, final FrameType expected ) throws IOException, Http2Exception {
        if ( !this.input.fill ( block, this.frameHeaderBuffer ) ) {
            return false;
        }
        final int payloadSize = ByteUtil.getThreeBytes ( this.frameHeaderBuffer, 0 );
        final FrameType frameType = FrameType.valueOf ( ByteUtil.getOneByte ( this.frameHeaderBuffer, 3 ) );
        final int flags = ByteUtil.getOneByte ( this.frameHeaderBuffer, 4 );
        final int streamId = ByteUtil.get31Bits ( this.frameHeaderBuffer, 5 );
        try {
            this.validateFrame ( expected, frameType, streamId, flags, payloadSize );
        } catch ( StreamException se ) {
            this.swallow ( streamId, payloadSize, false );
            throw se;
        }
        switch ( frameType ) {
        case DATA: {
            this.readDataFrame ( streamId, flags, payloadSize );
            break;
        }
        case HEADERS: {
            this.readHeadersFrame ( streamId, flags, payloadSize );
            break;
        }
        case PRIORITY: {
            this.readPriorityFrame ( streamId );
            break;
        }
        case RST: {
            this.readRstFrame ( streamId );
            break;
        }
        case SETTINGS: {
            this.readSettingsFrame ( flags, payloadSize );
            break;
        }
        case PUSH_PROMISE: {
            this.readPushPromiseFrame ( streamId );
            break;
        }
        case PING: {
            this.readPingFrame ( flags );
            break;
        }
        case GOAWAY: {
            this.readGoawayFrame ( payloadSize );
            break;
        }
        case WINDOW_UPDATE: {
            this.readWindowUpdateFrame ( streamId );
            break;
        }
        case CONTINUATION: {
            this.readContinuationFrame ( streamId, flags, payloadSize );
            break;
        }
        case UNKNOWN: {
            this.readUnknownFrame ( streamId, frameType, flags, payloadSize );
            break;
        }
        }
        return true;
    }
    private void readDataFrame ( final int streamId, final int flags, final int payloadSize ) throws Http2Exception, IOException {
        int padLength = 0;
        final boolean endOfStream = Flags.isEndOfStream ( flags );
        int dataLength;
        if ( Flags.hasPadding ( flags ) ) {
            final byte[] b = { 0 };
            this.input.fill ( true, b );
            padLength = ( b[0] & 0xFF );
            if ( padLength >= payloadSize ) {
                throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrame.tooMuchPadding", this.connectionId, Integer.toString ( streamId ), Integer.toString ( padLength ), Integer.toString ( payloadSize ) ), Http2Error.PROTOCOL_ERROR );
            }
            dataLength = payloadSize - ( padLength + 1 );
        } else {
            dataLength = payloadSize;
        }
        if ( Http2Parser.log.isDebugEnabled() ) {
            String padding;
            if ( Flags.hasPadding ( flags ) ) {
                padding = Integer.toString ( padLength );
            } else {
                padding = "none";
            }
            Http2Parser.log.debug ( Http2Parser.sm.getString ( "http2Parser.processFrameData.lengths", this.connectionId, Integer.toString ( streamId ), Integer.toString ( dataLength ), padding ) );
        }
        final ByteBuffer dest = this.output.startRequestBodyFrame ( streamId, payloadSize );
        if ( dest == null ) {
            this.swallow ( streamId, dataLength, false );
            if ( padLength > 0 ) {
                this.swallow ( streamId, padLength, true );
            }
            if ( endOfStream ) {
                this.output.receivedEndOfStream ( streamId );
            }
        } else {
            synchronized ( dest ) {
                this.input.fill ( true, dest, dataLength );
                if ( padLength > 0 ) {
                    this.swallow ( streamId, padLength, true );
                }
                if ( endOfStream ) {
                    this.output.receivedEndOfStream ( streamId );
                }
                this.output.endRequestBodyFrame ( streamId );
            }
        }
        if ( padLength > 0 ) {
            this.output.swallowedPadding ( streamId, padLength );
        }
    }
    private void readHeadersFrame ( final int streamId, final int flags, int payloadSize ) throws Http2Exception, IOException {
        this.headersEndStream = Flags.isEndOfStream ( flags );
        if ( this.hpackDecoder == null ) {
            this.hpackDecoder = this.output.getHpackDecoder();
        }
        try {
            this.hpackDecoder.setHeaderEmitter ( this.output.headersStart ( streamId, this.headersEndStream ) );
        } catch ( StreamException se ) {
            this.swallow ( streamId, payloadSize, false );
            throw se;
        }
        int padLength = 0;
        final boolean padding = Flags.hasPadding ( flags );
        final boolean priority = Flags.hasPriority ( flags );
        int optionalLen = 0;
        if ( padding ) {
            optionalLen = 1;
        }
        if ( priority ) {
            optionalLen += 5;
        }
        if ( optionalLen > 0 ) {
            final byte[] optional = new byte[optionalLen];
            this.input.fill ( true, optional );
            int optionalPos = 0;
            if ( padding ) {
                padLength = ByteUtil.getOneByte ( optional, optionalPos++ );
                if ( padLength >= payloadSize ) {
                    throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrame.tooMuchPadding", this.connectionId, Integer.toString ( streamId ), Integer.toString ( padLength ), Integer.toString ( payloadSize ) ), Http2Error.PROTOCOL_ERROR );
                }
            }
            if ( priority ) {
                final boolean exclusive = ByteUtil.isBit7Set ( optional[optionalPos] );
                final int parentStreamId = ByteUtil.get31Bits ( optional, optionalPos );
                final int weight = ByteUtil.getOneByte ( optional, optionalPos + 4 ) + 1;
                this.output.reprioritise ( streamId, parentStreamId, exclusive, weight );
            }
            payloadSize -= optionalLen;
            payloadSize -= padLength;
        }
        this.readHeaderPayload ( streamId, payloadSize );
        this.swallow ( streamId, padLength, true );
        if ( Flags.isEndOfHeaders ( flags ) ) {
            this.onHeadersComplete ( streamId );
        } else {
            this.headersCurrentStream = streamId;
        }
    }
    private void readPriorityFrame ( final int streamId ) throws Http2Exception, IOException {
        final byte[] payload = new byte[5];
        this.input.fill ( true, payload );
        final boolean exclusive = ByteUtil.isBit7Set ( payload[0] );
        final int parentStreamId = ByteUtil.get31Bits ( payload, 0 );
        final int weight = ByteUtil.getOneByte ( payload, 4 ) + 1;
        if ( streamId == parentStreamId ) {
            throw new StreamException ( Http2Parser.sm.getString ( "http2Parser.processFramePriority.invalidParent", this.connectionId, streamId ), Http2Error.PROTOCOL_ERROR, streamId );
        }
        this.output.reprioritise ( streamId, parentStreamId, exclusive, weight );
    }
    private void readRstFrame ( final int streamId ) throws Http2Exception, IOException {
        final byte[] payload = new byte[4];
        this.input.fill ( true, payload );
        final long errorCode = ByteUtil.getFourBytes ( payload, 0 );
        this.output.reset ( streamId, errorCode );
        this.headersCurrentStream = -1;
        this.headersEndStream = false;
    }
    private void readSettingsFrame ( final int flags, final int payloadSize ) throws Http2Exception, IOException {
        final boolean ack = Flags.isAck ( flags );
        if ( payloadSize > 0 && ack ) {
            throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrameSettings.ackWithNonZeroPayload" ), Http2Error.FRAME_SIZE_ERROR );
        }
        if ( payloadSize != 0 ) {
            final byte[] setting = new byte[6];
            for ( int i = 0; i < payloadSize / 6; ++i ) {
                this.input.fill ( true, setting );
                final int id = ByteUtil.getTwoBytes ( setting, 0 );
                final long value = ByteUtil.getFourBytes ( setting, 2 );
                this.output.setting ( Setting.valueOf ( id ), value );
            }
        }
        this.output.settingsEnd ( ack );
    }
    private void readPushPromiseFrame ( final int streamId ) throws Http2Exception {
        throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFramePushPromise", this.connectionId, streamId ), Http2Error.PROTOCOL_ERROR );
    }
    private void readPingFrame ( final int flags ) throws IOException {
        final byte[] payload = new byte[8];
        this.input.fill ( true, payload );
        this.output.pingReceive ( payload, Flags.isAck ( flags ) );
    }
    private void readGoawayFrame ( final int payloadSize ) throws IOException {
        final byte[] payload = new byte[payloadSize];
        this.input.fill ( true, payload );
        final int lastStreamId = ByteUtil.get31Bits ( payload, 0 );
        final long errorCode = ByteUtil.getFourBytes ( payload, 4 );
        String debugData = null;
        if ( payloadSize > 8 ) {
            debugData = new String ( payload, 8, payloadSize - 8, StandardCharsets.UTF_8 );
        }
        this.output.goaway ( lastStreamId, errorCode, debugData );
    }
    private void readWindowUpdateFrame ( final int streamId ) throws Http2Exception, IOException {
        final byte[] payload = new byte[4];
        this.input.fill ( true, payload );
        final int windowSizeIncrement = ByteUtil.get31Bits ( payload, 0 );
        if ( Http2Parser.log.isDebugEnabled() ) {
            Http2Parser.log.debug ( Http2Parser.sm.getString ( "http2Parser.processFrameWindowUpdate.debug", this.connectionId, Integer.toString ( streamId ), Integer.toString ( windowSizeIncrement ) ) );
        }
        if ( windowSizeIncrement != 0 ) {
            this.output.incrementWindowSize ( streamId, windowSizeIncrement );
            return;
        }
        if ( streamId == 0 ) {
            throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrameWindowUpdate.invalidIncrement" ), Http2Error.PROTOCOL_ERROR );
        }
        throw new StreamException ( Http2Parser.sm.getString ( "http2Parser.processFrameWindowUpdate.invalidIncrement" ), Http2Error.PROTOCOL_ERROR, streamId );
    }
    private void readContinuationFrame ( final int streamId, final int flags, final int payloadSize ) throws Http2Exception, IOException {
        if ( this.headersCurrentStream == -1 ) {
            throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrameContinuation.notExpected", this.connectionId, Integer.toString ( streamId ) ), Http2Error.PROTOCOL_ERROR );
        }
        this.readHeaderPayload ( streamId, payloadSize );
        if ( Flags.isEndOfHeaders ( flags ) ) {
            this.onHeadersComplete ( streamId );
            this.headersCurrentStream = -1;
        }
    }
    private void readHeaderPayload ( final int streamId, final int payloadSize ) throws Http2Exception, IOException {
        if ( Http2Parser.log.isDebugEnabled() ) {
            Http2Parser.log.debug ( Http2Parser.sm.getString ( "http2Parser.processFrameHeaders.payload", this.connectionId, streamId, payloadSize ) );
        }
        int remaining = payloadSize;
        while ( remaining > 0 ) {
            if ( this.headerReadBuffer.remaining() == 0 ) {
                int newSize;
                if ( this.headerReadBuffer.capacity() < payloadSize ) {
                    newSize = payloadSize;
                } else {
                    newSize = this.headerReadBuffer.capacity() * 2;
                }
                this.headerReadBuffer = ByteBufferUtils.expand ( this.headerReadBuffer, newSize );
            }
            final int toRead = Math.min ( this.headerReadBuffer.remaining(), remaining );
            this.input.fill ( true, this.headerReadBuffer, toRead );
            this.headerReadBuffer.flip();
            try {
                this.hpackDecoder.decode ( this.headerReadBuffer );
            } catch ( HpackException hpe ) {
                throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrameHeaders.decodingFailed" ), Http2Error.COMPRESSION_ERROR );
            }
            this.headerReadBuffer.compact();
            remaining -= toRead;
            if ( this.hpackDecoder.isHeaderCountExceeded() && !this.streamReset ) {
                this.streamReset = true;
                throw new StreamException ( Http2Parser.sm.getString ( "http2Parser.headerLimitCount", this.connectionId, streamId ), Http2Error.ENHANCE_YOUR_CALM, streamId );
            }
            if ( this.hpackDecoder.isHeaderSizeExceeded ( this.headerReadBuffer.position() ) && !this.streamReset ) {
                this.streamReset = true;
                throw new StreamException ( Http2Parser.sm.getString ( "http2Parser.headerLimitSize", this.connectionId, streamId ), Http2Error.ENHANCE_YOUR_CALM, streamId );
            }
            if ( this.hpackDecoder.isHeaderSwallowSizeExceeded ( this.headerReadBuffer.position() ) ) {
                throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.headerLimitSize", this.connectionId, streamId ), Http2Error.ENHANCE_YOUR_CALM );
            }
        }
        this.hpackDecoder.getHeaderEmitter().validateHeaders();
    }
    private void onHeadersComplete ( final int streamId ) throws Http2Exception {
        if ( this.headerReadBuffer.position() > 0 ) {
            throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.processFrameHeaders.decodingDataLeft" ), Http2Error.COMPRESSION_ERROR );
        }
        this.output.headersEnd ( streamId );
        if ( this.headersEndStream ) {
            this.output.receivedEndOfStream ( streamId );
            this.headersEndStream = false;
        }
        if ( this.headerReadBuffer.capacity() > 1024 ) {
            this.headerReadBuffer = ByteBuffer.allocate ( 1024 );
        }
        if ( this.streamReset ) {
            this.streamReset = false;
        }
    }
    private void readUnknownFrame ( final int streamId, final FrameType frameType, final int flags, final int payloadSize ) throws IOException {
        try {
            this.swallow ( streamId, payloadSize, false );
        } catch ( ConnectionException ex ) {}
        this.output.swallowed ( streamId, frameType, flags, payloadSize );
    }
    private void swallow ( final int streamId, final int len, final boolean mustBeZero ) throws IOException, ConnectionException {
        if ( Http2Parser.log.isDebugEnabled() ) {
            Http2Parser.log.debug ( Http2Parser.sm.getString ( "http2Parser.swallow.debug", this.connectionId, Integer.toString ( streamId ), Integer.toString ( len ) ) );
        }
        if ( len == 0 ) {
            return;
        }
        int read = 0;
        final byte[] buffer = new byte[1024];
        while ( read < len ) {
            final int thisTime = Math.min ( buffer.length, len - read );
            this.input.fill ( true, buffer, 0, thisTime );
            if ( mustBeZero ) {
                for ( int i = 0; i < thisTime; ++i ) {
                    if ( buffer[i] != 0 ) {
                        throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.nonZeroPadding", this.connectionId, Integer.toString ( streamId ) ), Http2Error.PROTOCOL_ERROR );
                    }
                }
            }
            read += thisTime;
        }
    }
    private void validateFrame ( final FrameType expected, final FrameType frameType, final int streamId, final int flags, final int payloadSize ) throws Http2Exception {
        if ( Http2Parser.log.isDebugEnabled() ) {
            Http2Parser.log.debug ( Http2Parser.sm.getString ( "http2Parser.processFrame", this.connectionId, Integer.toString ( streamId ), frameType, Integer.toString ( flags ), Integer.toString ( payloadSize ) ) );
        }
        if ( expected != null && frameType != expected ) {
            throw new StreamException ( Http2Parser.sm.getString ( "http2Parser.processFrame.unexpectedType", expected, frameType ), Http2Error.PROTOCOL_ERROR, streamId );
        }
        final int maxFrameSize = this.input.getMaxFrameSize();
        if ( payloadSize > maxFrameSize ) {
            throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.payloadTooBig", Integer.toString ( payloadSize ), Integer.toString ( maxFrameSize ) ), Http2Error.FRAME_SIZE_ERROR );
        }
        if ( this.headersCurrentStream != -1 ) {
            if ( this.headersCurrentStream != streamId ) {
                throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.headers.wrongStream", this.connectionId, Integer.toString ( this.headersCurrentStream ), Integer.toString ( streamId ) ), Http2Error.COMPRESSION_ERROR );
            }
            if ( frameType != FrameType.RST ) {
                if ( frameType != FrameType.CONTINUATION ) {
                    throw new ConnectionException ( Http2Parser.sm.getString ( "http2Parser.headers.wrongFrameType", this.connectionId, Integer.toString ( this.headersCurrentStream ), frameType ), Http2Error.COMPRESSION_ERROR );
                }
            }
        }
        frameType.check ( streamId, payloadSize );
    }
    void readConnectionPreface() throws Http2Exception {
        final byte[] data = new byte[Http2Parser.CLIENT_PREFACE_START.length];
        try {
            this.input.fill ( true, data );
            for ( int i = 0; i < Http2Parser.CLIENT_PREFACE_START.length; ++i ) {
                if ( Http2Parser.CLIENT_PREFACE_START[i] != data[i] ) {
                    throw new ProtocolException ( Http2Parser.sm.getString ( "http2Parser.preface.invalid" ) );
                }
            }
            this.readFrame ( true, FrameType.SETTINGS );
        } catch ( IOException ioe ) {
            throw new ProtocolException ( Http2Parser.sm.getString ( "http2Parser.preface.io" ), ioe );
        }
    }
    static {
        log = LogFactory.getLog ( Http2Parser.class );
        sm = StringManager.getManager ( Http2Parser.class );
        CLIENT_PREFACE_START = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes ( StandardCharsets.ISO_8859_1 );
    }
    interface Input {
        boolean fill ( boolean p0, byte[] p1, int p2, int p3 ) throws IOException;
    default boolean fill ( boolean block, byte[] data ) throws IOException {
                return this.fill ( block, data, 0, data.length );
            }
    default boolean fill ( boolean block, ByteBuffer data, int len ) throws IOException {
                boolean result;
                result = this.fill ( block, data.array(), data.arrayOffset() + data.position(), len );
                if ( result ) {
                    data.position ( data.position() + len );
                }
                return result;
            }
        int getMaxFrameSize();
    }
    interface Output {
        HpackDecoder getHpackDecoder();
        ByteBuffer startRequestBodyFrame ( int p0, int p1 ) throws Http2Exception;
        void endRequestBodyFrame ( int p0 ) throws Http2Exception;
        void receivedEndOfStream ( int p0 ) throws ConnectionException;
        void swallowedPadding ( int p0, int p1 ) throws ConnectionException, IOException;
        HpackDecoder.HeaderEmitter headersStart ( int p0, boolean p1 ) throws Http2Exception;
        void headersEnd ( int p0 ) throws ConnectionException;
        void reprioritise ( int p0, int p1, boolean p2, int p3 ) throws Http2Exception;
        void reset ( int p0, long p1 ) throws Http2Exception;
        void setting ( Setting p0, long p1 ) throws ConnectionException;
        void settingsEnd ( boolean p0 ) throws IOException;
        void pingReceive ( byte[] p0, boolean p1 ) throws IOException;
        void goaway ( int p0, long p1, String p2 );
        void incrementWindowSize ( int p0, int p1 ) throws Http2Exception;
        void swallowed ( int p0, FrameType p1, int p2, int p3 ) throws IOException;
    }
}

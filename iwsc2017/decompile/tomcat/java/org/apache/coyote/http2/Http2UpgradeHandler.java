package org.apache.coyote.http2;
import org.apache.juli.logging.LogFactory;
import java.io.EOFException;
import java.util.TreeSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.coyote.CloseNowException;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import java.nio.ByteBuffer;
import org.apache.coyote.Response;
import java.nio.charset.StandardCharsets;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketEvent;
import java.io.IOException;
import org.apache.coyote.ProtocolException;
import org.apache.tomcat.util.codec.binary.Base64;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.servlet.http.WebConnection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import org.apache.coyote.Request;
import java.util.Set;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.coyote.Adapter;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
class Http2UpgradeHandler extends AbstractStream implements InternalHttpUpgradeHandler, Http2Parser.Input, Http2Parser.Output {
    private static final Log log;
    private static final StringManager sm;
    private static final AtomicInteger connectionIdGenerator;
    private static final Integer STREAM_ID_ZERO;
    private static final int FLAG_END_OF_STREAM = 1;
    private static final int FLAG_END_OF_HEADERS = 4;
    private static final byte[] PING;
    private static final byte[] PING_ACK;
    private static final byte[] SETTINGS_ACK;
    private static final byte[] GOAWAY;
    private static final String HTTP2_SETTINGS_HEADER = "HTTP2-Settings";
    private static final HeaderSink HEADER_SINK;
    private final String connectionId;
    private final Adapter adapter;
    private volatile SocketWrapperBase<?> socketWrapper;
    private volatile SSLSupport sslSupport;
    private volatile Http2Parser parser;
    private AtomicReference<ConnectionState> connectionState;
    private volatile long pausedNanoTime;
    private final ConnectionSettingsRemote remoteSettings;
    private final ConnectionSettingsLocal localSettings;
    private HpackDecoder hpackDecoder;
    private HpackEncoder hpackEncoder;
    private long readTimeout;
    private long keepAliveTimeout;
    private long writeTimeout;
    private final Map<Integer, Stream> streams;
    private final AtomicInteger activeRemoteStreamCount;
    private volatile int maxRemoteStreamId;
    private volatile int maxActiveRemoteStreamId;
    private volatile int maxProcessedStreamId;
    private final AtomicInteger nextLocalStreamId;
    private final PingManager pingManager;
    private volatile int newStreamsSinceLastPrune;
    private final Map<AbstractStream, int[]> backLogStreams;
    private long backLogSize;
    private int maxConcurrentStreamExecution;
    private AtomicInteger streamConcurrency;
    private Queue<StreamProcessor> queuedProcessors;
    private Set<String> allowedTrailerHeaders;
    private int maxHeaderCount;
    private int maxHeaderSize;
    private int maxTrailerCount;
    private int maxTrailerSize;
    Http2UpgradeHandler ( final Adapter adapter, final Request coyoteRequest ) {
        super ( Http2UpgradeHandler.STREAM_ID_ZERO );
        this.connectionState = new AtomicReference<ConnectionState> ( ConnectionState.NEW );
        this.pausedNanoTime = Long.MAX_VALUE;
        this.readTimeout = 10000L;
        this.keepAliveTimeout = -1L;
        this.writeTimeout = 10000L;
        this.streams = new HashMap<Integer, Stream>();
        this.activeRemoteStreamCount = new AtomicInteger ( 0 );
        this.maxRemoteStreamId = 0;
        this.maxActiveRemoteStreamId = -1;
        this.nextLocalStreamId = new AtomicInteger ( 2 );
        this.pingManager = new PingManager();
        this.newStreamsSinceLastPrune = 0;
        this.backLogStreams = new ConcurrentHashMap<AbstractStream, int[]>();
        this.backLogSize = 0L;
        this.maxConcurrentStreamExecution = 200;
        this.streamConcurrency = null;
        this.queuedProcessors = null;
        this.allowedTrailerHeaders = Collections.emptySet();
        this.maxHeaderCount = 100;
        this.maxHeaderSize = 8192;
        this.maxTrailerCount = 100;
        this.maxTrailerSize = 8192;
        this.adapter = adapter;
        this.connectionId = Integer.toString ( Http2UpgradeHandler.connectionIdGenerator.getAndIncrement() );
        this.remoteSettings = new ConnectionSettingsRemote ( this.connectionId );
        this.localSettings = new ConnectionSettingsLocal ( this.connectionId );
        if ( coyoteRequest != null ) {
            if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.upgrade", this.connectionId ) );
            }
            final Integer key = 1;
            final Stream stream = new Stream ( key, this, coyoteRequest );
            this.streams.put ( key, stream );
            this.maxRemoteStreamId = 1;
            this.maxActiveRemoteStreamId = 1;
            this.activeRemoteStreamCount.set ( 1 );
            this.maxProcessedStreamId = 1;
        }
    }
    public void init ( final WebConnection webConnection ) {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.init", this.connectionId, this.connectionState.get() ) );
        }
        if ( !this.connectionState.compareAndSet ( ConnectionState.NEW, ConnectionState.CONNECTED ) ) {
            return;
        }
        if ( this.maxConcurrentStreamExecution < this.localSettings.getMaxConcurrentStreams() ) {
            this.streamConcurrency = new AtomicInteger ( 0 );
            this.queuedProcessors = new ConcurrentLinkedQueue<StreamProcessor>();
        }
        this.parser = new Http2Parser ( this.connectionId, this, this );
        Stream stream = null;
        this.socketWrapper.setReadTimeout ( this.getReadTimeout() );
        this.socketWrapper.setWriteTimeout ( this.getWriteTimeout() );
        if ( webConnection != null ) {
            try {
                stream = this.getStream ( 1, true );
                final String base64Settings = stream.getCoyoteRequest().getHeader ( "HTTP2-Settings" );
                final byte[] settings = Base64.decodeBase64 ( base64Settings );
                FrameType.SETTINGS.check ( 0, settings.length );
                for ( int i = 0; i < settings.length % 6; ++i ) {
                    final int id = ByteUtil.getTwoBytes ( settings, i * 6 );
                    final long value = ByteUtil.getFourBytes ( settings, i * 6 + 2 );
                    this.remoteSettings.set ( Setting.valueOf ( id ), value );
                }
            } catch ( Http2Exception e ) {
                throw new ProtocolException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.upgrade.fail", this.connectionId ) );
            }
        }
        try {
            final byte[] settings2 = this.localSettings.getSettingsFrameForPending();
            this.socketWrapper.write ( true, settings2, 0, settings2.length );
            this.socketWrapper.flush ( true );
        } catch ( IOException ioe ) {
            final String msg = Http2UpgradeHandler.sm.getString ( "upgradeHandler.sendPrefaceFail", this.connectionId );
            if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                Http2UpgradeHandler.log.debug ( msg );
            }
            throw new ProtocolException ( msg, ioe );
        }
        try {
            this.parser.readConnectionPreface();
        } catch ( Http2Exception e ) {
            final String msg = Http2UpgradeHandler.sm.getString ( "upgradeHandler.invalidPreface", this.connectionId );
            if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                Http2UpgradeHandler.log.debug ( msg );
            }
            throw new ProtocolException ( msg );
        }
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.prefaceReceived", this.connectionId ) );
        }
        try {
            this.pingManager.sendPing ( true );
        } catch ( IOException ioe ) {
            throw new ProtocolException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.pingFailed" ), ioe );
        }
        if ( webConnection != null ) {
            this.processStreamOnContainerThread ( stream );
        }
    }
    private void processStreamOnContainerThread ( final Stream stream ) {
        final StreamProcessor streamProcessor = new StreamProcessor ( this, stream, this.adapter, this.socketWrapper );
        streamProcessor.setSslSupport ( this.sslSupport );
        if ( this.streamConcurrency == null ) {
            this.socketWrapper.getEndpoint().getExecutor().execute ( streamProcessor );
        } else if ( this.getStreamConcurrency() < this.maxConcurrentStreamExecution ) {
            this.increaseStreamConcurrency();
            this.socketWrapper.getEndpoint().getExecutor().execute ( streamProcessor );
        } else {
            this.queuedProcessors.offer ( streamProcessor );
        }
    }
    @Override
    public void setSocketWrapper ( final SocketWrapperBase<?> wrapper ) {
        this.socketWrapper = wrapper;
    }
    @Override
    public void setSslSupport ( final SSLSupport sslSupport ) {
        this.sslSupport = sslSupport;
    }
    @Override
    public AbstractEndpoint.Handler.SocketState upgradeDispatch ( final SocketEvent status ) {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.upgradeDispatch.entry", this.connectionId, status ) );
        }
        this.init ( null );
        AbstractEndpoint.Handler.SocketState result = AbstractEndpoint.Handler.SocketState.CLOSED;
        try {
            this.pingManager.sendPing ( false );
            this.checkPauseState();
            switch ( status ) {
            case OPEN_READ: {
                try {
                    this.socketWrapper.setReadTimeout ( this.getReadTimeout() );
                    while ( true ) {
                        try {
                            while ( this.parser.readFrame ( false ) ) {}
                            break;
                        } catch ( StreamException se ) {
                            final Stream stream = this.getStream ( se.getStreamId(), false );
                            if ( stream == null ) {
                                this.sendStreamReset ( se );
                            } else {
                                stream.close ( se );
                            }
                            continue;
                        }
                    }
                    this.socketWrapper.setReadTimeout ( this.getKeepAliveTimeout() );
                } catch ( Http2Exception ce ) {
                    if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                        Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.connectionError" ), ce );
                    }
                    this.closeConnection ( ce );
                    break;
                }
                result = AbstractEndpoint.Handler.SocketState.UPGRADED;
                break;
            }
            case OPEN_WRITE: {
                this.processWrites();
                result = AbstractEndpoint.Handler.SocketState.UPGRADED;
                break;
            }
            case DISCONNECT:
            case ERROR:
            case TIMEOUT:
            case STOP: {
                this.close();
                break;
            }
            }
        } catch ( IOException ioe ) {
            if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.ioerror", this.connectionId ), ioe );
            }
            this.close();
        }
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.upgradeDispatch.exit", this.connectionId, result ) );
        }
        return result;
    }
    ConnectionSettingsRemote getRemoteSettings() {
        return this.remoteSettings;
    }
    ConnectionSettingsLocal getLocalSettings() {
        return this.localSettings;
    }
    @Override
    public void pause() {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.pause.entry", this.connectionId ) );
        }
        if ( this.connectionState.compareAndSet ( ConnectionState.CONNECTED, ConnectionState.PAUSING ) ) {
            this.pausedNanoTime = System.nanoTime();
            try {
                this.writeGoAwayFrame ( Integer.MAX_VALUE, Http2Error.NO_ERROR.getCode(), null );
            } catch ( IOException ex ) {}
        }
    }
    public void destroy() {
    }
    private void checkPauseState() throws IOException {
        if ( this.connectionState.get() == ConnectionState.PAUSING && this.pausedNanoTime + this.pingManager.getRoundTripTimeNano() < System.nanoTime() ) {
            this.connectionState.compareAndSet ( ConnectionState.PAUSING, ConnectionState.PAUSED );
            this.writeGoAwayFrame ( this.maxProcessedStreamId, Http2Error.NO_ERROR.getCode(), null );
        }
    }
    private int increaseStreamConcurrency() {
        return this.streamConcurrency.incrementAndGet();
    }
    private int decreaseStreamConcurrency() {
        return this.streamConcurrency.decrementAndGet();
    }
    private int getStreamConcurrency() {
        return this.streamConcurrency.get();
    }
    void executeQueuedStream() {
        if ( this.streamConcurrency == null ) {
            return;
        }
        this.decreaseStreamConcurrency();
        if ( this.getStreamConcurrency() < this.maxConcurrentStreamExecution ) {
            final StreamProcessor streamProcessor = this.queuedProcessors.poll();
            if ( streamProcessor != null ) {
                this.increaseStreamConcurrency();
                this.socketWrapper.getEndpoint().getExecutor().execute ( streamProcessor );
            }
        }
    }
    void sendStreamReset ( final StreamException se ) throws IOException {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.rst.debug", this.connectionId, Integer.toString ( se.getStreamId() ), se.getError() ) );
        }
        final byte[] rstFrame = new byte[13];
        ByteUtil.setThreeBytes ( rstFrame, 0, 4 );
        rstFrame[3] = FrameType.RST.getIdByte();
        ByteUtil.set31Bits ( rstFrame, 5, se.getStreamId() );
        ByteUtil.setFourBytes ( rstFrame, 9, se.getError().getCode() );
        synchronized ( this.socketWrapper ) {
            this.socketWrapper.write ( true, rstFrame, 0, rstFrame.length );
            this.socketWrapper.flush ( true );
        }
    }
    void closeConnection ( final Http2Exception ce ) {
        try {
            this.writeGoAwayFrame ( this.maxProcessedStreamId, ce.getError().getCode(), ce.getMessage().getBytes ( StandardCharsets.UTF_8 ) );
        } catch ( IOException ex ) {}
        this.close();
    }
    private void writeGoAwayFrame ( final int maxStreamId, final long errorCode, final byte[] debugMsg ) throws IOException {
        final byte[] fixedPayload = new byte[8];
        ByteUtil.set31Bits ( fixedPayload, 0, maxStreamId );
        ByteUtil.setFourBytes ( fixedPayload, 4, errorCode );
        int len = 8;
        if ( debugMsg != null ) {
            len += debugMsg.length;
        }
        final byte[] payloadLength = new byte[3];
        ByteUtil.setThreeBytes ( payloadLength, 0, len );
        synchronized ( this.socketWrapper ) {
            this.socketWrapper.write ( true, payloadLength, 0, payloadLength.length );
            this.socketWrapper.write ( true, Http2UpgradeHandler.GOAWAY, 0, Http2UpgradeHandler.GOAWAY.length );
            this.socketWrapper.write ( true, fixedPayload, 0, 8 );
            if ( debugMsg != null ) {
                this.socketWrapper.write ( true, debugMsg, 0, debugMsg.length );
            }
            this.socketWrapper.flush ( true );
        }
    }
    void writeHeaders ( final Stream stream, final Response coyoteResponse, final int payloadSize ) throws IOException {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.writeHeaders", this.connectionId, stream.getIdentifier() ) );
        }
        if ( !stream.canWrite() ) {
            return;
        }
        this.prepareHeaders ( coyoteResponse );
        final byte[] header = new byte[9];
        final ByteBuffer target = ByteBuffer.allocate ( payloadSize );
        boolean first = true;
        HpackEncoder.State state = null;
        synchronized ( this.socketWrapper ) {
            while ( state != HpackEncoder.State.COMPLETE ) {
                state = this.getHpackEncoder().encode ( coyoteResponse.getMimeHeaders(), target );
                target.flip();
                ByteUtil.setThreeBytes ( header, 0, target.limit() );
                if ( first ) {
                    first = false;
                    header[3] = FrameType.HEADERS.getIdByte();
                    if ( stream.getOutputBuffer().hasNoBody() ) {
                        header[4] = 1;
                    }
                } else {
                    header[3] = FrameType.CONTINUATION.getIdByte();
                }
                if ( state == HpackEncoder.State.COMPLETE ) {
                    final byte[] array = header;
                    final int n = 4;
                    array[n] += 4;
                }
                if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                    Http2UpgradeHandler.log.debug ( target.limit() + " bytes" );
                }
                ByteUtil.set31Bits ( header, 5, stream.getIdentifier() );
                try {
                    this.socketWrapper.write ( true, header, 0, header.length );
                    this.socketWrapper.write ( true, target );
                    this.socketWrapper.flush ( true );
                } catch ( IOException ioe ) {
                    this.handleAppInitiatedIOException ( ioe );
                }
            }
        }
    }
    private void prepareHeaders ( final Response coyoteResponse ) {
        final MimeHeaders headers = coyoteResponse.getMimeHeaders();
        final int statusCode = coyoteResponse.getStatus();
        headers.addValue ( ":status" ).setString ( Integer.toString ( statusCode ) );
        if ( statusCode >= 200 && statusCode != 205 && statusCode != 304 ) {
            final String contentType = coyoteResponse.getContentType();
            if ( contentType != null ) {
                headers.setValue ( "content-type" ).setString ( contentType );
            }
            final String contentLanguage = coyoteResponse.getContentLanguage();
            if ( contentLanguage != null ) {
                headers.setValue ( "content-language" ).setString ( contentLanguage );
            }
        }
        if ( headers.getValue ( "date" ) == null ) {
            headers.addValue ( "date" ).setString ( FastHttpDateFormat.getCurrentDate() );
        }
    }
    private void writePushHeaders ( final Stream stream, final int pushedStreamId, final Request coyoteRequest, final int payloadSize ) throws IOException {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.writePushHeaders", this.connectionId, stream.getIdentifier(), Integer.toString ( pushedStreamId ) ) );
        }
        synchronized ( this.socketWrapper ) {
            final byte[] header = new byte[9];
            final ByteBuffer target = ByteBuffer.allocate ( payloadSize );
            boolean first = true;
            HpackEncoder.State state = null;
            final byte[] pushedStreamIdBytes = new byte[4];
            ByteUtil.set31Bits ( pushedStreamIdBytes, 0, pushedStreamId );
            target.put ( pushedStreamIdBytes );
            while ( state != HpackEncoder.State.COMPLETE ) {
                state = this.getHpackEncoder().encode ( coyoteRequest.getMimeHeaders(), target );
                target.flip();
                ByteUtil.setThreeBytes ( header, 0, target.limit() );
                if ( first ) {
                    first = false;
                    header[3] = FrameType.PUSH_PROMISE.getIdByte();
                } else {
                    header[3] = FrameType.CONTINUATION.getIdByte();
                }
                if ( state == HpackEncoder.State.COMPLETE ) {
                    final byte[] array = header;
                    final int n = 4;
                    array[n] += 4;
                }
                if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                    Http2UpgradeHandler.log.debug ( target.limit() + " bytes" );
                }
                ByteUtil.set31Bits ( header, 5, stream.getIdentifier() );
                this.socketWrapper.write ( true, header, 0, header.length );
                this.socketWrapper.write ( true, target );
                this.socketWrapper.flush ( true );
            }
        }
    }
    private HpackEncoder getHpackEncoder() {
        if ( this.hpackEncoder == null ) {
            this.hpackEncoder = new HpackEncoder ( this.remoteSettings.getHeaderTableSize() );
        }
        return this.hpackEncoder;
    }
    void writeBody ( final Stream stream, final ByteBuffer data, final int len, final boolean finished ) throws IOException {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.writeBody", this.connectionId, stream.getIdentifier(), Integer.toString ( len ) ) );
        }
        final boolean writeable = stream.canWrite();
        final byte[] header = new byte[9];
        ByteUtil.setThreeBytes ( header, 0, len );
        header[3] = FrameType.DATA.getIdByte();
        if ( finished ) {
            header[4] = 1;
            stream.sentEndOfStream();
            if ( !stream.isActive() ) {
                this.activeRemoteStreamCount.decrementAndGet();
            }
        }
        if ( writeable ) {
            ByteUtil.set31Bits ( header, 5, stream.getIdentifier() );
            synchronized ( this.socketWrapper ) {
                try {
                    this.socketWrapper.write ( true, header, 0, header.length );
                    final int orgLimit = data.limit();
                    data.limit ( data.position() + len );
                    this.socketWrapper.write ( true, data );
                    data.limit ( orgLimit );
                    this.socketWrapper.flush ( true );
                } catch ( IOException ioe ) {
                    this.handleAppInitiatedIOException ( ioe );
                }
            }
        }
    }
    private void handleAppInitiatedIOException ( final IOException ioe ) throws IOException {
        this.close();
        throw ioe;
    }
    void writeWindowUpdate ( final Stream stream, final int increment, final boolean applicationInitiated ) throws IOException {
        if ( !stream.canWrite() ) {
            return;
        }
        synchronized ( this.socketWrapper ) {
            final byte[] frame = new byte[13];
            ByteUtil.setThreeBytes ( frame, 0, 4 );
            frame[3] = FrameType.WINDOW_UPDATE.getIdByte();
            ByteUtil.set31Bits ( frame, 9, increment );
            this.socketWrapper.write ( true, frame, 0, frame.length );
            ByteUtil.set31Bits ( frame, 5, stream.getIdentifier() );
            try {
                this.socketWrapper.write ( true, frame, 0, frame.length );
                this.socketWrapper.flush ( true );
            } catch ( IOException ioe ) {
                if ( !applicationInitiated ) {
                    throw ioe;
                }
                this.handleAppInitiatedIOException ( ioe );
            }
        }
    }
    private void processWrites() throws IOException {
        synchronized ( this.socketWrapper ) {
            if ( this.socketWrapper.flush ( false ) ) {
                this.socketWrapper.registerWriteInterest();
            }
        }
    }
    int reserveWindowSize ( final Stream stream, final int reservation ) throws IOException {
        int allocation = 0;
        synchronized ( stream ) {
            do {
                synchronized ( this ) {
                    if ( !stream.canWrite() ) {
                        throw new CloseNowException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.stream.notWritable", stream.getConnectionId(), stream.getIdentifier() ) );
                    }
                    final long windowSize = this.getWindowSize();
                    if ( windowSize < 1L || this.backLogSize > 0L ) {
                        int[] value = this.backLogStreams.get ( stream );
                        if ( value == null ) {
                            value = new int[] { reservation, 0 };
                            this.backLogStreams.put ( stream, value );
                            this.backLogSize += reservation;
                            for ( AbstractStream parent = stream.getParentStream(); parent != null && this.backLogStreams.putIfAbsent ( parent, new int[2] ) == null; parent = parent.getParentStream() ) {}
                        } else if ( value[1] > 0 ) {
                            allocation = value[1];
                            this.decrementWindowSize ( allocation );
                            if ( value[0] == 0 ) {
                                this.backLogStreams.remove ( stream );
                            } else {
                                value[1] = 0;
                            }
                        }
                    } else if ( windowSize < reservation ) {
                        allocation = ( int ) windowSize;
                        this.decrementWindowSize ( allocation );
                    } else {
                        allocation = reservation;
                        this.decrementWindowSize ( allocation );
                    }
                }
                if ( allocation == 0 ) {
                    try {
                        stream.wait();
                    } catch ( InterruptedException e ) {
                        throw new IOException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.windowSizeReservationInterrupted", this.connectionId, stream.getIdentifier(), Integer.toString ( reservation ) ), e );
                    }
                }
            } while ( allocation == 0 );
        }
        return allocation;
    }
    protected void incrementWindowSize ( final int increment ) throws Http2Exception {
        Set<AbstractStream> streamsToNotify = null;
        synchronized ( this ) {
            final long windowSize = this.getWindowSize();
            if ( windowSize < 1L && windowSize + increment > 0L ) {
                streamsToNotify = this.releaseBackLog ( ( int ) ( windowSize + increment ) );
            }
            super.incrementWindowSize ( increment );
        }
        if ( streamsToNotify != null ) {
            for ( final AbstractStream stream : streamsToNotify ) {
                synchronized ( stream ) {
                    stream.notifyAll();
                }
            }
        }
    }
    private synchronized Set<AbstractStream> releaseBackLog ( final int increment ) {
        final Set<AbstractStream> result = new HashSet<AbstractStream>();
        if ( this.backLogSize < increment ) {
            result.addAll ( this.backLogStreams.keySet() );
            this.backLogStreams.clear();
            this.backLogSize = 0L;
        } else {
            for ( int leftToAllocate = increment; leftToAllocate > 0; leftToAllocate = this.allocate ( this, leftToAllocate ) ) {}
            for ( final Map.Entry<AbstractStream, int[]> entry : this.backLogStreams.entrySet() ) {
                final int allocation = entry.getValue() [1];
                if ( allocation > 0 ) {
                    this.backLogSize -= allocation;
                    result.add ( entry.getKey() );
                }
            }
        }
        return result;
    }
    private int allocate ( final AbstractStream stream, final int allocation ) {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.allocate.debug", this.getConnectionId(), stream.getIdentifier(), Integer.toString ( allocation ) ) );
        }
        final int[] value = this.backLogStreams.get ( stream );
        if ( value[0] >= allocation ) {
            final int[] array = value;
            final int n = 0;
            array[n] -= allocation;
            final int[] array2 = value;
            final int n2 = 1;
            array2[n2] += allocation;
            return 0;
        }
        value[1] = value[0];
        value[0] = 0;
        int leftToAllocate = allocation - value[1];
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.allocate.left", this.getConnectionId(), stream.getIdentifier(), Integer.toString ( leftToAllocate ) ) );
        }
        final Set<AbstractStream> recipients = new HashSet<AbstractStream>();
        recipients.addAll ( stream.getChildStreams() );
        recipients.retainAll ( this.backLogStreams.keySet() );
        while ( leftToAllocate > 0 ) {
            if ( recipients.size() == 0 ) {
                this.backLogStreams.remove ( stream );
                return leftToAllocate;
            }
            int totalWeight = 0;
            for ( final AbstractStream recipient : recipients ) {
                if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                    Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.allocate.recipient", this.getConnectionId(), stream.getIdentifier(), recipient.getIdentifier(), Integer.toString ( recipient.getWeight() ) ) );
                }
                totalWeight += recipient.getWeight();
            }
            final Iterator<AbstractStream> iter = recipients.iterator();
            int allocated = 0;
            while ( iter.hasNext() ) {
                final AbstractStream recipient2 = iter.next();
                int share = leftToAllocate * recipient2.getWeight() / totalWeight;
                if ( share == 0 ) {
                    share = 1;
                }
                final int remainder = this.allocate ( recipient2, share );
                if ( remainder > 0 ) {
                    iter.remove();
                }
                allocated += share - remainder;
            }
            leftToAllocate -= allocated;
        }
        return 0;
    }
    private Stream getStream ( final int streamId, final boolean unknownIsError ) throws ConnectionException {
        final Integer key = streamId;
        final Stream result = this.streams.get ( key );
        if ( result == null && unknownIsError ) {
            throw new ConnectionException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.stream.closed", key ), Http2Error.PROTOCOL_ERROR );
        }
        return result;
    }
    private Stream createRemoteStream ( final int streamId ) throws ConnectionException {
        final Integer key = streamId;
        if ( streamId % 2 != 1 ) {
            throw new ConnectionException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.stream.even", key ), Http2Error.PROTOCOL_ERROR );
        }
        if ( streamId <= this.maxRemoteStreamId ) {
            throw new ConnectionException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.stream.old", key, this.maxRemoteStreamId ), Http2Error.PROTOCOL_ERROR );
        }
        this.pruneClosedStreams();
        final Stream result = new Stream ( key, this );
        this.streams.put ( key, result );
        this.maxRemoteStreamId = streamId;
        return result;
    }
    private Stream createLocalStream ( final Request request ) {
        final int streamId = this.nextLocalStreamId.getAndAdd ( 2 );
        final Integer key = streamId;
        final Stream result = new Stream ( key, this, request );
        this.streams.put ( key, result );
        this.maxRemoteStreamId = streamId;
        return result;
    }
    private void close() {
        this.connectionState.set ( ConnectionState.CLOSED );
        try {
            this.socketWrapper.close();
        } catch ( IOException ioe ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.socketCloseFailed" ), ioe );
        }
    }
    private void pruneClosedStreams() {
        if ( this.newStreamsSinceLastPrune < 9 ) {
            ++this.newStreamsSinceLastPrune;
            return;
        }
        this.newStreamsSinceLastPrune = 0;
        long max = this.localSettings.getMaxConcurrentStreams();
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.pruneStart", this.connectionId, Long.toString ( max ), Integer.toString ( this.streams.size() ) ) );
        }
        max += max / 10L;
        if ( max > 2147483647L ) {
            max = 2147483647L;
        }
        int toClose = this.streams.size() - ( int ) max;
        if ( toClose < 1 ) {
            return;
        }
        final TreeSet<Integer> additionalCandidates = new TreeSet<Integer>();
        final Iterator<Map.Entry<Integer, Stream>> entryIter = this.streams.entrySet().iterator();
        while ( entryIter.hasNext() && toClose > 0 ) {
            final Map.Entry<Integer, Stream> entry = entryIter.next();
            final Stream stream = entry.getValue();
            if ( !stream.isActive() ) {
                if ( stream.getChildStreams().size() > 0 ) {
                    continue;
                }
                if ( stream.isClosedFinal() ) {
                    additionalCandidates.add ( entry.getKey() );
                } else {
                    if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                        Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.pruned", this.connectionId, entry.getKey() ) );
                    }
                    entryIter.remove();
                    --toClose;
                }
            }
        }
        while ( toClose > 0 && additionalCandidates.size() > 0 ) {
            final Integer pruned = additionalCandidates.pollLast();
            if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.prunedPriority", this.connectionId, pruned ) );
            }
            ++toClose;
        }
        if ( toClose > 0 ) {
            Http2UpgradeHandler.log.warn ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.pruneIncomplete", this.connectionId, Integer.toString ( toClose ) ) );
        }
    }
    void push ( final Request request, final Stream associatedStream ) throws IOException {
        final Stream pushStream = this.createLocalStream ( request );
        this.writePushHeaders ( associatedStream, pushStream.getIdentifier(), request, 1024 );
        pushStream.sentPushPromise();
        this.processStreamOnContainerThread ( pushStream );
    }
    protected final String getConnectionId() {
        return this.connectionId;
    }
    protected final int getWeight() {
        return 0;
    }
    boolean isTrailerHeaderAllowed ( final String headerName ) {
        return this.allowedTrailerHeaders.contains ( headerName );
    }
    public long getReadTimeout() {
        return this.readTimeout;
    }
    public void setReadTimeout ( final long readTimeout ) {
        this.readTimeout = readTimeout;
    }
    public long getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }
    public void setKeepAliveTimeout ( final long keepAliveTimeout ) {
        this.keepAliveTimeout = keepAliveTimeout;
    }
    public long getWriteTimeout() {
        return this.writeTimeout;
    }
    public void setWriteTimeout ( final long writeTimeout ) {
        this.writeTimeout = writeTimeout;
    }
    public void setMaxConcurrentStreams ( final long maxConcurrentStreams ) {
        this.localSettings.set ( Setting.MAX_CONCURRENT_STREAMS, maxConcurrentStreams );
    }
    public void setMaxConcurrentStreamExecution ( final int maxConcurrentStreamExecution ) {
        this.maxConcurrentStreamExecution = maxConcurrentStreamExecution;
    }
    public void setInitialWindowSize ( final int initialWindowSize ) {
        this.localSettings.set ( Setting.INITIAL_WINDOW_SIZE, initialWindowSize );
    }
    public void setAllowedTrailerHeaders ( final Set<String> allowedTrailerHeaders ) {
        this.allowedTrailerHeaders = allowedTrailerHeaders;
    }
    public void setMaxHeaderCount ( final int maxHeaderCount ) {
        this.maxHeaderCount = maxHeaderCount;
    }
    public int getMaxHeaderCount() {
        return this.maxHeaderCount;
    }
    public void setMaxHeaderSize ( final int maxHeaderSize ) {
        this.maxHeaderSize = maxHeaderSize;
    }
    public int getMaxHeaderSize() {
        return this.maxHeaderSize;
    }
    public void setMaxTrailerCount ( final int maxTrailerCount ) {
        this.maxTrailerCount = maxTrailerCount;
    }
    public int getMaxTrailerCount() {
        return this.maxTrailerCount;
    }
    public void setMaxTrailerSize ( final int maxTrailerSize ) {
        this.maxTrailerSize = maxTrailerSize;
    }
    public int getMaxTrailerSize() {
        return this.maxTrailerSize;
    }
    @Override
    public boolean fill ( final boolean block, final byte[] data, final int offset, final int length ) throws IOException {
        int len = length;
        int pos = offset;
        boolean nextReadBlock = block;
        int thisRead = 0;
        while ( len > 0 ) {
            thisRead = this.socketWrapper.read ( nextReadBlock, data, pos, len );
            if ( thisRead == 0 ) {
                if ( nextReadBlock ) {
                    throw new IllegalStateException();
                }
                return false;
            } else if ( thisRead == -1 ) {
                if ( this.connectionState.get().isNewStreamAllowed() ) {
                    throw new EOFException();
                }
                return false;
            } else {
                pos += thisRead;
                len -= thisRead;
                nextReadBlock = true;
            }
        }
        return true;
    }
    @Override
    public int getMaxFrameSize() {
        return this.localSettings.getMaxFrameSize();
    }
    @Override
    public HpackDecoder getHpackDecoder() {
        if ( this.hpackDecoder == null ) {
            this.hpackDecoder = new HpackDecoder ( this.localSettings.getHeaderTableSize() );
        }
        return this.hpackDecoder;
    }
    @Override
    public ByteBuffer startRequestBodyFrame ( final int streamId, final int payloadSize ) throws Http2Exception {
        final Stream stream = this.getStream ( streamId, true );
        stream.checkState ( FrameType.DATA );
        return stream.getInputByteBuffer();
    }
    @Override
    public void endRequestBodyFrame ( final int streamId ) throws Http2Exception {
        final Stream stream = this.getStream ( streamId, true );
        stream.getInputBuffer().onDataAvailable();
    }
    @Override
    public void receivedEndOfStream ( final int streamId ) throws ConnectionException {
        final Stream stream = this.getStream ( streamId, this.connectionState.get().isNewStreamAllowed() );
        if ( stream != null ) {
            stream.receivedEndOfStream();
            if ( !stream.isActive() ) {
                this.activeRemoteStreamCount.decrementAndGet();
            }
        }
    }
    @Override
    public void swallowedPadding ( final int streamId, final int paddingLength ) throws ConnectionException, IOException {
        final Stream stream = this.getStream ( streamId, true );
        this.writeWindowUpdate ( stream, paddingLength + 1, false );
    }
    @Override
    public HpackDecoder.HeaderEmitter headersStart ( final int streamId, final boolean headersEndStream ) throws Http2Exception {
        if ( !this.connectionState.get().isNewStreamAllowed() ) {
            if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.noNewStreams", this.connectionId, Integer.toString ( streamId ) ) );
            }
            return Http2UpgradeHandler.HEADER_SINK;
        }
        Stream stream = this.getStream ( streamId, false );
        if ( stream == null ) {
            stream = this.createRemoteStream ( streamId );
        }
        stream.checkState ( FrameType.HEADERS );
        stream.receivedStartOfHeaders ( headersEndStream );
        this.closeIdleStreams ( streamId );
        if ( this.localSettings.getMaxConcurrentStreams() < this.activeRemoteStreamCount.incrementAndGet() ) {
            this.activeRemoteStreamCount.decrementAndGet();
            throw new StreamException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.tooManyRemoteStreams", Long.toString ( this.localSettings.getMaxConcurrentStreams() ) ), Http2Error.REFUSED_STREAM, streamId );
        }
        return stream;
    }
    private void closeIdleStreams ( final int newMaxActiveRemoteStreamId ) throws Http2Exception {
        for ( int i = this.maxActiveRemoteStreamId + 2; i < newMaxActiveRemoteStreamId; i += 2 ) {
            final Stream stream = this.getStream ( i, false );
            if ( stream != null ) {
                stream.closeIfIdle();
            }
        }
        this.maxActiveRemoteStreamId = newMaxActiveRemoteStreamId;
    }
    @Override
    public void reprioritise ( final int streamId, final int parentStreamId, final boolean exclusive, final int weight ) throws Http2Exception {
        Stream stream = this.getStream ( streamId, false );
        if ( stream == null ) {
            stream = this.createRemoteStream ( streamId );
        }
        stream.checkState ( FrameType.PRIORITY );
        AbstractStream parentStream = this.getStream ( parentStreamId, false );
        if ( parentStream == null ) {
            parentStream = this;
        }
        stream.rePrioritise ( parentStream, exclusive, weight );
    }
    @Override
    public void headersEnd ( final int streamId ) throws ConnectionException {
        this.setMaxProcessedStream ( streamId );
        final Stream stream = this.getStream ( streamId, this.connectionState.get().isNewStreamAllowed() );
        if ( stream != null && stream.isActive() && stream.receivedEndOfHeaders() ) {
            this.processStreamOnContainerThread ( stream );
        }
    }
    private void setMaxProcessedStream ( final int streamId ) {
        if ( this.maxProcessedStreamId < streamId ) {
            this.maxProcessedStreamId = streamId;
        }
    }
    @Override
    public void reset ( final int streamId, final long errorCode ) throws Http2Exception {
        final Stream stream = this.getStream ( streamId, true );
        stream.checkState ( FrameType.RST );
        stream.receiveReset ( errorCode );
    }
    @Override
    public void setting ( final Setting setting, final long value ) throws ConnectionException {
        if ( setting == Setting.INITIAL_WINDOW_SIZE ) {
            final long oldValue = this.remoteSettings.getInitialWindowSize();
            this.remoteSettings.set ( setting, value );
            final int diff = ( int ) ( value - oldValue );
            for ( final Stream stream : this.streams.values() ) {
                try {
                    stream.incrementWindowSize ( diff );
                } catch ( Http2Exception h2e ) {
                    stream.close ( new StreamException ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.windowSizeTooBig", this.connectionId, stream.getIdentifier() ), h2e.getError(), stream.getIdentifier() ) );
                }
            }
        } else {
            this.remoteSettings.set ( setting, value );
        }
    }
    @Override
    public void settingsEnd ( final boolean ack ) throws IOException {
        if ( ack ) {
            if ( !this.localSettings.ack() ) {
                Http2UpgradeHandler.log.warn ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.unexpectedAck", this.connectionId, this.getIdentifier() ) );
            }
        } else {
            synchronized ( this.socketWrapper ) {
                this.socketWrapper.write ( true, Http2UpgradeHandler.SETTINGS_ACK, 0, Http2UpgradeHandler.SETTINGS_ACK.length );
                this.socketWrapper.flush ( true );
            }
        }
    }
    @Override
    public void pingReceive ( final byte[] payload, final boolean ack ) throws IOException {
        this.pingManager.receivePing ( payload, ack );
    }
    @Override
    public void goaway ( final int lastStreamId, final long errorCode, final String debugData ) {
        if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
            Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "upgradeHandler.goaway.debug", this.connectionId, Integer.toString ( lastStreamId ), Long.toHexString ( errorCode ), debugData ) );
        }
    }
    @Override
    public void incrementWindowSize ( final int streamId, final int increment ) throws Http2Exception {
        if ( streamId == 0 ) {
            this.incrementWindowSize ( increment );
        } else {
            final Stream stream = this.getStream ( streamId, true );
            stream.checkState ( FrameType.WINDOW_UPDATE );
            stream.incrementWindowSize ( increment );
        }
    }
    @Override
    public void swallowed ( final int streamId, final FrameType frameType, final int flags, final int size ) throws IOException {
    }
    static {
        log = LogFactory.getLog ( Http2UpgradeHandler.class );
        sm = StringManager.getManager ( Http2UpgradeHandler.class );
        connectionIdGenerator = new AtomicInteger ( 0 );
        STREAM_ID_ZERO = 0;
        PING = new byte[] { 0, 0, 8, 6, 0, 0, 0, 0, 0 };
        PING_ACK = new byte[] { 0, 0, 8, 6, 1, 0, 0, 0, 0 };
        SETTINGS_ACK = new byte[] { 0, 0, 0, 4, 1, 0, 0, 0, 0 };
        GOAWAY = new byte[] { 7, 0, 0, 0, 0, 0 };
        HEADER_SINK = new HeaderSink();
    }
    private class PingManager {
        private final long pingIntervalNano = 10000000000L;
        private int sequence;
        private long lastPingNanoTime;
        private Queue<PingRecord> inflightPings;
        private Queue<Long> roundTripTimes;
        private PingManager() {
            this.sequence = 0;
            this.lastPingNanoTime = Long.MIN_VALUE;
            this.inflightPings = new ConcurrentLinkedQueue<PingRecord>();
            this.roundTripTimes = new ConcurrentLinkedQueue<Long>();
        }
        public void sendPing ( final boolean force ) throws IOException {
            final long now = System.nanoTime();
            if ( force || now - this.lastPingNanoTime > 10000000000L ) {
                this.lastPingNanoTime = now;
                final byte[] payload = new byte[8];
                synchronized ( Http2UpgradeHandler.this.socketWrapper ) {
                    final int sentSequence = ++this.sequence;
                    final PingRecord pingRecord = new PingRecord ( sentSequence, now );
                    this.inflightPings.add ( pingRecord );
                    ByteUtil.set31Bits ( payload, 4, sentSequence );
                    Http2UpgradeHandler.this.socketWrapper.write ( true, Http2UpgradeHandler.PING, 0, Http2UpgradeHandler.PING.length );
                    Http2UpgradeHandler.this.socketWrapper.write ( true, payload, 0, payload.length );
                    Http2UpgradeHandler.this.socketWrapper.flush ( true );
                }
            }
        }
        public void receivePing ( final byte[] payload, final boolean ack ) throws IOException {
            if ( ack ) {
                int receivedSequence;
                PingRecord pingRecord;
                for ( receivedSequence = ByteUtil.get31Bits ( payload, 4 ), pingRecord = this.inflightPings.poll(); pingRecord != null && pingRecord.getSequence() < receivedSequence; pingRecord = this.inflightPings.poll() ) {}
                if ( pingRecord != null ) {
                    final long roundTripTime = System.nanoTime() - pingRecord.getSentNanoTime();
                    this.roundTripTimes.add ( roundTripTime );
                    while ( this.roundTripTimes.size() > 3 ) {
                        this.roundTripTimes.poll();
                    }
                    if ( Http2UpgradeHandler.log.isDebugEnabled() ) {
                        Http2UpgradeHandler.log.debug ( Http2UpgradeHandler.sm.getString ( "pingManager.roundTripTime", Http2UpgradeHandler.this.connectionId, roundTripTime ) );
                    }
                }
            } else {
                synchronized ( Http2UpgradeHandler.this.socketWrapper ) {
                    Http2UpgradeHandler.this.socketWrapper.write ( true, Http2UpgradeHandler.PING_ACK, 0, Http2UpgradeHandler.PING_ACK.length );
                    Http2UpgradeHandler.this.socketWrapper.write ( true, payload, 0, payload.length );
                    Http2UpgradeHandler.this.socketWrapper.flush ( true );
                }
            }
        }
        public long getRoundTripTimeNano() {
            return ( long ) this.roundTripTimes.stream().mapToLong ( x -> x ).average().orElse ( 0.0 );
        }
    }
    private static class PingRecord {
        private final int sequence;
        private final long sentNanoTime;
        public PingRecord ( final int sequence, final long sentNanoTime ) {
            this.sequence = sequence;
            this.sentNanoTime = sentNanoTime;
        }
        public int getSequence() {
            return this.sequence;
        }
        public long getSentNanoTime() {
            return this.sentNanoTime;
        }
    }
    private enum ConnectionState {
        NEW ( true ),
        CONNECTED ( true ),
        PAUSING ( true ),
        PAUSED ( false ),
        CLOSED ( false );
        private final boolean newStreamsAllowed;
        private ConnectionState ( final boolean newStreamsAllowed ) {
            this.newStreamsAllowed = newStreamsAllowed;
        }
        public boolean isNewStreamAllowed() {
            return this.newStreamsAllowed;
        }
    }
}

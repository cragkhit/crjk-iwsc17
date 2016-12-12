package org.apache.tomcat.websocket;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import java.util.Collection;
import java.util.Iterator;
import java.net.SocketTimeoutException;
import javax.websocket.CloseReason;
import java.util.concurrent.TimeUnit;
import java.nio.charset.CoderResult;
import java.io.Writer;
import java.io.OutputStream;
import java.nio.CharBuffer;
import javax.websocket.SendHandler;
import java.util.concurrent.Future;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.tomcat.util.buf.Utf8Encoder;
import java.util.ArrayDeque;
import org.apache.juli.logging.LogFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.charset.CharsetEncoder;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import org.apache.juli.logging.Log;
import javax.websocket.SendResult;
import org.apache.tomcat.util.res.StringManager;
import javax.websocket.RemoteEndpoint;
public abstract class WsRemoteEndpointImplBase implements RemoteEndpoint {
    private static final StringManager sm;
    protected static final SendResult SENDRESULT_OK;
    private final Log log;
    private final StateMachine stateMachine;
    private final IntermediateMessageHandler intermediateMessageHandler;
    private Transformation transformation;
    private final Semaphore messagePartInProgress;
    private final Queue<MessagePart> messagePartQueue;
    private final Object messagePartLock;
    private volatile boolean closed;
    private boolean fragmented;
    private boolean nextFragmented;
    private boolean text;
    private boolean nextText;
    private final ByteBuffer headerBuffer;
    private final ByteBuffer outputBuffer;
    private final CharsetEncoder encoder;
    private final ByteBuffer encoderBuffer;
    private final AtomicBoolean batchingAllowed;
    private volatile long sendTimeout;
    private WsSession wsSession;
    private List<EncoderEntry> encoderEntries;
    public WsRemoteEndpointImplBase() {
        this.log = LogFactory.getLog ( WsRemoteEndpointImplBase.class );
        this.stateMachine = new StateMachine();
        this.intermediateMessageHandler = new IntermediateMessageHandler ( this );
        this.transformation = null;
        this.messagePartInProgress = new Semaphore ( 1 );
        this.messagePartQueue = new ArrayDeque<MessagePart>();
        this.messagePartLock = new Object();
        this.closed = false;
        this.fragmented = false;
        this.nextFragmented = false;
        this.text = false;
        this.nextText = false;
        this.headerBuffer = ByteBuffer.allocate ( 14 );
        this.outputBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
        this.encoder = new Utf8Encoder();
        this.encoderBuffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
        this.batchingAllowed = new AtomicBoolean ( false );
        this.sendTimeout = -1L;
        this.encoderEntries = new ArrayList<EncoderEntry>();
    }
    protected void setTransformation ( final Transformation transformation ) {
        this.transformation = transformation;
    }
    public long getSendTimeout() {
        return this.sendTimeout;
    }
    public void setSendTimeout ( final long timeout ) {
        this.sendTimeout = timeout;
    }
    public void setBatchingAllowed ( final boolean batchingAllowed ) throws IOException {
        final boolean oldValue = this.batchingAllowed.getAndSet ( batchingAllowed );
        if ( oldValue && !batchingAllowed ) {
            this.flushBatch();
        }
    }
    public boolean getBatchingAllowed() {
        return this.batchingAllowed.get();
    }
    public void flushBatch() throws IOException {
        this.sendMessageBlock ( ( byte ) 24, null, true );
    }
    public void sendBytes ( final ByteBuffer data ) throws IOException {
        if ( data == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        this.stateMachine.binaryStart();
        this.sendMessageBlock ( ( byte ) 2, data, true );
        this.stateMachine.complete ( true );
    }
    public Future<Void> sendBytesByFuture ( final ByteBuffer data ) {
        final FutureToSendHandler f2sh = new FutureToSendHandler ( this.wsSession );
        this.sendBytesByCompletion ( data, ( SendHandler ) f2sh );
        return f2sh;
    }
    public void sendBytesByCompletion ( final ByteBuffer data, final SendHandler handler ) {
        if ( data == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        if ( handler == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullHandler" ) );
        }
        final StateUpdateSendHandler sush = new StateUpdateSendHandler ( handler, this.stateMachine );
        this.stateMachine.binaryStart();
        this.startMessage ( ( byte ) 2, data, true, ( SendHandler ) sush );
    }
    public void sendPartialBytes ( final ByteBuffer partialByte, final boolean last ) throws IOException {
        if ( partialByte == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        this.stateMachine.binaryPartialStart();
        this.sendMessageBlock ( ( byte ) 2, partialByte, last );
        this.stateMachine.complete ( last );
    }
    public void sendPing ( final ByteBuffer applicationData ) throws IOException, IllegalArgumentException {
        if ( applicationData.remaining() > 125 ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.tooMuchData" ) );
        }
        this.sendMessageBlock ( ( byte ) 9, applicationData, true );
    }
    public void sendPong ( final ByteBuffer applicationData ) throws IOException, IllegalArgumentException {
        if ( applicationData.remaining() > 125 ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.tooMuchData" ) );
        }
        this.sendMessageBlock ( ( byte ) 10, applicationData, true );
    }
    public void sendString ( final String text ) throws IOException {
        if ( text == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        this.stateMachine.textStart();
        this.sendMessageBlock ( CharBuffer.wrap ( text ), true );
    }
    public Future<Void> sendStringByFuture ( final String text ) {
        final FutureToSendHandler f2sh = new FutureToSendHandler ( this.wsSession );
        this.sendStringByCompletion ( text, ( SendHandler ) f2sh );
        return f2sh;
    }
    public void sendStringByCompletion ( final String text, final SendHandler handler ) {
        if ( text == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        if ( handler == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullHandler" ) );
        }
        this.stateMachine.textStart();
        final TextMessageSendHandler tmsh = new TextMessageSendHandler ( handler, CharBuffer.wrap ( text ), true, this.encoder, this.encoderBuffer, this );
        tmsh.write();
    }
    public void sendPartialString ( final String fragment, final boolean isLast ) throws IOException {
        if ( fragment == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        this.stateMachine.textPartialStart();
        this.sendMessageBlock ( CharBuffer.wrap ( fragment ), isLast );
    }
    public OutputStream getSendStream() {
        this.stateMachine.streamStart();
        return new WsOutputStream ( this );
    }
    public Writer getSendWriter() {
        this.stateMachine.writeStart();
        return new WsWriter ( this );
    }
    void sendMessageBlock ( final CharBuffer part, final boolean last ) throws IOException {
        final long timeoutExpiry = this.getTimeoutExpiry();
        boolean isDone = false;
        while ( !isDone ) {
            this.encoderBuffer.clear();
            final CoderResult cr = this.encoder.encode ( part, this.encoderBuffer, true );
            if ( cr.isError() ) {
                throw new IllegalArgumentException ( cr.toString() );
            }
            isDone = !cr.isOverflow();
            this.encoderBuffer.flip();
            this.sendMessageBlock ( ( byte ) 1, this.encoderBuffer, last && isDone, timeoutExpiry );
        }
        this.stateMachine.complete ( last );
    }
    void sendMessageBlock ( final byte opCode, final ByteBuffer payload, final boolean last ) throws IOException {
        this.sendMessageBlock ( opCode, payload, last, this.getTimeoutExpiry() );
    }
    private long getTimeoutExpiry() {
        final long timeout = this.getBlockingSendTimeout();
        if ( timeout < 0L ) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() + timeout;
    }
    private void sendMessageBlock ( final byte opCode, final ByteBuffer payload, final boolean last, final long timeoutExpiry ) throws IOException {
        this.wsSession.updateLastActive();
        final BlockingSendHandler bsh = new BlockingSendHandler();
        List<MessagePart> messageParts = new ArrayList<MessagePart>();
        messageParts.add ( new MessagePart ( last, 0, opCode, payload, ( SendHandler ) bsh, ( SendHandler ) bsh, timeoutExpiry ) );
        messageParts = this.transformation.sendMessagePart ( messageParts );
        if ( messageParts.size() == 0 ) {
            return;
        }
        final long timeout = timeoutExpiry - System.currentTimeMillis();
        try {
            if ( !this.messagePartInProgress.tryAcquire ( timeout, TimeUnit.MILLISECONDS ) ) {
                final String msg = WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.acquireTimeout" );
                this.wsSession.doClose ( new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, msg ), new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, msg ) );
                throw new SocketTimeoutException ( msg );
            }
        } catch ( InterruptedException e ) {
            final String msg2 = WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.sendInterupt" );
            this.wsSession.doClose ( new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, msg2 ), new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, msg2 ) );
            throw new IOException ( msg2, e );
        }
        for ( final MessagePart mp : messageParts ) {
            this.writeMessagePart ( mp );
            if ( !bsh.getSendResult().isOK() ) {
                this.messagePartInProgress.release();
                final Throwable t = bsh.getSendResult().getException();
                this.wsSession.doClose ( new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, t.getMessage() ), new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, t.getMessage() ) );
                throw new IOException ( t );
            }
            this.fragmented = this.nextFragmented;
            this.text = this.nextText;
        }
        if ( payload != null ) {
            payload.clear();
        }
        this.endMessage ( null, null );
    }
    void startMessage ( final byte opCode, final ByteBuffer payload, final boolean last, final SendHandler handler ) {
        this.wsSession.updateLastActive();
        List<MessagePart> messageParts = new ArrayList<MessagePart>();
        messageParts.add ( new MessagePart ( last, 0, opCode, payload, ( SendHandler ) this.intermediateMessageHandler, ( SendHandler ) new EndMessageHandler ( this, handler ), -1L ) );
        messageParts = this.transformation.sendMessagePart ( messageParts );
        if ( messageParts.size() == 0 ) {
            handler.onResult ( new SendResult() );
            return;
        }
        final MessagePart mp = messageParts.remove ( 0 );
        boolean doWrite = false;
        synchronized ( this.messagePartLock ) {
            if ( 8 == mp.getOpCode() && this.getBatchingAllowed() ) {
                this.log.warn ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.flushOnCloseFailed" ) );
            }
            if ( this.messagePartInProgress.tryAcquire() ) {
                doWrite = true;
            } else {
                this.messagePartQueue.add ( mp );
            }
            this.messagePartQueue.addAll ( ( Collection<?> ) messageParts );
        }
        if ( doWrite ) {
            this.writeMessagePart ( mp );
        }
    }
    void endMessage ( final SendHandler handler, final SendResult result ) {
        boolean doWrite = false;
        MessagePart mpNext = null;
        synchronized ( this.messagePartLock ) {
            this.fragmented = this.nextFragmented;
            this.text = this.nextText;
            mpNext = this.messagePartQueue.poll();
            if ( mpNext == null ) {
                this.messagePartInProgress.release();
            } else if ( !this.closed ) {
                doWrite = true;
            }
        }
        if ( doWrite ) {
            this.writeMessagePart ( mpNext );
        }
        this.wsSession.updateLastActive();
        if ( handler != null ) {
            handler.onResult ( result );
        }
    }
    void writeMessagePart ( final MessagePart mp ) {
        if ( this.closed ) {
            throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closed" ) );
        }
        if ( 24 == mp.getOpCode() ) {
            this.nextFragmented = this.fragmented;
            this.nextText = this.text;
            this.outputBuffer.flip();
            final SendHandler flushHandler = ( SendHandler ) new OutputBufferFlushSendHandler ( this.outputBuffer, mp.getEndHandler() );
            this.doWrite ( flushHandler, mp.getBlockingWriteTimeoutExpiry(), this.outputBuffer );
            return;
        }
        boolean first;
        if ( Util.isControl ( mp.getOpCode() ) ) {
            this.nextFragmented = this.fragmented;
            this.nextText = this.text;
            if ( mp.getOpCode() == 8 ) {
                this.closed = true;
            }
            first = true;
        } else {
            final boolean isText = Util.isText ( mp.getOpCode() );
            if ( this.fragmented ) {
                if ( this.text != isText ) {
                    throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.changeType" ) );
                }
                this.nextText = this.text;
                this.nextFragmented = !mp.isFin();
                first = false;
            } else {
                if ( mp.isFin() ) {
                    this.nextFragmented = false;
                } else {
                    this.nextFragmented = true;
                    this.nextText = isText;
                }
                first = true;
            }
        }
        byte[] mask;
        if ( this.isMasked() ) {
            mask = Util.generateMask();
        } else {
            mask = null;
        }
        this.headerBuffer.clear();
        writeHeader ( this.headerBuffer, mp.isFin(), mp.getRsv(), mp.getOpCode(), this.isMasked(), mp.getPayload(), mask, first );
        this.headerBuffer.flip();
        if ( this.getBatchingAllowed() || this.isMasked() ) {
            final OutputBufferSendHandler obsh = new OutputBufferSendHandler ( mp.getEndHandler(), mp.getBlockingWriteTimeoutExpiry(), this.headerBuffer, mp.getPayload(), mask, this.outputBuffer, !this.getBatchingAllowed(), this );
            obsh.write();
        } else {
            this.doWrite ( mp.getEndHandler(), mp.getBlockingWriteTimeoutExpiry(), this.headerBuffer, mp.getPayload() );
        }
    }
    private long getBlockingSendTimeout() {
        final Object obj = this.wsSession.getUserProperties().get ( "org.apache.tomcat.websocket.BLOCKING_SEND_TIMEOUT" );
        Long userTimeout = null;
        if ( obj instanceof Long ) {
            userTimeout = ( Long ) obj;
        }
        if ( userTimeout == null ) {
            return 20000L;
        }
        return userTimeout;
    }
    public void sendObject ( final Object obj ) throws IOException, EncodeException {
        if ( obj == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        final Encoder encoder = this.findEncoder ( obj );
        if ( encoder == null && Util.isPrimitive ( obj.getClass() ) ) {
            final String msg = obj.toString();
            this.sendString ( msg );
            return;
        }
        if ( encoder == null && byte[].class.isAssignableFrom ( obj.getClass() ) ) {
            final ByteBuffer msg2 = ByteBuffer.wrap ( ( byte[] ) obj );
            this.sendBytes ( msg2 );
            return;
        }
        if ( encoder instanceof Encoder.Text ) {
            final String msg = ( ( Encoder.Text ) encoder ).encode ( obj );
            this.sendString ( msg );
        } else if ( encoder instanceof Encoder.TextStream ) {
            try ( final Writer w = this.getSendWriter() ) {
                ( ( Encoder.TextStream ) encoder ).encode ( obj, w );
            }
        } else if ( encoder instanceof Encoder.Binary ) {
            final ByteBuffer msg2 = ( ( Encoder.Binary ) encoder ).encode ( obj );
            this.sendBytes ( msg2 );
        } else {
            if ( ! ( encoder instanceof Encoder.BinaryStream ) ) {
                throw new EncodeException ( obj, WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.noEncoder", obj.getClass() ) );
            }
            try ( final OutputStream os = this.getSendStream() ) {
                ( ( Encoder.BinaryStream ) encoder ).encode ( obj, os );
            }
        }
    }
    public Future<Void> sendObjectByFuture ( final Object obj ) {
        final FutureToSendHandler f2sh = new FutureToSendHandler ( this.wsSession );
        this.sendObjectByCompletion ( obj, ( SendHandler ) f2sh );
        return f2sh;
    }
    public void sendObjectByCompletion ( final Object obj, final SendHandler completion ) {
        if ( obj == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullData" ) );
        }
        if ( completion == null ) {
            throw new IllegalArgumentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.nullHandler" ) );
        }
        final Encoder encoder = this.findEncoder ( obj );
        if ( encoder == null && Util.isPrimitive ( obj.getClass() ) ) {
            final String msg = obj.toString();
            this.sendStringByCompletion ( msg, completion );
            return;
        }
        if ( encoder == null && byte[].class.isAssignableFrom ( obj.getClass() ) ) {
            final ByteBuffer msg2 = ByteBuffer.wrap ( ( byte[] ) obj );
            this.sendBytesByCompletion ( msg2, completion );
            return;
        }
        try {
            if ( encoder instanceof Encoder.Text ) {
                final String msg = ( ( Encoder.Text ) encoder ).encode ( obj );
                this.sendStringByCompletion ( msg, completion );
            } else if ( encoder instanceof Encoder.TextStream ) {
                try ( final Writer w = this.getSendWriter() ) {
                    ( ( Encoder.TextStream ) encoder ).encode ( obj, w );
                }
                completion.onResult ( new SendResult() );
            } else if ( encoder instanceof Encoder.Binary ) {
                final ByteBuffer msg2 = ( ( Encoder.Binary ) encoder ).encode ( obj );
                this.sendBytesByCompletion ( msg2, completion );
            } else {
                if ( ! ( encoder instanceof Encoder.BinaryStream ) ) {
                    throw new EncodeException ( obj, WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.noEncoder", obj.getClass() ) );
                }
                try ( final OutputStream os = this.getSendStream() ) {
                    ( ( Encoder.BinaryStream ) encoder ).encode ( obj, os );
                }
                completion.onResult ( new SendResult() );
            }
        } catch ( Exception e ) {
            final SendResult sr = new SendResult ( ( Throwable ) e );
            completion.onResult ( sr );
        }
    }
    protected void setSession ( final WsSession wsSession ) {
        this.wsSession = wsSession;
    }
    protected void setEncoders ( final EndpointConfig endpointConfig ) throws DeploymentException {
        this.encoderEntries.clear();
        for ( final Class<? extends Encoder> encoderClazz : endpointConfig.getEncoders() ) {
            Encoder instance;
            try {
                instance = ( Encoder ) encoderClazz.newInstance();
                instance.init ( endpointConfig );
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.invalidEncoder", encoderClazz.getName() ), ( Throwable ) e );
            }
            final EncoderEntry entry = new EncoderEntry ( Util.getEncoderType ( encoderClazz ), instance );
            this.encoderEntries.add ( entry );
        }
    }
    private Encoder findEncoder ( final Object obj ) {
        for ( final EncoderEntry entry : this.encoderEntries ) {
            if ( entry.getClazz().isAssignableFrom ( obj.getClass() ) ) {
                return entry.getEncoder();
            }
        }
        return null;
    }
    public final void close() {
        for ( final EncoderEntry entry : this.encoderEntries ) {
            entry.getEncoder().destroy();
        }
        this.transformation.close();
        this.doClose();
    }
    protected abstract void doWrite ( final SendHandler p0, final long p1, final ByteBuffer... p2 );
    protected abstract boolean isMasked();
    protected abstract void doClose();
    private static void writeHeader ( final ByteBuffer headerBuffer, final boolean fin, final int rsv, final byte opCode, final boolean masked, final ByteBuffer payload, final byte[] mask, final boolean first ) {
        byte b = 0;
        if ( fin ) {
            b -= 128;
        }
        b += ( byte ) ( rsv << 4 );
        if ( first ) {
            b += opCode;
        }
        headerBuffer.put ( b );
        if ( masked ) {
            b = -128;
        } else {
            b = 0;
        }
        if ( payload.limit() < 126 ) {
            headerBuffer.put ( ( byte ) ( payload.limit() | b ) );
        } else if ( payload.limit() < 65536 ) {
            headerBuffer.put ( ( byte ) ( 0x7E | b ) );
            headerBuffer.put ( ( byte ) ( payload.limit() >>> 8 ) );
            headerBuffer.put ( ( byte ) ( payload.limit() & 0xFF ) );
        } else {
            headerBuffer.put ( ( byte ) ( 0x7F | b ) );
            headerBuffer.put ( ( byte ) 0 );
            headerBuffer.put ( ( byte ) 0 );
            headerBuffer.put ( ( byte ) 0 );
            headerBuffer.put ( ( byte ) 0 );
            headerBuffer.put ( ( byte ) ( payload.limit() >>> 24 ) );
            headerBuffer.put ( ( byte ) ( payload.limit() >>> 16 ) );
            headerBuffer.put ( ( byte ) ( payload.limit() >>> 8 ) );
            headerBuffer.put ( ( byte ) ( payload.limit() & 0xFF ) );
        }
        if ( masked ) {
            headerBuffer.put ( mask[0] );
            headerBuffer.put ( mask[1] );
            headerBuffer.put ( mask[2] );
            headerBuffer.put ( mask[3] );
        }
    }
    static {
        sm = StringManager.getManager ( WsRemoteEndpointImplBase.class );
        SENDRESULT_OK = new SendResult();
    }
    private static class EndMessageHandler implements SendHandler {
        private final WsRemoteEndpointImplBase endpoint;
        private final SendHandler handler;
        public EndMessageHandler ( final WsRemoteEndpointImplBase endpoint, final SendHandler handler ) {
            this.endpoint = endpoint;
            this.handler = handler;
        }
        public void onResult ( final SendResult result ) {
            this.endpoint.endMessage ( this.handler, result );
        }
    }
    private static class IntermediateMessageHandler implements SendHandler {
        private final WsRemoteEndpointImplBase endpoint;
        public IntermediateMessageHandler ( final WsRemoteEndpointImplBase endpoint ) {
            this.endpoint = endpoint;
        }
        public void onResult ( final SendResult result ) {
            this.endpoint.endMessage ( null, result );
        }
    }
    private class TextMessageSendHandler implements SendHandler {
        private final SendHandler handler;
        private final CharBuffer message;
        private final boolean isLast;
        private final CharsetEncoder encoder;
        private final ByteBuffer buffer;
        private final WsRemoteEndpointImplBase endpoint;
        private volatile boolean isDone;
        public TextMessageSendHandler ( final SendHandler handler, final CharBuffer message, final boolean isLast, final CharsetEncoder encoder, final ByteBuffer encoderBuffer, final WsRemoteEndpointImplBase endpoint ) {
            this.isDone = false;
            this.handler = handler;
            this.message = message;
            this.isLast = isLast;
            this.encoder = encoder.reset();
            this.buffer = encoderBuffer;
            this.endpoint = endpoint;
        }
        public void write() {
            this.buffer.clear();
            final CoderResult cr = this.encoder.encode ( this.message, this.buffer, true );
            if ( cr.isError() ) {
                throw new IllegalArgumentException ( cr.toString() );
            }
            this.isDone = !cr.isOverflow();
            this.buffer.flip();
            this.endpoint.startMessage ( ( byte ) 1, this.buffer, this.isDone && this.isLast, ( SendHandler ) this );
        }
        public void onResult ( final SendResult result ) {
            if ( this.isDone ) {
                this.endpoint.stateMachine.complete ( this.isLast );
                this.handler.onResult ( result );
            } else if ( !result.isOK() ) {
                this.handler.onResult ( result );
            } else if ( WsRemoteEndpointImplBase.this.closed ) {
                final SendResult sr = new SendResult ( ( Throwable ) new IOException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closedDuringMessage" ) ) );
                this.handler.onResult ( sr );
            } else {
                this.write();
            }
        }
    }
    private static class OutputBufferSendHandler implements SendHandler {
        private final SendHandler handler;
        private final long blockingWriteTimeoutExpiry;
        private final ByteBuffer headerBuffer;
        private final ByteBuffer payload;
        private final byte[] mask;
        private final ByteBuffer outputBuffer;
        private final boolean flushRequired;
        private final WsRemoteEndpointImplBase endpoint;
        private int maskIndex;
        public OutputBufferSendHandler ( final SendHandler completion, final long blockingWriteTimeoutExpiry, final ByteBuffer headerBuffer, final ByteBuffer payload, final byte[] mask, final ByteBuffer outputBuffer, final boolean flushRequired, final WsRemoteEndpointImplBase endpoint ) {
            this.maskIndex = 0;
            this.blockingWriteTimeoutExpiry = blockingWriteTimeoutExpiry;
            this.handler = completion;
            this.headerBuffer = headerBuffer;
            this.payload = payload;
            this.mask = mask;
            this.outputBuffer = outputBuffer;
            this.flushRequired = flushRequired;
            this.endpoint = endpoint;
        }
        public void write() {
            while ( this.headerBuffer.hasRemaining() && this.outputBuffer.hasRemaining() ) {
                this.outputBuffer.put ( this.headerBuffer.get() );
            }
            if ( this.headerBuffer.hasRemaining() ) {
                this.outputBuffer.flip();
                this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
                return;
            }
            final int payloadLeft = this.payload.remaining();
            final int payloadLimit = this.payload.limit();
            final int outputSpace = this.outputBuffer.remaining();
            int toWrite;
            if ( ( toWrite = payloadLeft ) > outputSpace ) {
                toWrite = outputSpace;
                this.payload.limit ( this.payload.position() + toWrite );
            }
            if ( this.mask == null ) {
                this.outputBuffer.put ( this.payload );
            } else {
                for ( int i = 0; i < toWrite; ++i ) {
                    this.outputBuffer.put ( ( byte ) ( this.payload.get() ^ ( this.mask[this.maskIndex++] & 0xFF ) ) );
                    if ( this.maskIndex > 3 ) {
                        this.maskIndex = 0;
                    }
                }
            }
            if ( payloadLeft > outputSpace ) {
                this.payload.limit ( payloadLimit );
                this.outputBuffer.flip();
                this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
                return;
            }
            if ( this.flushRequired ) {
                this.outputBuffer.flip();
                if ( this.outputBuffer.remaining() == 0 ) {
                    this.handler.onResult ( WsRemoteEndpointImplBase.SENDRESULT_OK );
                } else {
                    this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
                }
            } else {
                this.handler.onResult ( WsRemoteEndpointImplBase.SENDRESULT_OK );
            }
        }
        public void onResult ( final SendResult result ) {
            if ( result.isOK() ) {
                if ( this.outputBuffer.hasRemaining() ) {
                    this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
                } else {
                    this.outputBuffer.clear();
                    this.write();
                }
            } else {
                this.handler.onResult ( result );
            }
        }
    }
    private static class OutputBufferFlushSendHandler implements SendHandler {
        private final ByteBuffer outputBuffer;
        private final SendHandler handler;
        public OutputBufferFlushSendHandler ( final ByteBuffer outputBuffer, final SendHandler handler ) {
            this.outputBuffer = outputBuffer;
            this.handler = handler;
        }
        public void onResult ( final SendResult result ) {
            if ( result.isOK() ) {
                this.outputBuffer.clear();
            }
            this.handler.onResult ( result );
        }
    }
    private static class WsOutputStream extends OutputStream {
        private final WsRemoteEndpointImplBase endpoint;
        private final ByteBuffer buffer;
        private final Object closeLock;
        private volatile boolean closed;
        private volatile boolean used;
        public WsOutputStream ( final WsRemoteEndpointImplBase endpoint ) {
            this.buffer = ByteBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
            this.closeLock = new Object();
            this.closed = false;
            this.used = false;
            this.endpoint = endpoint;
        }
        @Override
        public void write ( final int b ) throws IOException {
            if ( this.closed ) {
                throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closedOutputStream" ) );
            }
            this.used = true;
            if ( this.buffer.remaining() == 0 ) {
                this.flush();
            }
            this.buffer.put ( ( byte ) b );
        }
        @Override
        public void write ( final byte[] b, final int off, final int len ) throws IOException {
            if ( this.closed ) {
                throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closedOutputStream" ) );
            }
            if ( len == 0 ) {
                return;
            }
            if ( off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0 ) {
                throw new IndexOutOfBoundsException();
            }
            this.used = true;
            if ( this.buffer.remaining() == 0 ) {
                this.flush();
            }
            int remaining;
            int written;
            for ( remaining = this.buffer.remaining(), written = 0; remaining < len - written; remaining = this.buffer.remaining() ) {
                this.buffer.put ( b, off + written, remaining );
                written += remaining;
                this.flush();
            }
            this.buffer.put ( b, off + written, len - written );
        }
        @Override
        public void flush() throws IOException {
            if ( this.closed ) {
                throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closedOutputStream" ) );
            }
            if ( !Constants.STREAMS_DROP_EMPTY_MESSAGES || this.buffer.position() > 0 ) {
                this.doWrite ( false );
            }
        }
        @Override
        public void close() throws IOException {
            synchronized ( this.closeLock ) {
                if ( this.closed ) {
                    return;
                }
                this.closed = true;
            }
            this.doWrite ( true );
        }
        private void doWrite ( final boolean last ) throws IOException {
            if ( !Constants.STREAMS_DROP_EMPTY_MESSAGES || this.used ) {
                this.buffer.flip();
                this.endpoint.sendMessageBlock ( ( byte ) 2, this.buffer, last );
            }
            this.endpoint.stateMachine.complete ( last );
            this.buffer.clear();
        }
    }
    private static class WsWriter extends Writer {
        private final WsRemoteEndpointImplBase endpoint;
        private final CharBuffer buffer;
        private final Object closeLock;
        private volatile boolean closed;
        private volatile boolean used;
        public WsWriter ( final WsRemoteEndpointImplBase endpoint ) {
            this.buffer = CharBuffer.allocate ( Constants.DEFAULT_BUFFER_SIZE );
            this.closeLock = new Object();
            this.closed = false;
            this.used = false;
            this.endpoint = endpoint;
        }
        @Override
        public void write ( final char[] cbuf, final int off, final int len ) throws IOException {
            if ( this.closed ) {
                throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closedWriter" ) );
            }
            if ( len == 0 ) {
                return;
            }
            if ( off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0 ) {
                throw new IndexOutOfBoundsException();
            }
            this.used = true;
            if ( this.buffer.remaining() == 0 ) {
                this.flush();
            }
            int remaining;
            int written;
            for ( remaining = this.buffer.remaining(), written = 0; remaining < len - written; remaining = this.buffer.remaining() ) {
                this.buffer.put ( cbuf, off + written, remaining );
                written += remaining;
                this.flush();
            }
            this.buffer.put ( cbuf, off + written, len - written );
        }
        @Override
        public void flush() throws IOException {
            if ( this.closed ) {
                throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.closedWriter" ) );
            }
            if ( !Constants.STREAMS_DROP_EMPTY_MESSAGES || this.buffer.position() > 0 ) {
                this.doWrite ( false );
            }
        }
        @Override
        public void close() throws IOException {
            synchronized ( this.closeLock ) {
                if ( this.closed ) {
                    return;
                }
                this.closed = true;
            }
            this.doWrite ( true );
        }
        private void doWrite ( final boolean last ) throws IOException {
            if ( !Constants.STREAMS_DROP_EMPTY_MESSAGES || this.used ) {
                this.buffer.flip();
                this.endpoint.sendMessageBlock ( this.buffer, last );
                this.buffer.clear();
            } else {
                this.endpoint.stateMachine.complete ( last );
            }
        }
    }
    private static class EncoderEntry {
        private final Class<?> clazz;
        private final Encoder encoder;
        public EncoderEntry ( final Class<?> clazz, final Encoder encoder ) {
            this.clazz = clazz;
            this.encoder = encoder;
        }
        public Class<?> getClazz() {
            return this.clazz;
        }
        public Encoder getEncoder() {
            return this.encoder;
        }
    }
    private enum State {
        OPEN,
        STREAM_WRITING,
        WRITER_WRITING,
        BINARY_PARTIAL_WRITING,
        BINARY_PARTIAL_READY,
        BINARY_FULL_WRITING,
        TEXT_PARTIAL_WRITING,
        TEXT_PARTIAL_READY,
        TEXT_FULL_WRITING;
    }
    private static class StateMachine {
        private State state;
        private StateMachine() {
            this.state = State.OPEN;
        }
        public synchronized void streamStart() {
            this.checkState ( State.OPEN );
            this.state = State.STREAM_WRITING;
        }
        public synchronized void writeStart() {
            this.checkState ( State.OPEN );
            this.state = State.WRITER_WRITING;
        }
        public synchronized void binaryPartialStart() {
            this.checkState ( State.OPEN, State.BINARY_PARTIAL_READY );
            this.state = State.BINARY_PARTIAL_WRITING;
        }
        public synchronized void binaryStart() {
            this.checkState ( State.OPEN );
            this.state = State.BINARY_FULL_WRITING;
        }
        public synchronized void textPartialStart() {
            this.checkState ( State.OPEN, State.TEXT_PARTIAL_READY );
            this.state = State.TEXT_PARTIAL_WRITING;
        }
        public synchronized void textStart() {
            this.checkState ( State.OPEN );
            this.state = State.TEXT_FULL_WRITING;
        }
        public synchronized void complete ( final boolean last ) {
            if ( last ) {
                this.checkState ( State.TEXT_PARTIAL_WRITING, State.TEXT_FULL_WRITING, State.BINARY_PARTIAL_WRITING, State.BINARY_FULL_WRITING, State.STREAM_WRITING, State.WRITER_WRITING );
                this.state = State.OPEN;
            } else {
                this.checkState ( State.TEXT_PARTIAL_WRITING, State.BINARY_PARTIAL_WRITING, State.STREAM_WRITING, State.WRITER_WRITING );
                if ( this.state == State.TEXT_PARTIAL_WRITING ) {
                    this.state = State.TEXT_PARTIAL_READY;
                } else if ( this.state == State.BINARY_PARTIAL_WRITING ) {
                    this.state = State.BINARY_PARTIAL_READY;
                } else if ( this.state != State.WRITER_WRITING ) {
                    if ( this.state != State.STREAM_WRITING ) {
                        throw new IllegalStateException ( "BUG: This code should never be called" );
                    }
                }
            }
        }
        private void checkState ( final State... required ) {
            for ( final State state : required ) {
                if ( this.state == state ) {
                    return;
                }
            }
            throw new IllegalStateException ( WsRemoteEndpointImplBase.sm.getString ( "wsRemoteEndpoint.wrongState", this.state ) );
        }
    }
    private static class StateUpdateSendHandler implements SendHandler {
        private final SendHandler handler;
        private final StateMachine stateMachine;
        public StateUpdateSendHandler ( final SendHandler handler, final StateMachine stateMachine ) {
            this.handler = handler;
            this.stateMachine = stateMachine;
        }
        public void onResult ( final SendResult result ) {
            if ( result.isOK() ) {
                this.stateMachine.complete ( true );
            }
            this.handler.onResult ( result );
        }
    }
    private static class BlockingSendHandler implements SendHandler {
        private SendResult sendResult;
        private BlockingSendHandler() {
            this.sendResult = null;
        }
        public void onResult ( final SendResult result ) {
            this.sendResult = result;
        }
        public SendResult getSendResult() {
            return this.sendResult;
        }
    }
}

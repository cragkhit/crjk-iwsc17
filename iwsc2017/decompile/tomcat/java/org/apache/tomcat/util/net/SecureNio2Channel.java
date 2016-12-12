package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import java.nio.channels.WritePendingException;
import java.io.EOFException;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import java.util.List;
import java.util.Collections;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class SecureNio2Channel extends Nio2Channel {
    private static final Log log;
    private static final StringManager sm;
    private static final int DEFAULT_NET_BUFFER_SIZE = 16921;
    protected ByteBuffer netInBuffer;
    protected ByteBuffer netOutBuffer;
    protected SSLEngine sslEngine;
    protected final Nio2Endpoint endpoint;
    protected boolean sniComplete;
    private volatile boolean handshakeComplete;
    private volatile SSLEngineResult.HandshakeStatus handshakeStatus;
    private volatile boolean unwrapBeforeRead;
    protected boolean closed;
    protected boolean closing;
    private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> handshakeReadCompletionHandler;
    private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> handshakeWriteCompletionHandler;
    public SecureNio2Channel ( final SocketBufferHandler bufHandler, final Nio2Endpoint endpoint ) {
        super ( bufHandler );
        this.sniComplete = false;
        this.unwrapBeforeRead = false;
        this.endpoint = endpoint;
        if ( endpoint.getSocketProperties().getDirectSslBuffer() ) {
            this.netInBuffer = ByteBuffer.allocateDirect ( 16921 );
            this.netOutBuffer = ByteBuffer.allocateDirect ( 16921 );
        } else {
            this.netInBuffer = ByteBuffer.allocate ( 16921 );
            this.netOutBuffer = ByteBuffer.allocate ( 16921 );
        }
        this.handshakeReadCompletionHandler = new HandshakeReadCompletionHandler();
        this.handshakeWriteCompletionHandler = new HandshakeWriteCompletionHandler();
    }
    @Override
    public void reset ( final AsynchronousSocketChannel channel, final SocketWrapperBase<Nio2Channel> socket ) throws IOException {
        super.reset ( channel, socket );
        this.sslEngine = null;
        this.sniComplete = false;
        this.handshakeComplete = false;
        this.closed = false;
        this.closing = false;
        this.netInBuffer.clear();
    }
    @Override
    public void free() {
        super.free();
        if ( this.endpoint.getSocketProperties().getDirectSslBuffer() ) {
            ByteBufferUtils.cleanDirectBuffer ( this.netInBuffer );
            ByteBufferUtils.cleanDirectBuffer ( this.netOutBuffer );
        }
    }
    @Override
    public Future<Boolean> flush() {
        return new FutureFlush();
    }
    @Override
    public int handshake() throws IOException {
        return this.handshakeInternal ( true );
    }
    protected int handshakeInternal ( final boolean async ) throws IOException {
        if ( this.handshakeComplete ) {
            return 0;
        }
        if ( !this.sniComplete ) {
            final int sniResult = this.processSNI();
            if ( sniResult != 0 ) {
                return sniResult;
            }
            this.sniComplete = true;
        }
        SSLEngineResult handshake = null;
        while ( !this.handshakeComplete ) {
            Label_0455: {
                switch ( this.handshakeStatus ) {
                case NOT_HANDSHAKING: {
                    throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.notHandshaking" ) );
                }
                case FINISHED: {
                    if ( this.endpoint.hasNegotiableProtocols() && this.sslEngine instanceof SSLUtil.ProtocolInfo ) {
                        this.socket.setNegotiatedProtocol ( ( ( SSLUtil.ProtocolInfo ) this.sslEngine ).getNegotiatedProtocol() );
                    }
                    this.handshakeComplete = !this.netOutBuffer.hasRemaining();
                    if ( this.handshakeComplete ) {
                        return 0;
                    }
                    if ( async ) {
                        this.sc.write ( this.netOutBuffer, this.socket, this.handshakeWriteCompletionHandler );
                    } else {
                        try {
                            this.sc.write ( this.netOutBuffer ).get ( this.endpoint.getConnectionTimeout(), TimeUnit.MILLISECONDS );
                        } catch ( InterruptedException | ExecutionException | TimeoutException e2 ) {
                            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.handhakeError" ) );
                        }
                    }
                    return 1;
                }
                case NEED_WRAP: {
                    try {
                        handshake = this.handshakeWrap();
                    } catch ( SSLException e ) {
                        if ( SecureNio2Channel.log.isDebugEnabled() ) {
                            SecureNio2Channel.log.debug ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.wrapException" ), e );
                        }
                        handshake = this.handshakeWrap();
                    }
                    if ( handshake.getStatus() == SSLEngineResult.Status.OK ) {
                        if ( this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            this.handshakeStatus = this.tasks();
                        }
                        if ( this.handshakeStatus != SSLEngineResult.HandshakeStatus.NEED_UNWRAP || this.netOutBuffer.remaining() > 0 ) {
                            if ( async ) {
                                this.sc.write ( this.netOutBuffer, this.socket, this.handshakeWriteCompletionHandler );
                            } else {
                                try {
                                    this.sc.write ( this.netOutBuffer ).get ( this.endpoint.getConnectionTimeout(), TimeUnit.MILLISECONDS );
                                } catch ( InterruptedException | ExecutionException | TimeoutException e2 ) {
                                    throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.handhakeError" ) );
                                }
                            }
                            return 1;
                        }
                        break Label_0455;
                    } else {
                        if ( handshake.getStatus() == SSLEngineResult.Status.CLOSED ) {
                            return -1;
                        }
                        throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unexpectedStatusDuringWrap", handshake.getStatus() ) );
                    }
                    break;
                }
                case NEED_UNWRAP: {
                    handshake = this.handshakeUnwrap();
                    if ( handshake.getStatus() == SSLEngineResult.Status.OK ) {
                        if ( this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            this.handshakeStatus = this.tasks();
                            continue;
                        }
                        continue;
                    } else {
                        if ( handshake.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                            if ( async ) {
                                this.sc.read ( this.netInBuffer, this.socket, this.handshakeReadCompletionHandler );
                            } else {
                                try {
                                    this.sc.read ( this.netInBuffer ).get ( this.endpoint.getConnectionTimeout(), TimeUnit.MILLISECONDS );
                                } catch ( InterruptedException | ExecutionException | TimeoutException e2 ) {
                                    throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.handhakeError" ) );
                                }
                            }
                            return 1;
                        }
                        throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unexpectedStatusDuringUnwrap", this.handshakeStatus ) );
                    }
                    break;
                }
                case NEED_TASK: {
                    this.handshakeStatus = this.tasks();
                    continue;
                }
                default: {
                    throw new IllegalStateException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.invalidStatus", this.handshakeStatus ) );
                }
                }
            }
        }
        return this.handshakeComplete ? 0 : this.handshakeInternal ( async );
    }
    private int processSNI() throws IOException {
        if ( this.netInBuffer.position() == 0 ) {
            this.sc.read ( this.netInBuffer, this.socket, this.handshakeReadCompletionHandler );
            return 1;
        }
        TLSClientHelloExtractor extractor;
        for ( extractor = new TLSClientHelloExtractor ( this.netInBuffer ); extractor.getResult() == TLSClientHelloExtractor.ExtractorResult.UNDERFLOW && this.netInBuffer.capacity() < this.endpoint.getSniParseLimit(); extractor = new TLSClientHelloExtractor ( this.netInBuffer ) ) {
            final int newLimit = Math.min ( this.netInBuffer.capacity() * 2, this.endpoint.getSniParseLimit() );
            SecureNio2Channel.log.info ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.expandNetInBuffer", Integer.toString ( newLimit ) ) );
            this.netInBuffer = ByteBufferUtils.expand ( this.netInBuffer, newLimit );
            this.sc.read ( this.netInBuffer );
        }
        String hostName = null;
        List<Cipher> clientRequestedCiphers = null;
        switch ( extractor.getResult() ) {
        case COMPLETE: {
            hostName = extractor.getSNIValue();
        }
        case NOT_PRESENT: {
            clientRequestedCiphers = extractor.getClientRequestedCiphers();
            break;
        }
        case NEED_READ: {
            this.sc.read ( this.netInBuffer, this.socket, this.handshakeReadCompletionHandler );
            return 1;
        }
        case UNDERFLOW: {
            if ( SecureNio2Channel.log.isDebugEnabled() ) {
                SecureNio2Channel.log.debug ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.sniDefault" ) );
            }
            hostName = this.endpoint.getDefaultSSLHostConfigName();
            clientRequestedCiphers = Collections.emptyList();
            break;
        }
        }
        if ( SecureNio2Channel.log.isDebugEnabled() ) {
            SecureNio2Channel.log.debug ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.sniHostName", hostName ) );
        }
        this.sslEngine = this.endpoint.createSSLEngine ( hostName, clientRequestedCiphers );
        this.getBufHandler().expand ( this.sslEngine.getSession().getApplicationBufferSize() );
        if ( this.netOutBuffer.capacity() < this.sslEngine.getSession().getApplicationBufferSize() ) {
            SecureNio2Channel.log.info ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.expandNetOutBuffer", Integer.toString ( this.sslEngine.getSession().getApplicationBufferSize() ) ) );
        }
        this.netInBuffer = ByteBufferUtils.expand ( this.netInBuffer, this.sslEngine.getSession().getPacketBufferSize() );
        ( this.netOutBuffer = ByteBufferUtils.expand ( this.netOutBuffer, this.sslEngine.getSession().getPacketBufferSize() ) ).position ( 0 );
        this.netOutBuffer.limit ( 0 );
        this.sslEngine.beginHandshake();
        this.handshakeStatus = this.sslEngine.getHandshakeStatus();
        return 0;
    }
    public void rehandshake() throws IOException {
        if ( this.netInBuffer.position() > 0 && this.netInBuffer.position() < this.netInBuffer.limit() ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.netInputNotEmpty" ) );
        }
        if ( this.netOutBuffer.position() > 0 && this.netOutBuffer.position() < this.netOutBuffer.limit() ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.netOutputNotEmpty" ) );
        }
        if ( !this.getBufHandler().isReadBufferEmpty() ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.appInputNotEmpty" ) );
        }
        if ( !this.getBufHandler().isWriteBufferEmpty() ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.appOutputNotEmpty" ) );
        }
        this.netOutBuffer.position ( 0 );
        this.netOutBuffer.limit ( 0 );
        this.netInBuffer.position ( 0 );
        this.netInBuffer.limit ( 0 );
        this.getBufHandler().reset();
        this.handshakeComplete = false;
        this.sslEngine.beginHandshake();
        this.handshakeStatus = this.sslEngine.getHandshakeStatus();
        boolean handshaking = true;
        try {
            while ( handshaking ) {
                final int hsStatus = this.handshakeInternal ( false );
                switch ( hsStatus ) {
                case -1: {
                    throw new EOFException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.eofDuringHandshake" ) );
                }
                case 0: {
                    handshaking = false;
                    continue;
                }
                }
            }
        } catch ( IOException x ) {
            throw x;
        } catch ( Exception cx ) {
            final IOException x2 = new IOException ( cx );
            throw x2;
        }
    }
    protected SSLEngineResult.HandshakeStatus tasks() {
        Runnable r = null;
        while ( ( r = this.sslEngine.getDelegatedTask() ) != null ) {
            r.run();
        }
        return this.sslEngine.getHandshakeStatus();
    }
    protected SSLEngineResult handshakeWrap() throws IOException {
        this.netOutBuffer.clear();
        this.getBufHandler().configureWriteBufferForRead();
        final SSLEngineResult result = this.sslEngine.wrap ( this.getBufHandler().getWriteBuffer(), this.netOutBuffer );
        this.netOutBuffer.flip();
        this.handshakeStatus = result.getHandshakeStatus();
        return result;
    }
    protected SSLEngineResult handshakeUnwrap() throws IOException {
        if ( this.netInBuffer.position() == this.netInBuffer.limit() ) {
            this.netInBuffer.clear();
        }
        boolean cont = false;
        SSLEngineResult result;
        do {
            this.netInBuffer.flip();
            this.getBufHandler().configureReadBufferForWrite();
            result = this.sslEngine.unwrap ( this.netInBuffer, this.getBufHandler().getReadBuffer() );
            this.netInBuffer.compact();
            this.handshakeStatus = result.getHandshakeStatus();
            if ( result.getStatus() == SSLEngineResult.Status.OK && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                this.handshakeStatus = this.tasks();
            }
            cont = ( result.getStatus() == SSLEngineResult.Status.OK && this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP );
        } while ( cont );
        return result;
    }
    @Override
    public void close() throws IOException {
        if ( this.closing ) {
            return;
        }
        this.closing = true;
        this.sslEngine.closeOutbound();
        try {
            if ( !this.flush().get ( this.endpoint.getConnectionTimeout(), TimeUnit.MILLISECONDS ) ) {
                throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ) );
            }
        } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ), e );
        } catch ( WritePendingException e2 ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.pendingWriteDuringClose" ), e2 );
        }
        this.netOutBuffer.clear();
        final SSLEngineResult handshake = this.sslEngine.wrap ( this.getEmptyBuf(), this.netOutBuffer );
        if ( handshake.getStatus() != SSLEngineResult.Status.CLOSED ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.invalidCloseState" ) );
        }
        this.netOutBuffer.flip();
        try {
            if ( !this.flush().get ( this.endpoint.getConnectionTimeout(), TimeUnit.MILLISECONDS ) ) {
                throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ) );
            }
        } catch ( InterruptedException | ExecutionException | TimeoutException e3 ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ), e3 );
        } catch ( WritePendingException e4 ) {
            throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.pendingWriteDuringClose" ), e4 );
        }
        this.closed = ( !this.netOutBuffer.hasRemaining() && handshake.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_WRAP );
    }
    @Override
    public void close ( final boolean force ) throws IOException {
        try {
            this.close();
        } finally {
            if ( force || this.closed ) {
                this.closed = true;
                this.sc.close();
            }
        }
    }
    @Override
    public Future<Integer> read ( final ByteBuffer dst ) {
        if ( !this.handshakeComplete ) {
            throw new IllegalStateException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        return new FutureRead ( dst );
    }
    @Override
    public Future<Integer> write ( final ByteBuffer src ) {
        return new FutureWrite ( src );
    }
    @Override
    public <A> void read ( final ByteBuffer dst, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        if ( this.closing || this.closed ) {
            handler.completed ( -1, ( Object ) attachment );
            return;
        }
        if ( !this.handshakeComplete ) {
            throw new IllegalStateException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        final CompletionHandler<Integer, A> readCompletionHandler = new CompletionHandler<Integer, A>() {
            @Override
            public void completed ( final Integer nBytes, final A attach ) {
                if ( nBytes < 0 ) {
                    this.failed ( new EOFException(), attach );
                } else {
                    try {
                        ByteBuffer dst2 = dst;
                        int read = 0;
                        do {
                            SecureNio2Channel.this.netInBuffer.flip();
                            final SSLEngineResult unwrap = SecureNio2Channel.this.sslEngine.unwrap ( SecureNio2Channel.this.netInBuffer, dst2 );
                            SecureNio2Channel.this.netInBuffer.compact();
                            if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                                read += unwrap.bytesProduced();
                                if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                                    SecureNio2Channel.this.tasks();
                                }
                                if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                                    continue;
                                }
                                if ( read == 0 ) {
                                    SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer, timeout, unit, attachment, this );
                                    return;
                                }
                                break;
                            } else {
                                if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                                    throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                                }
                                if ( read > 0 ) {
                                    break;
                                }
                                if ( dst2 != SecureNio2Channel.this.getBufHandler().getReadBuffer() ) {
                                    throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) );
                                }
                                SecureNio2Channel.this.getBufHandler().expand ( SecureNio2Channel.this.sslEngine.getSession().getApplicationBufferSize() );
                                dst2 = SecureNio2Channel.this.getBufHandler().getReadBuffer();
                            }
                        } while ( SecureNio2Channel.this.netInBuffer.position() != 0 );
                        if ( !dst2.hasRemaining() ) {
                            SecureNio2Channel.this.unwrapBeforeRead = true;
                        } else {
                            SecureNio2Channel.this.unwrapBeforeRead = false;
                        }
                        handler.completed ( read, attach );
                    } catch ( Exception e ) {
                        this.failed ( e, attach );
                    }
                }
            }
            @Override
            public void failed ( final Throwable exc, final A attach ) {
                handler.failed ( exc, attach );
            }
        };
        if ( this.unwrapBeforeRead || this.netInBuffer.position() > 0 ) {
            readCompletionHandler.completed ( this.netInBuffer.position(), attachment );
        } else {
            this.sc.read ( this.netInBuffer, timeout, unit, attachment, readCompletionHandler );
        }
    }
    @Override
    public <A> void read ( final ByteBuffer[] dsts, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Long, ? super A> handler ) {
        if ( offset < 0 || dsts == null || offset + length > dsts.length ) {
            throw new IllegalArgumentException();
        }
        if ( this.closing || this.closed ) {
            handler.completed ( -1L, ( Object ) attachment );
            return;
        }
        if ( !this.handshakeComplete ) {
            throw new IllegalStateException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        final CompletionHandler<Integer, A> readCompletionHandler = new CompletionHandler<Integer, A>() {
            @Override
            public void completed ( final Integer nBytes, final A attach ) {
                if ( nBytes < 0 ) {
                    this.failed ( new EOFException(), attach );
                } else {
                    try {
                        long read = 0L;
                        do {
                            SecureNio2Channel.this.netInBuffer.flip();
                            final SSLEngineResult unwrap = SecureNio2Channel.this.sslEngine.unwrap ( SecureNio2Channel.this.netInBuffer, dsts, offset, length );
                            SecureNio2Channel.this.netInBuffer.compact();
                            if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                                read += unwrap.bytesProduced();
                                if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                                    SecureNio2Channel.this.tasks();
                                }
                                if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                                    continue;
                                }
                                if ( read == 0L ) {
                                    SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer, timeout, unit, attachment, this );
                                    return;
                                }
                                break;
                            } else {
                                if ( unwrap.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW && read > 0L ) {
                                    break;
                                }
                                throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                            }
                        } while ( SecureNio2Channel.this.netInBuffer.position() != 0 );
                        int capacity = 0;
                        for ( int endOffset = offset + length, i = offset; i < endOffset; ++i ) {
                            capacity += dsts[i].remaining();
                        }
                        if ( capacity == 0 ) {
                            SecureNio2Channel.this.unwrapBeforeRead = true;
                        } else {
                            SecureNio2Channel.this.unwrapBeforeRead = false;
                        }
                        handler.completed ( read, attach );
                    } catch ( Exception e ) {
                        this.failed ( e, attach );
                    }
                }
            }
            @Override
            public void failed ( final Throwable exc, final A attach ) {
                handler.failed ( exc, attach );
            }
        };
        if ( this.unwrapBeforeRead || this.netInBuffer.position() > 0 ) {
            readCompletionHandler.completed ( this.netInBuffer.position(), attachment );
        } else {
            this.sc.read ( this.netInBuffer, timeout, unit, attachment, readCompletionHandler );
        }
    }
    @Override
    public <A> void write ( final ByteBuffer src, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        if ( this.closing || this.closed ) {
            handler.failed ( ( Throwable ) new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.closing" ) ), ( Object ) attachment );
            return;
        }
        try {
            this.netOutBuffer.clear();
            final SSLEngineResult result = this.sslEngine.wrap ( src, this.netOutBuffer );
            final int written = result.bytesConsumed();
            this.netOutBuffer.flip();
            if ( result.getStatus() != SSLEngineResult.Status.OK ) {
                throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
            }
            if ( result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                this.tasks();
            }
            this.sc.write ( this.netOutBuffer, timeout, unit, attachment, new CompletionHandler<Integer, A>() {
                @Override
                public void completed ( final Integer nBytes, final A attach ) {
                    if ( nBytes < 0 ) {
                        this.failed ( new EOFException(), attach );
                    } else if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
                        SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer, timeout, unit, attachment, this );
                    } else if ( written == 0 ) {
                        SecureNio2Channel.this.write ( src, timeout, unit, attachment, handler );
                    } else {
                        handler.completed ( written, attach );
                    }
                }
                @Override
                public void failed ( final Throwable exc, final A attach ) {
                    handler.failed ( exc, attach );
                }
            } );
        } catch ( Exception e ) {
            handler.failed ( ( Throwable ) e, ( Object ) attachment );
        }
    }
    @Override
    public <A> void write ( final ByteBuffer[] srcs, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Long, ? super A> handler ) {
        if ( offset < 0 || length < 0 || offset > srcs.length - length ) {
            throw new IndexOutOfBoundsException();
        }
        if ( this.closing || this.closed ) {
            handler.failed ( ( Throwable ) new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.closing" ) ), ( Object ) attachment );
            return;
        }
        try {
            this.netOutBuffer.clear();
            final SSLEngineResult result = this.sslEngine.wrap ( srcs, offset, length, this.netOutBuffer );
            final int written = result.bytesConsumed();
            this.netOutBuffer.flip();
            if ( result.getStatus() != SSLEngineResult.Status.OK ) {
                throw new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
            }
            if ( result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                this.tasks();
            }
            this.sc.write ( this.netOutBuffer, timeout, unit, attachment, new CompletionHandler<Integer, A>() {
                @Override
                public void completed ( final Integer nBytes, final A attach ) {
                    if ( nBytes < 0 ) {
                        this.failed ( new EOFException(), attach );
                    } else if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
                        SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer, timeout, unit, attachment, this );
                    } else if ( written == 0 ) {
                        SecureNio2Channel.this.write ( srcs, offset, length, timeout, unit, attachment, handler );
                    } else {
                        handler.completed ( ( long ) written, attach );
                    }
                }
                @Override
                public void failed ( final Throwable exc, final A attach ) {
                    handler.failed ( exc, attach );
                }
            } );
        } catch ( Exception e ) {
            handler.failed ( ( Throwable ) e, ( Object ) attachment );
        }
    }
    @Override
    public boolean isHandshakeComplete() {
        return this.handshakeComplete;
    }
    @Override
    public boolean isClosing() {
        return this.closing;
    }
    public SSLEngine getSslEngine() {
        return this.sslEngine;
    }
    public ByteBuffer getEmptyBuf() {
        return SecureNio2Channel.emptyBuf;
    }
    @Override
    public AsynchronousSocketChannel getIOChannel() {
        return this.sc;
    }
    static {
        log = LogFactory.getLog ( SecureNio2Channel.class );
        sm = StringManager.getManager ( SecureNio2Channel.class );
    }
    private class HandshakeReadCompletionHandler implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
        @Override
        public void completed ( final Integer result, final SocketWrapperBase<Nio2Channel> attachment ) {
            if ( result < 0 ) {
                this.failed ( ( Throwable ) new EOFException(), attachment );
            } else {
                SecureNio2Channel.this.endpoint.processSocket ( attachment, SocketEvent.OPEN_READ, false );
            }
        }
        @Override
        public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
            SecureNio2Channel.this.endpoint.processSocket ( attachment, SocketEvent.ERROR, false );
        }
    }
    private class HandshakeWriteCompletionHandler implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
        @Override
        public void completed ( final Integer result, final SocketWrapperBase<Nio2Channel> attachment ) {
            if ( result < 0 ) {
                this.failed ( ( Throwable ) new EOFException(), attachment );
            } else {
                SecureNio2Channel.this.endpoint.processSocket ( attachment, SocketEvent.OPEN_WRITE, false );
            }
        }
        @Override
        public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
            SecureNio2Channel.this.endpoint.processSocket ( attachment, SocketEvent.ERROR, false );
        }
    }
    private class FutureFlush implements Future<Boolean> {
        private Future<Integer> integer;
        protected FutureFlush() {
            this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
        }
        @Override
        public boolean cancel ( final boolean mayInterruptIfRunning ) {
            return this.integer.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return this.integer.isCancelled();
        }
        @Override
        public boolean isDone() {
            return this.integer.isDone();
        }
        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return this.integer.get() >= 0;
        }
        @Override
        public Boolean get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            return this.integer.get ( timeout, unit ) >= 0;
        }
    }
    private class FutureRead implements Future<Integer> {
        private ByteBuffer dst;
        private Future<Integer> integer;
        private FutureRead ( final ByteBuffer dst ) {
            this.dst = dst;
            if ( SecureNio2Channel.this.unwrapBeforeRead || SecureNio2Channel.this.netInBuffer.position() > 0 ) {
                this.integer = null;
            } else {
                this.integer = SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer );
            }
        }
        @Override
        public boolean cancel ( final boolean mayInterruptIfRunning ) {
            return this.integer != null && this.integer.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return this.integer != null && this.integer.isCancelled();
        }
        @Override
        public boolean isDone() {
            return this.integer == null || this.integer.isDone();
        }
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            try {
                return ( this.integer == null ) ? this.unwrap ( SecureNio2Channel.this.netInBuffer.position(), -1L, TimeUnit.MILLISECONDS ) : this.unwrap ( this.integer.get(), -1L, TimeUnit.MILLISECONDS );
            } catch ( TimeoutException e ) {
                throw new ExecutionException ( e );
            }
        }
        @Override
        public Integer get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            return ( this.integer == null ) ? this.unwrap ( SecureNio2Channel.this.netInBuffer.position(), timeout, unit ) : this.unwrap ( this.integer.get ( timeout, unit ), timeout, unit );
        }
        private Integer unwrap ( final int nRead, final long timeout, final TimeUnit unit ) throws ExecutionException, TimeoutException, InterruptedException {
            if ( SecureNio2Channel.this.closing || SecureNio2Channel.this.closed ) {
                return -1;
            }
            if ( nRead < 0 ) {
                return -1;
            }
            int read = 0;
            do {
                SecureNio2Channel.this.netInBuffer.flip();
                SSLEngineResult unwrap;
                try {
                    unwrap = SecureNio2Channel.this.sslEngine.unwrap ( SecureNio2Channel.this.netInBuffer, this.dst );
                } catch ( SSLException e ) {
                    throw new ExecutionException ( e );
                }
                SecureNio2Channel.this.netInBuffer.compact();
                if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                    read += unwrap.bytesProduced();
                    if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                        SecureNio2Channel.this.tasks();
                    }
                    if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                        continue;
                    }
                    if ( read != 0 ) {
                        break;
                    }
                    this.integer = SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer );
                    if ( timeout > 0L ) {
                        return this.unwrap ( this.integer.get ( timeout, unit ), timeout, unit );
                    }
                    return this.unwrap ( this.integer.get(), -1L, TimeUnit.MILLISECONDS );
                } else {
                    if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                        throw new ExecutionException ( new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) ) );
                    }
                    if ( read > 0 ) {
                        break;
                    }
                    if ( this.dst == SecureNio2Channel.this.getBufHandler().getReadBuffer() ) {
                        SecureNio2Channel.this.getBufHandler().expand ( SecureNio2Channel.this.sslEngine.getSession().getApplicationBufferSize() );
                        this.dst = SecureNio2Channel.this.getBufHandler().getReadBuffer();
                    } else {
                        if ( this.dst != SecureNio2Channel.this.getAppReadBufHandler().getByteBuffer() ) {
                            throw new ExecutionException ( new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) ) );
                        }
                        SecureNio2Channel.this.getAppReadBufHandler().expand ( SecureNio2Channel.this.sslEngine.getSession().getApplicationBufferSize() );
                        this.dst = SecureNio2Channel.this.getAppReadBufHandler().getByteBuffer();
                    }
                }
            } while ( SecureNio2Channel.this.netInBuffer.position() != 0 );
            if ( !this.dst.hasRemaining() ) {
                SecureNio2Channel.this.unwrapBeforeRead = true;
            } else {
                SecureNio2Channel.this.unwrapBeforeRead = false;
            }
            return read;
        }
    }
    private class FutureWrite implements Future<Integer> {
        private final ByteBuffer src;
        private Future<Integer> integer;
        private int written;
        private Throwable t;
        private FutureWrite ( final ByteBuffer src ) {
            this.integer = null;
            this.written = 0;
            this.t = null;
            this.src = src;
            if ( SecureNio2Channel.this.closing || SecureNio2Channel.this.closed ) {
                this.t = new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.closing" ) );
            } else {
                this.wrap();
            }
        }
        @Override
        public boolean cancel ( final boolean mayInterruptIfRunning ) {
            return this.integer.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return this.integer.isCancelled();
        }
        @Override
        public boolean isDone() {
            return this.integer.isDone();
        }
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            if ( this.t != null ) {
                throw new ExecutionException ( this.t );
            }
            if ( this.integer.get() > 0 && this.written == 0 ) {
                this.wrap();
                return this.get();
            }
            if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
                this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
                return this.get();
            }
            return this.written;
        }
        @Override
        public Integer get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            if ( this.t != null ) {
                throw new ExecutionException ( this.t );
            }
            if ( this.integer.get ( timeout, unit ) > 0 && this.written == 0 ) {
                this.wrap();
                return this.get ( timeout, unit );
            }
            if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
                this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
                return this.get ( timeout, unit );
            }
            return this.written;
        }
        protected void wrap() {
            try {
                if ( !SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
                    SecureNio2Channel.this.netOutBuffer.clear();
                    final SSLEngineResult result = SecureNio2Channel.this.sslEngine.wrap ( this.src, SecureNio2Channel.this.netOutBuffer );
                    this.written = result.bytesConsumed();
                    SecureNio2Channel.this.netOutBuffer.flip();
                    if ( result.getStatus() == SSLEngineResult.Status.OK ) {
                        if ( result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            SecureNio2Channel.this.tasks();
                        }
                    } else {
                        this.t = new IOException ( SecureNio2Channel.sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
                    }
                }
                this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
            } catch ( SSLException e ) {
                this.t = e;
            }
        }
    }
}

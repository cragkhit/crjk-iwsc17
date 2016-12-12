package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritePendingException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import org.apache.tomcat.util.net.TLSClientHelloExtractor.ExtractorResult;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import org.apache.tomcat.util.res.StringManager;
public class SecureNio2Channel extends Nio2Channel  {
    private static final Log log = LogFactory.getLog ( SecureNio2Channel.class );
    private static final StringManager sm = StringManager.getManager ( SecureNio2Channel.class );
    private static final int DEFAULT_NET_BUFFER_SIZE = 16921;
    protected ByteBuffer netInBuffer;
    protected ByteBuffer netOutBuffer;
    protected SSLEngine sslEngine;
    protected final Nio2Endpoint endpoint;
    protected boolean sniComplete = false;
    private volatile boolean handshakeComplete;
    private volatile HandshakeStatus handshakeStatus;
    private volatile boolean unwrapBeforeRead = false;
    protected boolean closed;
    protected boolean closing;
    private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> handshakeReadCompletionHandler;
    private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> handshakeWriteCompletionHandler;
    public SecureNio2Channel ( SocketBufferHandler bufHandler, Nio2Endpoint endpoint ) {
        super ( bufHandler );
        this.endpoint = endpoint;
        if ( endpoint.getSocketProperties().getDirectSslBuffer() ) {
            netInBuffer = ByteBuffer.allocateDirect ( DEFAULT_NET_BUFFER_SIZE );
            netOutBuffer = ByteBuffer.allocateDirect ( DEFAULT_NET_BUFFER_SIZE );
        } else {
            netInBuffer = ByteBuffer.allocate ( DEFAULT_NET_BUFFER_SIZE );
            netOutBuffer = ByteBuffer.allocate ( DEFAULT_NET_BUFFER_SIZE );
        }
        handshakeReadCompletionHandler = new HandshakeReadCompletionHandler();
        handshakeWriteCompletionHandler = new HandshakeWriteCompletionHandler();
    }
    private class HandshakeReadCompletionHandler
        implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
        @Override
        public void completed ( Integer result, SocketWrapperBase<Nio2Channel> attachment ) {
            if ( result.intValue() < 0 ) {
                failed ( new EOFException(), attachment );
            } else {
                endpoint.processSocket ( attachment, SocketEvent.OPEN_READ, false );
            }
        }
        @Override
        public void failed ( Throwable exc, SocketWrapperBase<Nio2Channel> attachment ) {
            endpoint.processSocket ( attachment, SocketEvent.ERROR, false );
        }
    }
    private class HandshakeWriteCompletionHandler
        implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
        @Override
        public void completed ( Integer result, SocketWrapperBase<Nio2Channel> attachment ) {
            if ( result.intValue() < 0 ) {
                failed ( new EOFException(), attachment );
            } else {
                endpoint.processSocket ( attachment, SocketEvent.OPEN_WRITE, false );
            }
        }
        @Override
        public void failed ( Throwable exc, SocketWrapperBase<Nio2Channel> attachment ) {
            endpoint.processSocket ( attachment, SocketEvent.ERROR, false );
        }
    }
    @Override
    public void reset ( AsynchronousSocketChannel channel, SocketWrapperBase<Nio2Channel> socket )
    throws IOException {
        super.reset ( channel, socket );
        sslEngine = null;
        sniComplete = false;
        handshakeComplete = false;
        closed = false;
        closing = false;
        netInBuffer.clear();
    }
    @Override
    public void free() {
        super.free();
        if ( endpoint.getSocketProperties().getDirectSslBuffer() ) {
            ByteBufferUtils.cleanDirectBuffer ( netInBuffer );
            ByteBufferUtils.cleanDirectBuffer ( netOutBuffer );
        }
    }
    private class FutureFlush implements Future<Boolean> {
        private Future<Integer> integer;
        protected FutureFlush() {
            integer = sc.write ( netOutBuffer );
        }
        @Override
        public boolean cancel ( boolean mayInterruptIfRunning ) {
            return integer.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return integer.isCancelled();
        }
        @Override
        public boolean isDone() {
            return integer.isDone();
        }
        @Override
        public Boolean get() throws InterruptedException,
            ExecutionException {
            return Boolean.valueOf ( integer.get().intValue() >= 0 );
        }
        @Override
        public Boolean get ( long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException,
            TimeoutException {
            return Boolean.valueOf ( integer.get ( timeout, unit ).intValue() >= 0 );
        }
    }
    @Override
    public Future<Boolean> flush() {
        return new FutureFlush();
    }
    @Override
    public int handshake() throws IOException {
        return handshakeInternal ( true );
    }
    protected int handshakeInternal ( boolean async ) throws IOException {
        if ( handshakeComplete ) {
            return 0;
        }
        if ( !sniComplete ) {
            int sniResult = processSNI();
            if ( sniResult == 0 ) {
                sniComplete = true;
            } else {
                return sniResult;
            }
        }
        SSLEngineResult handshake = null;
        while ( !handshakeComplete ) {
            switch ( handshakeStatus ) {
            case NOT_HANDSHAKING: {
                throw new IOException ( sm.getString ( "channel.nio.ssl.notHandshaking" ) );
            }
            case FINISHED: {
                if ( endpoint.hasNegotiableProtocols() && sslEngine instanceof SSLUtil.ProtocolInfo ) {
                    socket.setNegotiatedProtocol ( ( ( SSLUtil.ProtocolInfo ) sslEngine ).getNegotiatedProtocol() );
                }
                handshakeComplete = !netOutBuffer.hasRemaining();
                if ( handshakeComplete ) {
                    return 0;
                } else {
                    if ( async ) {
                        sc.write ( netOutBuffer, socket, handshakeWriteCompletionHandler );
                    } else {
                        try {
                            sc.write ( netOutBuffer ).get ( endpoint.getConnectionTimeout(),
                                                            TimeUnit.MILLISECONDS );
                        } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                            throw new IOException ( sm.getString ( "channel.nio.ssl.handhakeError" ) );
                        }
                    }
                    return 1;
                }
            }
            case NEED_WRAP: {
                try {
                    handshake = handshakeWrap();
                } catch ( SSLException e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "channel.nio.ssl.wrapException" ), e );
                    }
                    handshake = handshakeWrap();
                }
                if ( handshake.getStatus() == Status.OK ) {
                    if ( handshakeStatus == HandshakeStatus.NEED_TASK ) {
                        handshakeStatus = tasks();
                    }
                } else if ( handshake.getStatus() == Status.CLOSED ) {
                    return -1;
                } else {
                    throw new IOException ( sm.getString ( "channel.nio.ssl.unexpectedStatusDuringWrap", handshake.getStatus() ) );
                }
                if ( handshakeStatus != HandshakeStatus.NEED_UNWRAP || netOutBuffer.remaining() > 0 ) {
                    if ( async ) {
                        sc.write ( netOutBuffer, socket, handshakeWriteCompletionHandler );
                    } else {
                        try {
                            sc.write ( netOutBuffer ).get ( endpoint.getConnectionTimeout(),
                                                            TimeUnit.MILLISECONDS );
                        } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                            throw new IOException ( sm.getString ( "channel.nio.ssl.handhakeError" ) );
                        }
                    }
                    return 1;
                }
            }
            case NEED_UNWRAP: {
                handshake = handshakeUnwrap();
                if ( handshake.getStatus() == Status.OK ) {
                    if ( handshakeStatus == HandshakeStatus.NEED_TASK ) {
                        handshakeStatus = tasks();
                    }
                } else if ( handshake.getStatus() == Status.BUFFER_UNDERFLOW ) {
                    if ( async ) {
                        sc.read ( netInBuffer, socket, handshakeReadCompletionHandler );
                    } else {
                        try {
                            sc.read ( netInBuffer ).get ( endpoint.getConnectionTimeout(),
                                                          TimeUnit.MILLISECONDS );
                        } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                            throw new IOException ( sm.getString ( "channel.nio.ssl.handhakeError" ) );
                        }
                    }
                    return 1;
                } else {
                    throw new IOException ( sm.getString ( "channel.nio.ssl.unexpectedStatusDuringUnwrap", handshakeStatus ) );
                }
                break;
            }
            case NEED_TASK: {
                handshakeStatus = tasks();
                break;
            }
            default:
                throw new IllegalStateException ( sm.getString ( "channel.nio.ssl.invalidStatus", handshakeStatus ) );
            }
        }
        return handshakeComplete ? 0 : handshakeInternal ( async );
    }
    private int processSNI() throws IOException {
        if ( netInBuffer.position() == 0 ) {
            sc.read ( netInBuffer, socket, handshakeReadCompletionHandler );
            return 1;
        }
        TLSClientHelloExtractor extractor = new TLSClientHelloExtractor ( netInBuffer );
        while ( extractor.getResult() == ExtractorResult.UNDERFLOW &&
                netInBuffer.capacity() < endpoint.getSniParseLimit() ) {
            int newLimit = Math.min ( netInBuffer.capacity() * 2, endpoint.getSniParseLimit() );
            log.info ( sm.getString ( "channel.nio.ssl.expandNetInBuffer",
                                      Integer.toString ( newLimit ) ) );
            netInBuffer = ByteBufferUtils.expand ( netInBuffer, newLimit );
            sc.read ( netInBuffer );
            extractor = new TLSClientHelloExtractor ( netInBuffer );
        }
        String hostName = null;
        List<Cipher> clientRequestedCiphers = null;
        switch ( extractor.getResult() ) {
        case COMPLETE:
            hostName = extractor.getSNIValue();
        case NOT_PRESENT:
            clientRequestedCiphers = extractor.getClientRequestedCiphers();
            break;
        case NEED_READ:
            sc.read ( netInBuffer, socket, handshakeReadCompletionHandler );
            return 1;
        case UNDERFLOW:
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "channel.nio.ssl.sniDefault" ) );
            }
            hostName = endpoint.getDefaultSSLHostConfigName();
            clientRequestedCiphers = Collections.emptyList();
            break;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "channel.nio.ssl.sniHostName", hostName ) );
        }
        sslEngine = endpoint.createSSLEngine ( hostName, clientRequestedCiphers );
        getBufHandler().expand ( sslEngine.getSession().getApplicationBufferSize() );
        if ( netOutBuffer.capacity() < sslEngine.getSession().getApplicationBufferSize() ) {
            log.info ( sm.getString ( "channel.nio.ssl.expandNetOutBuffer",
                                      Integer.toString ( sslEngine.getSession().getApplicationBufferSize() ) ) );
        }
        netInBuffer = ByteBufferUtils.expand ( netInBuffer, sslEngine.getSession().getPacketBufferSize() );
        netOutBuffer = ByteBufferUtils.expand ( netOutBuffer, sslEngine.getSession().getPacketBufferSize() );
        netOutBuffer.position ( 0 );
        netOutBuffer.limit ( 0 );
        sslEngine.beginHandshake();
        handshakeStatus = sslEngine.getHandshakeStatus();
        return 0;
    }
    public void rehandshake() throws IOException {
        if ( netInBuffer.position() > 0 && netInBuffer.position() < netInBuffer.limit() ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.netInputNotEmpty" ) );
        }
        if ( netOutBuffer.position() > 0 && netOutBuffer.position() < netOutBuffer.limit() ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.netOutputNotEmpty" ) );
        }
        if ( !getBufHandler().isReadBufferEmpty() ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.appInputNotEmpty" ) );
        }
        if ( !getBufHandler().isWriteBufferEmpty() ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.appOutputNotEmpty" ) );
        }
        netOutBuffer.position ( 0 );
        netOutBuffer.limit ( 0 );
        netInBuffer.position ( 0 );
        netInBuffer.limit ( 0 );
        getBufHandler().reset();
        handshakeComplete = false;
        sslEngine.beginHandshake();
        handshakeStatus = sslEngine.getHandshakeStatus();
        boolean handshaking = true;
        try {
            while ( handshaking ) {
                int hsStatus = handshakeInternal ( false );
                switch ( hsStatus ) {
                case -1 :
                    throw new EOFException ( sm.getString ( "channel.nio.ssl.eofDuringHandshake" ) );
                case  0 :
                    handshaking = false;
                    break;
                default :
                }
            }
        } catch ( IOException x ) {
            throw x;
        } catch ( Exception cx ) {
            IOException x = new IOException ( cx );
            throw x;
        }
    }
    protected SSLEngineResult.HandshakeStatus tasks() {
        Runnable r = null;
        while ( ( r = sslEngine.getDelegatedTask() ) != null ) {
            r.run();
        }
        return sslEngine.getHandshakeStatus();
    }
    protected SSLEngineResult handshakeWrap() throws IOException {
        netOutBuffer.clear();
        getBufHandler().configureWriteBufferForRead();
        SSLEngineResult result = sslEngine.wrap ( getBufHandler().getWriteBuffer(), netOutBuffer );
        netOutBuffer.flip();
        handshakeStatus = result.getHandshakeStatus();
        return result;
    }
    protected SSLEngineResult handshakeUnwrap() throws IOException {
        if ( netInBuffer.position() == netInBuffer.limit() ) {
            netInBuffer.clear();
        }
        SSLEngineResult result;
        boolean cont = false;
        do {
            netInBuffer.flip();
            getBufHandler().configureReadBufferForWrite();
            result = sslEngine.unwrap ( netInBuffer, getBufHandler().getReadBuffer() );
            netInBuffer.compact();
            handshakeStatus = result.getHandshakeStatus();
            if ( result.getStatus() == SSLEngineResult.Status.OK &&
                    result.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                handshakeStatus = tasks();
            }
            cont = result.getStatus() == SSLEngineResult.Status.OK &&
                   handshakeStatus == HandshakeStatus.NEED_UNWRAP;
        } while ( cont );
        return result;
    }
    @Override
    public void close() throws IOException {
        if ( closing ) {
            return;
        }
        closing = true;
        sslEngine.closeOutbound();
        try {
            if ( !flush().get ( endpoint.getConnectionTimeout(),
                                TimeUnit.MILLISECONDS ).booleanValue() ) {
                throw new IOException ( sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ) );
            }
        } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ), e );
        } catch ( WritePendingException e ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.pendingWriteDuringClose" ), e );
        }
        netOutBuffer.clear();
        SSLEngineResult handshake = sslEngine.wrap ( getEmptyBuf(), netOutBuffer );
        if ( handshake.getStatus() != SSLEngineResult.Status.CLOSED ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.invalidCloseState" ) );
        }
        netOutBuffer.flip();
        try {
            if ( !flush().get ( endpoint.getConnectionTimeout(),
                                TimeUnit.MILLISECONDS ).booleanValue() ) {
                throw new IOException ( sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ) );
            }
        } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ), e );
        } catch ( WritePendingException e ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.pendingWriteDuringClose" ), e );
        }
        closed = ( !netOutBuffer.hasRemaining() && ( handshake.getHandshakeStatus() != HandshakeStatus.NEED_WRAP ) );
    }
    @Override
    public void close ( boolean force ) throws IOException {
        try {
            close();
        } finally {
            if ( force || closed ) {
                closed = true;
                sc.close();
            }
        }
    }
    private class FutureRead implements Future<Integer> {
        private ByteBuffer dst;
        private Future<Integer> integer;
        private FutureRead ( ByteBuffer dst ) {
            this.dst = dst;
            if ( unwrapBeforeRead || netInBuffer.position() > 0 ) {
                this.integer = null;
            } else {
                this.integer = sc.read ( netInBuffer );
            }
        }
        @Override
        public boolean cancel ( boolean mayInterruptIfRunning ) {
            return ( integer == null ) ? false : integer.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return ( integer == null ) ? false : integer.isCancelled();
        }
        @Override
        public boolean isDone() {
            return ( integer == null ) ? true : integer.isDone();
        }
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            try {
                return ( integer == null ) ? unwrap ( netInBuffer.position(), -1, TimeUnit.MILLISECONDS ) : unwrap ( integer.get().intValue(), -1, TimeUnit.MILLISECONDS );
            } catch ( TimeoutException e ) {
                throw new ExecutionException ( e );
            }
        }
        @Override
        public Integer get ( long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException,
            TimeoutException {
            return ( integer == null ) ? unwrap ( netInBuffer.position(), timeout, unit ) : unwrap ( integer.get ( timeout, unit ).intValue(), timeout, unit );
        }
        private Integer unwrap ( int nRead, long timeout, TimeUnit unit ) throws ExecutionException, TimeoutException, InterruptedException {
            if ( closing || closed ) {
                return Integer.valueOf ( -1 );
            }
            if ( nRead < 0 ) {
                return Integer.valueOf ( -1 );
            }
            int read = 0;
            SSLEngineResult unwrap;
            do {
                netInBuffer.flip();
                try {
                    unwrap = sslEngine.unwrap ( netInBuffer, dst );
                } catch ( SSLException e ) {
                    throw new ExecutionException ( e );
                }
                netInBuffer.compact();
                if ( unwrap.getStatus() == Status.OK || unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                    read += unwrap.bytesProduced();
                    if ( unwrap.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                        tasks();
                    }
                    if ( unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                        if ( read == 0 ) {
                            integer = sc.read ( netInBuffer );
                            if ( timeout > 0 ) {
                                return unwrap ( integer.get ( timeout, unit ).intValue(), timeout, unit );
                            } else {
                                return unwrap ( integer.get().intValue(), -1, TimeUnit.MILLISECONDS );
                            }
                        } else {
                            break;
                        }
                    }
                } else if ( unwrap.getStatus() == Status.BUFFER_OVERFLOW ) {
                    if ( read > 0 ) {
                        break;
                    } else {
                        if ( dst == getBufHandler().getReadBuffer() ) {
                            getBufHandler()
                            .expand ( sslEngine.getSession().getApplicationBufferSize() );
                            dst = getBufHandler().getReadBuffer();
                        } else if ( dst == getAppReadBufHandler().getByteBuffer() ) {
                            getAppReadBufHandler()
                            .expand ( sslEngine.getSession().getApplicationBufferSize() );
                            dst = getAppReadBufHandler().getByteBuffer();
                        } else {
                            throw new ExecutionException ( new IOException ( sm.getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) ) );
                        }
                    }
                } else {
                    throw new ExecutionException ( new IOException ( sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) ) );
                }
            } while ( netInBuffer.position() != 0 );
            if ( !dst.hasRemaining() ) {
                unwrapBeforeRead = true;
            } else {
                unwrapBeforeRead = false;
            }
            return Integer.valueOf ( read );
        }
    }
    @Override
    public Future<Integer> read ( ByteBuffer dst ) {
        if ( !handshakeComplete ) {
            throw new IllegalStateException ( sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        return new FutureRead ( dst );
    }
    private class FutureWrite implements Future<Integer> {
        private final ByteBuffer src;
        private Future<Integer> integer = null;
        private int written = 0;
        private Throwable t = null;
        private FutureWrite ( ByteBuffer src ) {
            this.src = src;
            if ( closing || closed ) {
                t = new IOException ( sm.getString ( "channel.nio.ssl.closing" ) );
            } else {
                wrap();
            }
        }
        @Override
        public boolean cancel ( boolean mayInterruptIfRunning ) {
            return integer.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return integer.isCancelled();
        }
        @Override
        public boolean isDone() {
            return integer.isDone();
        }
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            if ( t != null ) {
                throw new ExecutionException ( t );
            }
            if ( integer.get().intValue() > 0 && written == 0 ) {
                wrap();
                return get();
            } else if ( netOutBuffer.hasRemaining() ) {
                integer = sc.write ( netOutBuffer );
                return get();
            } else {
                return Integer.valueOf ( written );
            }
        }
        @Override
        public Integer get ( long timeout, TimeUnit unit )
        throws InterruptedException, ExecutionException,
            TimeoutException {
            if ( t != null ) {
                throw new ExecutionException ( t );
            }
            if ( integer.get ( timeout, unit ).intValue() > 0 && written == 0 ) {
                wrap();
                return get ( timeout, unit );
            } else if ( netOutBuffer.hasRemaining() ) {
                integer = sc.write ( netOutBuffer );
                return get ( timeout, unit );
            } else {
                return Integer.valueOf ( written );
            }
        }
        protected void wrap() {
            try {
                if ( !netOutBuffer.hasRemaining() ) {
                    netOutBuffer.clear();
                    SSLEngineResult result = sslEngine.wrap ( src, netOutBuffer );
                    written = result.bytesConsumed();
                    netOutBuffer.flip();
                    if ( result.getStatus() == Status.OK ) {
                        if ( result.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                            tasks();
                        }
                    } else {
                        t = new IOException ( sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
                    }
                }
                integer = sc.write ( netOutBuffer );
            } catch ( SSLException e ) {
                t = e;
            }
        }
    }
    @Override
    public Future<Integer> write ( ByteBuffer src ) {
        return new FutureWrite ( src );
    }
    @Override
    public <A> void read ( final ByteBuffer dst,
                           final long timeout, final TimeUnit unit, final A attachment,
                           final CompletionHandler<Integer, ? super A> handler ) {
        if ( closing || closed ) {
            handler.completed ( Integer.valueOf ( -1 ), attachment );
            return;
        }
        if ( !handshakeComplete ) {
            throw new IllegalStateException ( sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        CompletionHandler<Integer, A> readCompletionHandler = new CompletionHandler<Integer, A>() {
            @Override
            public void completed ( Integer nBytes, A attach ) {
                if ( nBytes.intValue() < 0 ) {
                    failed ( new EOFException(), attach );
                } else {
                    try {
                        ByteBuffer dst2 = dst;
                        int read = 0;
                        SSLEngineResult unwrap;
                        do {
                            netInBuffer.flip();
                            unwrap = sslEngine.unwrap ( netInBuffer, dst2 );
                            netInBuffer.compact();
                            if ( unwrap.getStatus() == Status.OK || unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                                read += unwrap.bytesProduced();
                                if ( unwrap.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                                    tasks();
                                }
                                if ( unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                                    if ( read == 0 ) {
                                        sc.read ( netInBuffer, timeout, unit, attachment, this );
                                        return;
                                    } else {
                                        break;
                                    }
                                }
                            } else if ( unwrap.getStatus() == Status.BUFFER_OVERFLOW ) {
                                if ( read > 0 ) {
                                    break;
                                } else {
                                    if ( dst2 == getBufHandler().getReadBuffer() ) {
                                        getBufHandler().expand (
                                            sslEngine.getSession().getApplicationBufferSize() );
                                        dst2 = getBufHandler().getReadBuffer();
                                    } else {
                                        throw new IOException (
                                            sm.getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) );
                                    }
                                }
                            } else {
                                throw new IOException ( sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                            }
                        } while ( netInBuffer.position() != 0 );
                        if ( !dst2.hasRemaining() ) {
                            unwrapBeforeRead = true;
                        } else {
                            unwrapBeforeRead = false;
                        }
                        handler.completed ( Integer.valueOf ( read ), attach );
                    } catch ( Exception e ) {
                        failed ( e, attach );
                    }
                }
            }
            @Override
            public void failed ( Throwable exc, A attach ) {
                handler.failed ( exc, attach );
            }
        };
        if ( unwrapBeforeRead || netInBuffer.position() > 0 ) {
            readCompletionHandler.completed ( Integer.valueOf ( netInBuffer.position() ), attachment );
        } else {
            sc.read ( netInBuffer, timeout, unit, attachment, readCompletionHandler );
        }
    }
    @Override
    public <A> void read ( final ByteBuffer[] dsts, final int offset, final int length,
                           final long timeout, final TimeUnit unit, final A attachment,
                           final CompletionHandler<Long, ? super A> handler ) {
        if ( offset < 0 || dsts == null || ( offset + length ) > dsts.length ) {
            throw new IllegalArgumentException();
        }
        if ( closing || closed ) {
            handler.completed ( Long.valueOf ( -1 ), attachment );
            return;
        }
        if ( !handshakeComplete ) {
            throw new IllegalStateException ( sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        CompletionHandler<Integer, A> readCompletionHandler = new CompletionHandler<Integer, A>() {
            @Override
            public void completed ( Integer nBytes, A attach ) {
                if ( nBytes.intValue() < 0 ) {
                    failed ( new EOFException(), attach );
                } else {
                    try {
                        long read = 0;
                        SSLEngineResult unwrap;
                        do {
                            netInBuffer.flip();
                            unwrap = sslEngine.unwrap ( netInBuffer, dsts, offset, length );
                            netInBuffer.compact();
                            if ( unwrap.getStatus() == Status.OK || unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                                read += unwrap.bytesProduced();
                                if ( unwrap.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                                    tasks();
                                }
                                if ( unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                                    if ( read == 0 ) {
                                        sc.read ( netInBuffer, timeout, unit, attachment, this );
                                        return;
                                    } else {
                                        break;
                                    }
                                }
                            } else if ( unwrap.getStatus() == Status.BUFFER_OVERFLOW && read > 0 ) {
                                break;
                            } else {
                                throw new IOException ( sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                            }
                        } while ( netInBuffer.position() != 0 );
                        int capacity = 0;
                        final int endOffset = offset + length;
                        for ( int i = offset; i < endOffset; i++ ) {
                            capacity += dsts[i].remaining();
                        }
                        if ( capacity == 0 ) {
                            unwrapBeforeRead = true;
                        } else {
                            unwrapBeforeRead = false;
                        }
                        handler.completed ( Long.valueOf ( read ), attach );
                    } catch ( Exception e ) {
                        failed ( e, attach );
                    }
                }
            }
            @Override
            public void failed ( Throwable exc, A attach ) {
                handler.failed ( exc, attach );
            }
        };
        if ( unwrapBeforeRead || netInBuffer.position() > 0 ) {
            readCompletionHandler.completed ( Integer.valueOf ( netInBuffer.position() ), attachment );
        } else {
            sc.read ( netInBuffer, timeout, unit, attachment, readCompletionHandler );
        }
    }
    @Override
    public <A> void write ( final ByteBuffer src, final long timeout, final TimeUnit unit,
                            final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        if ( closing || closed ) {
            handler.failed ( new IOException ( sm.getString ( "channel.nio.ssl.closing" ) ), attachment );
            return;
        }
        try {
            netOutBuffer.clear();
            SSLEngineResult result = sslEngine.wrap ( src, netOutBuffer );
            final int written = result.bytesConsumed();
            netOutBuffer.flip();
            if ( result.getStatus() == Status.OK ) {
                if ( result.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                    tasks();
                }
                sc.write ( netOutBuffer, timeout, unit, attachment,
                new CompletionHandler<Integer, A>() {
                    @Override
                    public void completed ( Integer nBytes, A attach ) {
                        if ( nBytes.intValue() < 0 ) {
                            failed ( new EOFException(), attach );
                        } else if ( netOutBuffer.hasRemaining() ) {
                            sc.write ( netOutBuffer, timeout, unit, attachment, this );
                        } else if ( written == 0 ) {
                            write ( src, timeout, unit, attachment, handler );
                        } else {
                            handler.completed ( Integer.valueOf ( written ), attach );
                        }
                    }
                    @Override
                    public void failed ( Throwable exc, A attach ) {
                        handler.failed ( exc, attach );
                    }
                } );
            } else {
                throw new IOException ( sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
            }
        } catch ( Exception e ) {
            handler.failed ( e, attachment );
        }
    }
    @Override
    public <A> void write ( final ByteBuffer[] srcs, final int offset, final int length,
                            final long timeout, final TimeUnit unit, final A attachment,
                            final CompletionHandler<Long, ? super A> handler ) {
        if ( ( offset < 0 ) || ( length < 0 ) || ( offset > srcs.length - length ) ) {
            throw new IndexOutOfBoundsException();
        }
        if ( closing || closed ) {
            handler.failed ( new IOException ( sm.getString ( "channel.nio.ssl.closing" ) ), attachment );
            return;
        }
        try {
            netOutBuffer.clear();
            SSLEngineResult result = sslEngine.wrap ( srcs, offset, length, netOutBuffer );
            final int written = result.bytesConsumed();
            netOutBuffer.flip();
            if ( result.getStatus() == Status.OK ) {
                if ( result.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                    tasks();
                }
                sc.write ( netOutBuffer, timeout, unit, attachment, new CompletionHandler<Integer, A>() {
                    @Override
                    public void completed ( Integer nBytes, A attach ) {
                        if ( nBytes.intValue() < 0 ) {
                            failed ( new EOFException(), attach );
                        } else if ( netOutBuffer.hasRemaining() ) {
                            sc.write ( netOutBuffer, timeout, unit, attachment, this );
                        } else if ( written == 0 ) {
                            write ( srcs, offset, length, timeout, unit, attachment, handler );
                        } else {
                            handler.completed ( Long.valueOf ( written ), attach );
                        }
                    }
                    @Override
                    public void failed ( Throwable exc, A attach ) {
                        handler.failed ( exc, attach );
                    }
                } );
            } else {
                throw new IOException ( sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
            }
        } catch ( Exception e ) {
            handler.failed ( e, attachment );
        }
    }
    @Override
    public boolean isHandshakeComplete() {
        return handshakeComplete;
    }
    @Override
    public boolean isClosing() {
        return closing;
    }
    public SSLEngine getSslEngine() {
        return sslEngine;
    }
    public ByteBuffer getEmptyBuf() {
        return emptyBuf;
    }
    @Override
    public AsynchronousSocketChannel getIOChannel() {
        return sc;
    }
}

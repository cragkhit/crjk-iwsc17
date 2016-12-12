package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import java.nio.channels.SelectionKey;
import java.net.SocketTimeoutException;
import java.io.EOFException;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import java.util.List;
import java.util.Collections;
import javax.net.ssl.SSLException;
import java.nio.channels.Selector;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class SecureNioChannel extends NioChannel {
    private static final Log log;
    private static final StringManager sm;
    private static final int DEFAULT_NET_BUFFER_SIZE = 16921;
    protected ByteBuffer netInBuffer;
    protected ByteBuffer netOutBuffer;
    protected SSLEngine sslEngine;
    protected boolean sniComplete;
    protected boolean handshakeComplete;
    protected SSLEngineResult.HandshakeStatus handshakeStatus;
    protected boolean closed;
    protected boolean closing;
    protected NioSelectorPool pool;
    private final NioEndpoint endpoint;
    public SecureNioChannel ( final SocketChannel channel, final SocketBufferHandler bufHandler, final NioSelectorPool pool, final NioEndpoint endpoint ) {
        super ( channel, bufHandler );
        this.sniComplete = false;
        this.handshakeComplete = false;
        this.closed = false;
        this.closing = false;
        if ( endpoint.getSocketProperties().getDirectSslBuffer() ) {
            this.netInBuffer = ByteBuffer.allocateDirect ( 16921 );
            this.netOutBuffer = ByteBuffer.allocateDirect ( 16921 );
        } else {
            this.netInBuffer = ByteBuffer.allocate ( 16921 );
            this.netOutBuffer = ByteBuffer.allocate ( 16921 );
        }
        this.pool = pool;
        this.endpoint = endpoint;
    }
    @Override
    public void reset() throws IOException {
        super.reset();
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
    public boolean flush ( final boolean block, final Selector s, final long timeout ) throws IOException {
        if ( !block ) {
            this.flush ( this.netOutBuffer );
        } else {
            this.pool.write ( this.netOutBuffer, this, s, timeout, block );
        }
        return !this.netOutBuffer.hasRemaining();
    }
    protected boolean flush ( final ByteBuffer buf ) throws IOException {
        final int remaining = buf.remaining();
        if ( remaining > 0 ) {
            final int written = this.sc.write ( buf );
            return written >= remaining;
        }
        return true;
    }
    @Override
    public int handshake ( final boolean read, final boolean write ) throws IOException {
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
        if ( !this.flush ( this.netOutBuffer ) ) {
            return 4;
        }
        SSLEngineResult handshake = null;
        while ( !this.handshakeComplete ) {
            Label_0339: {
                switch ( this.handshakeStatus ) {
                case NOT_HANDSHAKING: {
                    throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.notHandshaking" ) );
                }
                case FINISHED: {
                    if ( this.endpoint.hasNegotiableProtocols() && this.sslEngine instanceof SSLUtil.ProtocolInfo ) {
                        this.socketWrapper.setNegotiatedProtocol ( ( ( SSLUtil.ProtocolInfo ) this.sslEngine ).getNegotiatedProtocol() );
                    }
                    this.handshakeComplete = !this.netOutBuffer.hasRemaining();
                    return this.handshakeComplete ? 0 : 4;
                }
                case NEED_WRAP: {
                    try {
                        handshake = this.handshakeWrap ( write );
                    } catch ( SSLException e ) {
                        if ( SecureNioChannel.log.isDebugEnabled() ) {
                            SecureNioChannel.log.debug ( SecureNioChannel.sm.getString ( "channel.nio.ssl.wrapException" ), e );
                        }
                        handshake = this.handshakeWrap ( write );
                    }
                    if ( handshake.getStatus() == SSLEngineResult.Status.OK ) {
                        if ( this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            this.handshakeStatus = this.tasks();
                        }
                        if ( this.handshakeStatus != SSLEngineResult.HandshakeStatus.NEED_UNWRAP || !this.flush ( this.netOutBuffer ) ) {
                            return 4;
                        }
                        break Label_0339;
                    } else {
                        if ( handshake.getStatus() == SSLEngineResult.Status.CLOSED ) {
                            this.flush ( this.netOutBuffer );
                            return -1;
                        }
                        throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.unexpectedStatusDuringWrap", handshake.getStatus() ) );
                    }
                    break;
                }
                case NEED_UNWRAP: {
                    handshake = this.handshakeUnwrap ( read );
                    if ( handshake.getStatus() == SSLEngineResult.Status.OK ) {
                        if ( this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            this.handshakeStatus = this.tasks();
                            continue;
                        }
                        continue;
                    } else {
                        if ( handshake.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                            return 1;
                        }
                        if ( handshake.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                            this.getBufHandler().configureReadBufferForWrite();
                            continue;
                        }
                        throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.unexpectedStatusDuringWrap", this.handshakeStatus ) );
                    }
                    break;
                }
                case NEED_TASK: {
                    this.handshakeStatus = this.tasks();
                    continue;
                }
                default: {
                    throw new IllegalStateException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.invalidStatus", this.handshakeStatus ) );
                }
                }
            }
        }
        return 0;
    }
    private int processSNI() throws IOException {
        final int bytesRead = this.sc.read ( this.netInBuffer );
        if ( bytesRead == -1 ) {
            return -1;
        }
        TLSClientHelloExtractor extractor;
        for ( extractor = new TLSClientHelloExtractor ( this.netInBuffer ); extractor.getResult() == TLSClientHelloExtractor.ExtractorResult.UNDERFLOW && this.netInBuffer.capacity() < this.endpoint.getSniParseLimit(); extractor = new TLSClientHelloExtractor ( this.netInBuffer ) ) {
            final int newLimit = Math.min ( this.netInBuffer.capacity() * 2, this.endpoint.getSniParseLimit() );
            SecureNioChannel.log.info ( SecureNioChannel.sm.getString ( "channel.nio.ssl.expandNetInBuffer", Integer.toString ( newLimit ) ) );
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
            return 1;
        }
        case UNDERFLOW: {
            if ( SecureNioChannel.log.isDebugEnabled() ) {
                SecureNioChannel.log.debug ( SecureNioChannel.sm.getString ( "channel.nio.ssl.sniDefault" ) );
            }
            hostName = this.endpoint.getDefaultSSLHostConfigName();
            clientRequestedCiphers = Collections.emptyList();
            break;
        }
        }
        if ( SecureNioChannel.log.isDebugEnabled() ) {
            SecureNioChannel.log.debug ( SecureNioChannel.sm.getString ( "channel.nio.ssl.sniHostName", hostName ) );
        }
        this.sslEngine = this.endpoint.createSSLEngine ( hostName, clientRequestedCiphers );
        this.getBufHandler().expand ( this.sslEngine.getSession().getApplicationBufferSize() );
        if ( this.netOutBuffer.capacity() < this.sslEngine.getSession().getApplicationBufferSize() ) {
            SecureNioChannel.log.info ( SecureNioChannel.sm.getString ( "channel.nio.ssl.expandNetOutBuffer", Integer.toString ( this.sslEngine.getSession().getApplicationBufferSize() ) ) );
        }
        this.netInBuffer = ByteBufferUtils.expand ( this.netInBuffer, this.sslEngine.getSession().getPacketBufferSize() );
        ( this.netOutBuffer = ByteBufferUtils.expand ( this.netOutBuffer, this.sslEngine.getSession().getPacketBufferSize() ) ).position ( 0 );
        this.netOutBuffer.limit ( 0 );
        this.sslEngine.beginHandshake();
        this.handshakeStatus = this.sslEngine.getHandshakeStatus();
        return 0;
    }
    public void rehandshake ( final long timeout ) throws IOException {
        if ( this.netInBuffer.position() > 0 && this.netInBuffer.position() < this.netInBuffer.limit() ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.netInputNotEmpty" ) );
        }
        if ( this.netOutBuffer.position() > 0 && this.netOutBuffer.position() < this.netOutBuffer.limit() ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.netOutputNotEmpty" ) );
        }
        if ( !this.getBufHandler().isReadBufferEmpty() ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.appInputNotEmpty" ) );
        }
        if ( !this.getBufHandler().isWriteBufferEmpty() ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.appOutputNotEmpty" ) );
        }
        this.handshakeComplete = false;
        boolean isReadable = false;
        boolean isWriteable = false;
        boolean handshaking = true;
        Selector selector = null;
        SelectionKey key = null;
        try {
            this.sslEngine.beginHandshake();
            this.handshakeStatus = this.sslEngine.getHandshakeStatus();
            while ( handshaking ) {
                final int hsStatus = this.handshake ( isReadable, isWriteable );
                switch ( hsStatus ) {
                case -1: {
                    throw new EOFException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.eofDuringHandshake" ) );
                }
                case 0: {
                    handshaking = false;
                    continue;
                }
                default: {
                    final long now = System.currentTimeMillis();
                    if ( selector == null ) {
                        selector = Selector.open();
                        key = this.getIOChannel().register ( selector, hsStatus );
                    } else {
                        key.interestOps ( hsStatus );
                    }
                    final int keyCount = selector.select ( timeout );
                    if ( keyCount == 0 && System.currentTimeMillis() - now >= timeout ) {
                        throw new SocketTimeoutException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.timeoutDuringHandshake" ) );
                    }
                    isReadable = key.isReadable();
                    isWriteable = key.isWritable();
                    continue;
                }
                }
            }
        } catch ( IOException x ) {
            throw x;
        } catch ( Exception cx ) {
            final IOException x2 = new IOException ( cx );
            throw x2;
        } finally {
            if ( key != null ) {
                try {
                    key.cancel();
                } catch ( Exception ex ) {}
            }
            if ( selector != null ) {
                try {
                    selector.close();
                } catch ( Exception ex2 ) {}
            }
        }
    }
    protected SSLEngineResult.HandshakeStatus tasks() {
        Runnable r = null;
        while ( ( r = this.sslEngine.getDelegatedTask() ) != null ) {
            r.run();
        }
        return this.sslEngine.getHandshakeStatus();
    }
    protected SSLEngineResult handshakeWrap ( final boolean doWrite ) throws IOException {
        this.netOutBuffer.clear();
        this.getBufHandler().configureWriteBufferForWrite();
        final SSLEngineResult result = this.sslEngine.wrap ( this.getBufHandler().getWriteBuffer(), this.netOutBuffer );
        this.netOutBuffer.flip();
        this.handshakeStatus = result.getHandshakeStatus();
        if ( doWrite ) {
            this.flush ( this.netOutBuffer );
        }
        return result;
    }
    protected SSLEngineResult handshakeUnwrap ( final boolean doread ) throws IOException {
        if ( this.netInBuffer.position() == this.netInBuffer.limit() ) {
            this.netInBuffer.clear();
        }
        if ( doread ) {
            final int read = this.sc.read ( this.netInBuffer );
            if ( read == -1 ) {
                throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.eofDuringHandshake" ) );
            }
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
        if ( !this.flush ( this.netOutBuffer ) ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ) );
        }
        this.netOutBuffer.clear();
        final SSLEngineResult handshake = this.sslEngine.wrap ( this.getEmptyBuf(), this.netOutBuffer );
        if ( handshake.getStatus() != SSLEngineResult.Status.CLOSED ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.invalidCloseState" ) );
        }
        this.netOutBuffer.flip();
        this.flush ( this.netOutBuffer );
        this.closed = ( !this.netOutBuffer.hasRemaining() && handshake.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_WRAP );
    }
    @Override
    public void close ( final boolean force ) throws IOException {
        try {
            this.close();
        } finally {
            if ( force || this.closed ) {
                this.closed = true;
                this.sc.socket().close();
                this.sc.close();
            }
        }
    }
    @Override
    public int read ( ByteBuffer dst ) throws IOException {
        if ( dst != this.getBufHandler().getReadBuffer() && ( this.getAppReadBufHandler() == null || dst != this.getAppReadBufHandler().getByteBuffer() ) ) {
            throw new IllegalArgumentException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.invalidBuffer" ) );
        }
        if ( this.closing || this.closed ) {
            return -1;
        }
        if ( !this.handshakeComplete ) {
            throw new IllegalStateException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        final int netread = this.sc.read ( this.netInBuffer );
        if ( netread == -1 ) {
            return -1;
        }
        int read = 0;
        do {
            this.netInBuffer.flip();
            final SSLEngineResult unwrap = this.sslEngine.unwrap ( this.netInBuffer, dst );
            this.netInBuffer.compact();
            if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                read += unwrap.bytesProduced();
                if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                    this.tasks();
                }
                if ( unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                    break;
                }
                continue;
            } else {
                if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                    throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                }
                if ( read > 0 ) {
                    break;
                }
                if ( dst == this.getBufHandler().getReadBuffer() ) {
                    this.getBufHandler().expand ( this.sslEngine.getSession().getApplicationBufferSize() );
                    dst = this.getBufHandler().getReadBuffer();
                } else {
                    if ( dst != this.getAppReadBufHandler().getByteBuffer() ) {
                        throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) );
                    }
                    this.getAppReadBufHandler().expand ( this.sslEngine.getSession().getApplicationBufferSize() );
                    dst = this.getAppReadBufHandler().getByteBuffer();
                }
            }
        } while ( this.netInBuffer.position() != 0 );
        return read;
    }
    @Override
    public int write ( final ByteBuffer src ) throws IOException {
        this.checkInterruptStatus();
        if ( src == this.netOutBuffer ) {
            final int written = this.sc.write ( src );
            return written;
        }
        if ( this.closing || this.closed ) {
            throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.closing" ) );
        }
        if ( !this.flush ( this.netOutBuffer ) ) {
            return 0;
        }
        this.netOutBuffer.clear();
        final SSLEngineResult result = this.sslEngine.wrap ( src, this.netOutBuffer );
        final int written2 = result.bytesConsumed();
        this.netOutBuffer.flip();
        if ( result.getStatus() == SSLEngineResult.Status.OK ) {
            if ( result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                this.tasks();
            }
            this.flush ( this.netOutBuffer );
            return written2;
        }
        throw new IOException ( SecureNioChannel.sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
    }
    @Override
    public int getOutboundRemaining() {
        return this.netOutBuffer.remaining();
    }
    @Override
    public boolean flushOutbound() throws IOException {
        final int remaining = this.netOutBuffer.remaining();
        this.flush ( this.netOutBuffer );
        final int remaining2 = this.netOutBuffer.remaining();
        return remaining2 < remaining;
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
        return SecureNioChannel.emptyBuf;
    }
    @Override
    public SocketChannel getIOChannel() {
        return this.sc;
    }
    static {
        log = LogFactory.getLog ( SecureNioChannel.class );
        sm = StringManager.getManager ( SecureNioChannel.class );
    }
}

package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
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
public class SecureNioChannel extends NioChannel  {
    private static final Log log = LogFactory.getLog ( SecureNioChannel.class );
    private static final StringManager sm = StringManager.getManager ( SecureNioChannel.class );
    private static final int DEFAULT_NET_BUFFER_SIZE = 16921;
    protected ByteBuffer netInBuffer;
    protected ByteBuffer netOutBuffer;
    protected SSLEngine sslEngine;
    protected boolean sniComplete = false;
    protected boolean handshakeComplete = false;
    protected HandshakeStatus handshakeStatus;
    protected boolean closed = false;
    protected boolean closing = false;
    protected NioSelectorPool pool;
    private final NioEndpoint endpoint;
    public SecureNioChannel ( SocketChannel channel, SocketBufferHandler bufHandler,
                              NioSelectorPool pool, NioEndpoint endpoint ) {
        super ( channel, bufHandler );
        if ( endpoint.getSocketProperties().getDirectSslBuffer() ) {
            netInBuffer = ByteBuffer.allocateDirect ( DEFAULT_NET_BUFFER_SIZE );
            netOutBuffer = ByteBuffer.allocateDirect ( DEFAULT_NET_BUFFER_SIZE );
        } else {
            netInBuffer = ByteBuffer.allocate ( DEFAULT_NET_BUFFER_SIZE );
            netOutBuffer = ByteBuffer.allocate ( DEFAULT_NET_BUFFER_SIZE );
        }
        this.pool = pool;
        this.endpoint = endpoint;
    }
    @Override
    public void reset() throws IOException {
        super.reset();
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
    @Override
    public boolean flush ( boolean block, Selector s, long timeout ) throws IOException {
        if ( !block ) {
            flush ( netOutBuffer );
        } else {
            pool.write ( netOutBuffer, this, s, timeout, block );
        }
        return !netOutBuffer.hasRemaining();
    }
    protected boolean flush ( ByteBuffer buf ) throws IOException {
        int remaining = buf.remaining();
        if ( remaining > 0 ) {
            int written = sc.write ( buf );
            return written >= remaining;
        } else {
            return true;
        }
    }
    @Override
    public int handshake ( boolean read, boolean write ) throws IOException {
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
        if ( !flush ( netOutBuffer ) ) {
            return SelectionKey.OP_WRITE;
        }
        SSLEngineResult handshake = null;
        while ( !handshakeComplete ) {
            switch ( handshakeStatus ) {
            case NOT_HANDSHAKING: {
                throw new IOException ( sm.getString ( "channel.nio.ssl.notHandshaking" ) );
            }
            case FINISHED: {
                if ( endpoint.hasNegotiableProtocols() && sslEngine instanceof SSLUtil.ProtocolInfo ) {
                    socketWrapper.setNegotiatedProtocol (
                        ( ( SSLUtil.ProtocolInfo ) sslEngine ).getNegotiatedProtocol() );
                }
                handshakeComplete = !netOutBuffer.hasRemaining();
                return handshakeComplete ? 0 : SelectionKey.OP_WRITE;
            }
            case NEED_WRAP: {
                try {
                    handshake = handshakeWrap ( write );
                } catch ( SSLException e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "channel.nio.ssl.wrapException" ), e );
                    }
                    handshake = handshakeWrap ( write );
                }
                if ( handshake.getStatus() == Status.OK ) {
                    if ( handshakeStatus == HandshakeStatus.NEED_TASK ) {
                        handshakeStatus = tasks();
                    }
                } else if ( handshake.getStatus() == Status.CLOSED ) {
                    flush ( netOutBuffer );
                    return -1;
                } else {
                    throw new IOException ( sm.getString ( "channel.nio.ssl.unexpectedStatusDuringWrap", handshake.getStatus() ) );
                }
                if ( handshakeStatus != HandshakeStatus.NEED_UNWRAP || ( !flush ( netOutBuffer ) ) ) {
                    return SelectionKey.OP_WRITE;
                }
            }
            case NEED_UNWRAP: {
                handshake = handshakeUnwrap ( read );
                if ( handshake.getStatus() == Status.OK ) {
                    if ( handshakeStatus == HandshakeStatus.NEED_TASK ) {
                        handshakeStatus = tasks();
                    }
                } else if ( handshake.getStatus() == Status.BUFFER_UNDERFLOW ) {
                    return SelectionKey.OP_READ;
                } else if ( handshake.getStatus() == Status.BUFFER_OVERFLOW ) {
                    getBufHandler().configureReadBufferForWrite();
                } else {
                    throw new IOException ( sm.getString ( "channel.nio.ssl.unexpectedStatusDuringWrap", handshakeStatus ) );
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
        return 0;
    }
    private int processSNI() throws IOException {
        int bytesRead = sc.read ( netInBuffer );
        if ( bytesRead == -1 ) {
            return -1;
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
            return SelectionKey.OP_READ;
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
    @SuppressWarnings ( "null" )
    public void rehandshake ( long timeout ) throws IOException {
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
        handshakeComplete = false;
        boolean isReadable = false;
        boolean isWriteable = false;
        boolean handshaking = true;
        Selector selector = null;
        SelectionKey key = null;
        try {
            sslEngine.beginHandshake();
            handshakeStatus = sslEngine.getHandshakeStatus();
            while ( handshaking ) {
                int hsStatus = this.handshake ( isReadable, isWriteable );
                switch ( hsStatus ) {
                case -1 :
                    throw new EOFException ( sm.getString ( "channel.nio.ssl.eofDuringHandshake" ) );
                case  0 :
                    handshaking = false;
                    break;
                default : {
                    long now = System.currentTimeMillis();
                    if ( selector == null ) {
                        selector = Selector.open();
                        key = getIOChannel().register ( selector, hsStatus );
                    } else {
                        key.interestOps ( hsStatus );
                    }
                    int keyCount = selector.select ( timeout );
                    if ( keyCount == 0 && ( ( System.currentTimeMillis() - now ) >= timeout ) ) {
                        throw new SocketTimeoutException ( sm.getString ( "channel.nio.ssl.timeoutDuringHandshake" ) );
                    }
                    isReadable = key.isReadable();
                    isWriteable = key.isWritable();
                }
                }
            }
        } catch ( IOException x ) {
            throw x;
        } catch ( Exception cx ) {
            IOException x = new IOException ( cx );
            throw x;
        } finally {
            if ( key != null ) try {
                    key.cancel();
                } catch ( Exception ignore ) {}
            if ( selector != null ) try {
                    selector.close();
                } catch ( Exception ignore ) {}
        }
    }
    protected SSLEngineResult.HandshakeStatus tasks() {
        Runnable r = null;
        while ( ( r = sslEngine.getDelegatedTask() ) != null ) {
            r.run();
        }
        return sslEngine.getHandshakeStatus();
    }
    protected SSLEngineResult handshakeWrap ( boolean doWrite ) throws IOException {
        netOutBuffer.clear();
        getBufHandler().configureWriteBufferForWrite();
        SSLEngineResult result = sslEngine.wrap ( getBufHandler().getWriteBuffer(), netOutBuffer );
        netOutBuffer.flip();
        handshakeStatus = result.getHandshakeStatus();
        if ( doWrite ) {
            flush ( netOutBuffer );
        }
        return result;
    }
    protected SSLEngineResult handshakeUnwrap ( boolean doread ) throws IOException {
        if ( netInBuffer.position() == netInBuffer.limit() ) {
            netInBuffer.clear();
        }
        if ( doread )  {
            int read = sc.read ( netInBuffer );
            if ( read == -1 ) {
                throw new IOException ( sm.getString ( "channel.nio.ssl.eofDuringHandshake" ) );
            }
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
        if ( !flush ( netOutBuffer ) ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.remainingDataDuringClose" ) );
        }
        netOutBuffer.clear();
        SSLEngineResult handshake = sslEngine.wrap ( getEmptyBuf(), netOutBuffer );
        if ( handshake.getStatus() != SSLEngineResult.Status.CLOSED ) {
            throw new IOException ( sm.getString ( "channel.nio.ssl.invalidCloseState" ) );
        }
        netOutBuffer.flip();
        flush ( netOutBuffer );
        closed = ( !netOutBuffer.hasRemaining() && ( handshake.getHandshakeStatus() != HandshakeStatus.NEED_WRAP ) );
    }
    @Override
    public void close ( boolean force ) throws IOException {
        try {
            close();
        } finally {
            if ( force || closed ) {
                closed = true;
                sc.socket().close();
                sc.close();
            }
        }
    }
    @Override
    public int read ( ByteBuffer dst ) throws IOException {
        if ( dst != getBufHandler().getReadBuffer() && ( getAppReadBufHandler() == null
                || dst != getAppReadBufHandler().getByteBuffer() ) ) {
            throw new IllegalArgumentException ( sm.getString ( "channel.nio.ssl.invalidBuffer" ) );
        }
        if ( closing || closed ) {
            return -1;
        }
        if ( !handshakeComplete ) {
            throw new IllegalStateException ( sm.getString ( "channel.nio.ssl.incompleteHandshake" ) );
        }
        int netread = sc.read ( netInBuffer );
        if ( netread == -1 ) {
            return -1;
        }
        int read = 0;
        SSLEngineResult unwrap;
        do {
            netInBuffer.flip();
            unwrap = sslEngine.unwrap ( netInBuffer, dst );
            netInBuffer.compact();
            if ( unwrap.getStatus() == Status.OK || unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                read += unwrap.bytesProduced();
                if ( unwrap.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                    tasks();
                }
                if ( unwrap.getStatus() == Status.BUFFER_UNDERFLOW ) {
                    break;
                }
            } else if ( unwrap.getStatus() == Status.BUFFER_OVERFLOW ) {
                if ( read > 0 ) {
                    break;
                } else {
                    if ( dst == getBufHandler().getReadBuffer() ) {
                        getBufHandler().expand ( sslEngine.getSession().getApplicationBufferSize() );
                        dst = getBufHandler().getReadBuffer();
                    } else if ( dst == getAppReadBufHandler().getByteBuffer() ) {
                        getAppReadBufHandler()
                        .expand ( sslEngine.getSession().getApplicationBufferSize() );
                        dst = getAppReadBufHandler().getByteBuffer();
                    } else {
                        throw new IOException (
                            sm.getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) );
                    }
                }
            } else {
                throw new IOException ( sm.getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
            }
        } while ( netInBuffer.position() != 0 );
        return read;
    }
    @Override
    public int write ( ByteBuffer src ) throws IOException {
        checkInterruptStatus();
        if ( src == this.netOutBuffer ) {
            int written = sc.write ( src );
            return written;
        } else {
            if ( closing || closed ) {
                throw new IOException ( sm.getString ( "channel.nio.ssl.closing" ) );
            }
            if ( !flush ( netOutBuffer ) ) {
                return 0;
            }
            netOutBuffer.clear();
            SSLEngineResult result = sslEngine.wrap ( src, netOutBuffer );
            int written = result.bytesConsumed();
            netOutBuffer.flip();
            if ( result.getStatus() == Status.OK ) {
                if ( result.getHandshakeStatus() == HandshakeStatus.NEED_TASK ) {
                    tasks();
                }
            } else {
                throw new IOException ( sm.getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
            }
            flush ( netOutBuffer );
            return written;
        }
    }
    @Override
    public int getOutboundRemaining() {
        return netOutBuffer.remaining();
    }
    @Override
    public boolean flushOutbound() throws IOException {
        int remaining = netOutBuffer.remaining();
        flush ( netOutBuffer );
        int remaining2 = netOutBuffer.remaining();
        return remaining2 < remaining;
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
    public SocketChannel getIOChannel() {
        return sc;
    }
}

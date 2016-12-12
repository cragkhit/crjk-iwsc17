package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.jni.File;
import org.apache.tomcat.jni.Library;
import org.apache.tomcat.jni.OS;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.jni.SSLContext.SNICallBack;
import org.apache.tomcat.jni.SSLSocket;
import org.apache.tomcat.jni.Sockaddr;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.jni.Status;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.net.AbstractEndpoint.Acceptor.AcceptorState;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLHostConfig.Type;
import org.apache.tomcat.util.net.openssl.OpenSSLEngine;
public class AprEndpoint extends AbstractEndpoint<Long> implements SNICallBack {
    private static final Log log = LogFactory.getLog ( AprEndpoint.class );
    protected long rootPool = 0;
    protected long serverSock = 0;
    protected long serverSockPool = 0;
    protected long sslContext = 0;
    private final Map<Long, AprSocketWrapper> connections = new ConcurrentHashMap<>();
    public AprEndpoint() {
        setMaxConnections ( 8 * 1024 );
    }
    protected boolean deferAccept = true;
    public void setDeferAccept ( boolean deferAccept ) {
        this.deferAccept = deferAccept;
    }
    @Override
    public boolean getDeferAccept() {
        return deferAccept;
    }
    protected int sendfileSize = 1 * 1024;
    public void setSendfileSize ( int sendfileSize ) {
        this.sendfileSize = sendfileSize;
    }
    public int getSendfileSize() {
        return sendfileSize;
    }
    protected int pollTime = 2000;
    public int getPollTime() {
        return pollTime;
    }
    public void setPollTime ( int pollTime ) {
        if ( pollTime > 0 ) {
            this.pollTime = pollTime;
        }
    }
    private boolean useSendFileSet = false;
    @Override
    public void setUseSendfile ( boolean useSendfile ) {
        useSendFileSet = true;
        super.setUseSendfile ( useSendfile );
    }
    private void setUseSendfileInternal ( boolean useSendfile ) {
        super.setUseSendfile ( useSendfile );
    }
    protected Poller poller = null;
    public Poller getPoller() {
        return poller;
    }
    protected Sendfile sendfile = null;
    public Sendfile getSendfile() {
        return sendfile;
    }
    @Override
    protected Type getSslConfigType() {
        return SSLHostConfig.Type.OPENSSL;
    }
    @Override
    public int getLocalPort() {
        long s = serverSock;
        if ( s == 0 ) {
            return -1;
        } else {
            long sa;
            try {
                sa = Address.get ( Socket.APR_LOCAL, s );
                Sockaddr addr = Address.getInfo ( sa );
                return addr.port;
            } catch ( Exception e ) {
                return -1;
            }
        }
    }
    @Override
    public void setMaxConnections ( int maxConnections ) {
        if ( maxConnections == -1 ) {
            log.warn ( sm.getString ( "endpoint.apr.maxConnections.unlimited",
                                      Integer.valueOf ( getMaxConnections() ) ) );
            return;
        }
        if ( running ) {
            log.warn ( sm.getString ( "endpoint.apr.maxConnections.running",
                                      Integer.valueOf ( getMaxConnections() ) ) );
            return;
        }
        super.setMaxConnections ( maxConnections );
    }
    public int getKeepAliveCount() {
        if ( poller == null ) {
            return 0;
        }
        return poller.getConnectionCount();
    }
    public int getSendfileCount() {
        if ( sendfile == null ) {
            return 0;
        }
        return sendfile.getSendfileCount();
    }
    @Override
    public void bind() throws Exception {
        try {
            rootPool = Pool.create ( 0 );
        } catch ( UnsatisfiedLinkError e ) {
            throw new Exception ( sm.getString ( "endpoint.init.notavail" ) );
        }
        serverSockPool = Pool.create ( rootPool );
        String addressStr = null;
        if ( getAddress() != null ) {
            addressStr = getAddress().getHostAddress();
        }
        int family = Socket.APR_INET;
        if ( Library.APR_HAVE_IPV6 ) {
            if ( addressStr == null ) {
                if ( !OS.IS_BSD && !OS.IS_WIN32 && !OS.IS_WIN64 ) {
                    family = Socket.APR_UNSPEC;
                }
            } else if ( addressStr.indexOf ( ':' ) >= 0 ) {
                family = Socket.APR_UNSPEC;
            }
        }
        long inetAddress = Address.info ( addressStr, family,
                                          getPort(), 0, rootPool );
        serverSock = Socket.create ( Address.getInfo ( inetAddress ).family,
                                     Socket.SOCK_STREAM,
                                     Socket.APR_PROTO_TCP, rootPool );
        if ( OS.IS_UNIX ) {
            Socket.optSet ( serverSock, Socket.APR_SO_REUSEADDR, 1 );
        }
        Socket.optSet ( serverSock, Socket.APR_SO_KEEPALIVE, 1 );
        int ret = Socket.bind ( serverSock, inetAddress );
        if ( ret != 0 ) {
            throw new Exception ( sm.getString ( "endpoint.init.bind", "" + ret, Error.strerror ( ret ) ) );
        }
        ret = Socket.listen ( serverSock, getAcceptCount() );
        if ( ret != 0 ) {
            throw new Exception ( sm.getString ( "endpoint.init.listen", "" + ret, Error.strerror ( ret ) ) );
        }
        if ( OS.IS_WIN32 || OS.IS_WIN64 ) {
            Socket.optSet ( serverSock, Socket.APR_SO_REUSEADDR, 1 );
        }
        if ( !useSendFileSet ) {
            setUseSendfileInternal ( Library.APR_HAS_SENDFILE );
        } else if ( getUseSendfile() && !Library.APR_HAS_SENDFILE ) {
            setUseSendfileInternal ( false );
        }
        if ( acceptorThreadCount == 0 ) {
            acceptorThreadCount = 1;
        }
        if ( deferAccept ) {
            if ( Socket.optSet ( serverSock, Socket.APR_TCP_DEFER_ACCEPT, 1 ) == Status.APR_ENOTIMPL ) {
                deferAccept = false;
            }
        }
        if ( isSSLEnabled() ) {
            for ( SSLHostConfig sslHostConfig : sslHostConfigs.values() ) {
                createSSLContext ( sslHostConfig );
            }
            SSLHostConfig defaultSSLHostConfig = sslHostConfigs.get ( getDefaultSSLHostConfigName() );
            Long defaultSSLContext = defaultSSLHostConfig.getOpenSslContext();
            sslContext = defaultSSLContext.longValue();
            SSLContext.registerDefault ( defaultSSLContext, this );
        }
    }
    @Override
    protected void createSSLContext ( SSLHostConfig sslHostConfig ) throws Exception {
        Set<SSLHostConfigCertificate> certificates = sslHostConfig.getCertificates ( true );
        boolean firstCertificate = true;
        for ( SSLHostConfigCertificate certificate : certificates ) {
            if ( SSLHostConfig.adjustRelativePath ( certificate.getCertificateFile() ) == null ) {
                throw new Exception ( sm.getString ( "endpoint.apr.noSslCertFile" ) );
            }
            if ( firstCertificate ) {
                firstCertificate = false;
                List<String> enabledProtocols = SSLUtilBase.getEnabled ( "protocols", log,
                                                true, sslHostConfig.getProtocols(),
                                                OpenSSLEngine.IMPLEMENTED_PROTOCOLS_SET );
                sslHostConfig.setEnabledProtocols (
                    enabledProtocols.toArray ( new String[enabledProtocols.size()] ) );
                List<String> enabledCiphers = SSLUtilBase.getEnabled ( "ciphers", log,
                                              false, sslHostConfig.getJsseCipherNames(),
                                              OpenSSLEngine.AVAILABLE_CIPHER_SUITES );
                sslHostConfig.setEnabledCiphers (
                    enabledCiphers.toArray ( new String[enabledCiphers.size()] ) );
            }
        }
        if ( certificates.size() > 2 ) {
            throw new Exception ( sm.getString ( "endpoint.apr.tooManyCertFiles" ) );
        }
        int value = SSL.SSL_PROTOCOL_NONE;
        if ( sslHostConfig.getProtocols().size() == 0 ) {
            value = SSL.SSL_PROTOCOL_ALL;
        } else {
            for ( String protocol : sslHostConfig.getEnabledProtocols() ) {
                if ( Constants.SSL_PROTO_SSLv2Hello.equalsIgnoreCase ( protocol ) ) {
                } else if ( Constants.SSL_PROTO_SSLv2.equalsIgnoreCase ( protocol ) ) {
                    value |= SSL.SSL_PROTOCOL_SSLV2;
                } else if ( Constants.SSL_PROTO_SSLv3.equalsIgnoreCase ( protocol ) ) {
                    value |= SSL.SSL_PROTOCOL_SSLV3;
                } else if ( Constants.SSL_PROTO_TLSv1.equalsIgnoreCase ( protocol ) ) {
                    value |= SSL.SSL_PROTOCOL_TLSV1;
                } else if ( Constants.SSL_PROTO_TLSv1_1.equalsIgnoreCase ( protocol ) ) {
                    value |= SSL.SSL_PROTOCOL_TLSV1_1;
                } else if ( Constants.SSL_PROTO_TLSv1_2.equalsIgnoreCase ( protocol ) ) {
                    value |= SSL.SSL_PROTOCOL_TLSV1_2;
                } else {
                    throw new Exception ( sm.getString (
                                              "endpoint.apr.invalidSslProtocol", protocol ) );
                }
            }
        }
        long ctx = 0;
        try {
            ctx = SSLContext.make ( rootPool, value, SSL.SSL_MODE_SERVER );
        } catch ( Exception e ) {
            throw new Exception (
                sm.getString ( "endpoint.apr.failSslContextMake" ), e );
        }
        if ( sslHostConfig.getInsecureRenegotiation() ) {
            SSLContext.setOptions ( ctx, SSL.SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION );
        } else {
            SSLContext.clearOptions ( ctx, SSL.SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION );
        }
        if ( sslHostConfig.getHonorCipherOrder() ) {
            SSLContext.setOptions ( ctx, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE );
        } else {
            SSLContext.clearOptions ( ctx, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE );
        }
        if ( sslHostConfig.getDisableCompression() ) {
            SSLContext.setOptions ( ctx, SSL.SSL_OP_NO_COMPRESSION );
        } else {
            SSLContext.clearOptions ( ctx, SSL.SSL_OP_NO_COMPRESSION );
        }
        if ( sslHostConfig.getDisableSessionTickets() ) {
            SSLContext.setOptions ( ctx, SSL.SSL_OP_NO_TICKET );
        } else {
            SSLContext.clearOptions ( ctx, SSL.SSL_OP_NO_TICKET );
        }
        SSLContext.setCipherSuite ( ctx, sslHostConfig.getCiphers() );
        int idx = 0;
        for ( SSLHostConfigCertificate certificate : sslHostConfig.getCertificates ( true ) ) {
            SSLContext.setCertificate ( ctx,
                                        SSLHostConfig.adjustRelativePath ( certificate.getCertificateFile() ),
                                        SSLHostConfig.adjustRelativePath ( certificate.getCertificateKeyFile() ),
                                        certificate.getCertificateKeyPassword(), idx++ );
            SSLContext.setCertificateChainFile ( ctx,
                                                 SSLHostConfig.adjustRelativePath ( certificate.getCertificateChainFile() ), false );
        }
        SSLContext.setCACertificate ( ctx,
                                      SSLHostConfig.adjustRelativePath ( sslHostConfig.getCaCertificateFile() ),
                                      SSLHostConfig.adjustRelativePath ( sslHostConfig.getCaCertificatePath() ) );
        SSLContext.setCARevocation ( ctx,
                                     SSLHostConfig.adjustRelativePath (
                                         sslHostConfig.getCertificateRevocationListFile() ),
                                     SSLHostConfig.adjustRelativePath (
                                         sslHostConfig.getCertificateRevocationListPath() ) );
        switch ( sslHostConfig.getCertificateVerification() ) {
        case NONE:
            value = SSL.SSL_CVERIFY_NONE;
            break;
        case OPTIONAL:
            value = SSL.SSL_CVERIFY_OPTIONAL;
            break;
        case OPTIONAL_NO_CA:
            value = SSL.SSL_CVERIFY_OPTIONAL_NO_CA;
            break;
        case REQUIRED:
            value = SSL.SSL_CVERIFY_REQUIRE;
            break;
        }
        SSLContext.setVerify ( ctx, value, sslHostConfig.getCertificateVerificationDepth() );
        if ( getUseSendfile() ) {
            setUseSendfileInternal ( false );
            if ( useSendFileSet ) {
                log.warn ( sm.getString ( "endpoint.apr.noSendfileWithSSL" ) );
            }
        }
        if ( negotiableProtocols.size() > 0 ) {
            ArrayList<String> protocols = new ArrayList<>();
            protocols.addAll ( negotiableProtocols );
            protocols.add ( "http/1.1" );
            String[] protocolsArray = protocols.toArray ( new String[0] );
            SSLContext.setAlpnProtos ( ctx, protocolsArray, SSL.SSL_SELECTOR_FAILURE_NO_ADVERTISE );
        }
        sslHostConfig.setOpenSslContext ( Long.valueOf ( ctx ) );
    }
    @Override
    protected void releaseSSLContext ( SSLHostConfig sslHostConfig ) {
        Long ctx = sslHostConfig.getOpenSslContext();
        if ( ctx != null ) {
            SSLContext.free ( ctx.longValue() );
            sslHostConfig.setOpenSslContext ( null );
        }
    }
    @Override
    public long getSslContext ( String sniHostName ) {
        SSLHostConfig sslHostConfig = getSSLHostConfig ( sniHostName );
        Long ctx = sslHostConfig.getOpenSslContext();
        if ( ctx != null ) {
            return ctx.longValue();
        }
        return 0;
    }
    @Override
    public void startInternal() throws Exception {
        if ( !running ) {
            running = true;
            paused = false;
            processorCache = new SynchronizedStack<> ( SynchronizedStack.DEFAULT_SIZE,
                    socketProperties.getProcessorCache() );
            if ( getExecutor() == null ) {
                createExecutor();
            }
            initializeConnectionLatch();
            poller = new Poller();
            poller.init();
            Thread pollerThread = new Thread ( poller, getName() + "-Poller" );
            pollerThread.setPriority ( threadPriority );
            pollerThread.setDaemon ( true );
            pollerThread.start();
            if ( getUseSendfile() ) {
                sendfile = new Sendfile();
                sendfile.init();
                Thread sendfileThread =
                    new Thread ( sendfile, getName() + "-Sendfile" );
                sendfileThread.setPriority ( threadPriority );
                sendfileThread.setDaemon ( true );
                sendfileThread.start();
            }
            startAcceptorThreads();
        }
    }
    @Override
    public void stopInternal() {
        releaseConnectionLatch();
        if ( !paused ) {
            pause();
        }
        if ( running ) {
            running = false;
            poller.stop();
            for ( SocketWrapperBase<Long> socketWrapper : connections.values() ) {
                try {
                    socketWrapper.close();
                    getHandler().release ( socketWrapper );
                } catch ( IOException e ) {
                }
            }
            for ( AbstractEndpoint.Acceptor acceptor : acceptors ) {
                long waitLeft = 10000;
                while ( waitLeft > 0 &&
                        acceptor.getState() != AcceptorState.ENDED &&
                        serverSock != 0 ) {
                    try {
                        Thread.sleep ( 50 );
                    } catch ( InterruptedException e ) {
                    }
                    waitLeft -= 50;
                }
                if ( waitLeft == 0 ) {
                    log.warn ( sm.getString ( "endpoint.warn.unlockAcceptorFailed",
                                              acceptor.getThreadName() ) );
                    if ( serverSock != 0 ) {
                        Socket.shutdown ( serverSock, Socket.APR_SHUTDOWN_READ );
                        serverSock = 0;
                    }
                }
            }
            try {
                poller.destroy();
            } catch ( Exception e ) {
            }
            poller = null;
            connections.clear();
            if ( getUseSendfile() ) {
                try {
                    sendfile.destroy();
                } catch ( Exception e ) {
                }
                sendfile = null;
            }
            processorCache.clear();
        }
        shutdownExecutor();
    }
    @Override
    public void unbind() throws Exception {
        if ( running ) {
            stop();
        }
        if ( serverSockPool != 0 ) {
            Pool.destroy ( serverSockPool );
            serverSockPool = 0;
        }
        if ( serverSock != 0 ) {
            Socket.close ( serverSock );
            serverSock = 0;
        }
        if ( sslContext != 0 ) {
            Long ctx = Long.valueOf ( sslContext );
            SSLContext.unregisterDefault ( ctx );
            for ( SSLHostConfig sslHostConfig : sslHostConfigs.values() ) {
                sslHostConfig.setOpenSslContext ( null );
            }
            sslContext = 0;
        }
        if ( rootPool != 0 ) {
            Pool.destroy ( rootPool );
            rootPool = 0;
        }
        getHandler().recycle();
    }
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    protected boolean setSocketOptions ( SocketWrapperBase<Long> socketWrapper ) {
        long socket = socketWrapper.getSocket().longValue();
        int step = 1;
        try {
            if ( socketProperties.getSoLingerOn() && socketProperties.getSoLingerTime() >= 0 ) {
                Socket.optSet ( socket, Socket.APR_SO_LINGER, socketProperties.getSoLingerTime() );
            }
            if ( socketProperties.getTcpNoDelay() ) {
                Socket.optSet ( socket, Socket.APR_TCP_NODELAY, ( socketProperties.getTcpNoDelay() ? 1 : 0 ) );
            }
            Socket.timeoutSet ( socket, socketProperties.getSoTimeout() * 1000 );
            step = 2;
            if ( sslContext != 0 ) {
                SSLSocket.attach ( sslContext, socket );
                if ( SSLSocket.handshake ( socket ) != 0 ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "endpoint.err.handshake" ) + ": " + SSL.getLastError() );
                    }
                    return false;
                }
                if ( negotiableProtocols.size() > 0 ) {
                    byte[] negotiated = new byte[256];
                    int len = SSLSocket.getALPN ( socket, negotiated );
                    String negotiatedProtocol =
                        new String ( negotiated, 0, len, StandardCharsets.UTF_8 );
                    if ( negotiatedProtocol.length() > 0 ) {
                        socketWrapper.setNegotiatedProtocol ( negotiatedProtocol );
                        if ( log.isDebugEnabled() ) {
                            log.debug ( sm.getString ( "endpoint.alpn.negotiated", negotiatedProtocol ) );
                        }
                    }
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            if ( log.isDebugEnabled() ) {
                if ( step == 2 ) {
                    log.debug ( sm.getString ( "endpoint.err.handshake" ), t );
                } else {
                    log.debug ( sm.getString ( "endpoint.err.unexpected" ), t );
                }
            }
            return false;
        }
        return true;
    }
    protected long allocatePoller ( int size, long pool, int timeout ) {
        try {
            return Poll.create ( size, pool, 0, timeout * 1000 );
        } catch ( Error e ) {
            if ( Status.APR_STATUS_IS_EINVAL ( e.getError() ) ) {
                log.info ( sm.getString ( "endpoint.poll.limitedpollsize", "" + size ) );
                return 0;
            } else {
                log.error ( sm.getString ( "endpoint.poll.initfail" ), e );
                return -1;
            }
        }
    }
    protected boolean processSocketWithOptions ( long socket ) {
        try {
            if ( running ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "endpoint.debug.socket",
                                               Long.valueOf ( socket ) ) );
                }
                AprSocketWrapper wrapper = new AprSocketWrapper ( Long.valueOf ( socket ), this );
                wrapper.setKeepAliveLeft ( getMaxKeepAliveRequests() );
                wrapper.setSecure ( isSSLEnabled() );
                wrapper.setReadTimeout ( getConnectionTimeout() );
                wrapper.setWriteTimeout ( getConnectionTimeout() );
                connections.put ( Long.valueOf ( socket ), wrapper );
                getExecutor().execute ( new SocketWithOptionsProcessor ( wrapper ) );
            }
        } catch ( RejectedExecutionException x ) {
            log.warn ( "Socket processing request was rejected for:" + socket, x );
            return false;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "endpoint.process.fail" ), t );
            return false;
        }
        return true;
    }
    protected boolean processSocket ( long socket, SocketEvent event ) {
        SocketWrapperBase<Long> socketWrapper = connections.get ( Long.valueOf ( socket ) );
        return processSocket ( socketWrapper, event, true );
    }
    @Override
    protected SocketProcessorBase<Long> createSocketProcessor (
        SocketWrapperBase<Long> socketWrapper, SocketEvent event ) {
        return new SocketProcessor ( socketWrapper, event );
    }
    private void closeSocket ( long socket ) {
        SocketWrapperBase<Long> wrapper = connections.remove ( Long.valueOf ( socket ) );
        if ( wrapper != null ) {
            ( ( AprSocketWrapper ) wrapper ).close();
        }
    }
    private void destroySocket ( long socket ) {
        connections.remove ( Long.valueOf ( socket ) );
        if ( log.isDebugEnabled() ) {
            String msg = sm.getString ( "endpoint.debug.destroySocket",
                                        Long.valueOf ( socket ) );
            if ( log.isTraceEnabled() ) {
                log.trace ( msg, new Exception() );
            } else {
                log.debug ( msg );
            }
        }
        if ( socket != 0 ) {
            Socket.destroy ( socket );
            countDownConnection();
        }
    }
    @Override
    protected Log getLog() {
        return log;
    }
    protected class Acceptor extends AbstractEndpoint.Acceptor {
        private final Log log = LogFactory.getLog ( AprEndpoint.Acceptor.class );
        @Override
        public void run() {
            int errorDelay = 0;
            while ( running ) {
                while ( paused && running ) {
                    state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep ( 50 );
                    } catch ( InterruptedException e ) {
                    }
                }
                if ( !running ) {
                    break;
                }
                state = AcceptorState.RUNNING;
                try {
                    countUpOrAwaitConnection();
                    long socket = 0;
                    try {
                        socket = Socket.accept ( serverSock );
                        if ( log.isDebugEnabled() ) {
                            long sa = Address.get ( Socket.APR_REMOTE, socket );
                            Sockaddr addr = Address.getInfo ( sa );
                            log.debug ( sm.getString ( "endpoint.apr.remoteport",
                                                       Long.valueOf ( socket ),
                                                       Long.valueOf ( addr.port ) ) );
                        }
                    } catch ( Exception e ) {
                        countDownConnection();
                        errorDelay = handleExceptionWithDelay ( errorDelay );
                        throw e;
                    }
                    errorDelay = 0;
                    if ( running && !paused ) {
                        if ( !processSocketWithOptions ( socket ) ) {
                            closeSocket ( socket );
                        }
                    } else {
                        destroySocket ( socket );
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    if ( running ) {
                        String msg = sm.getString ( "endpoint.accept.fail" );
                        if ( t instanceof Error ) {
                            Error e = ( Error ) t;
                            if ( e.getError() == 233 ) {
                                log.warn ( msg, t );
                            } else {
                                log.error ( msg, t );
                            }
                        } else {
                            log.error ( msg, t );
                        }
                    }
                }
            }
            state = AcceptorState.ENDED;
        }
    }
    public static class SocketInfo {
        public long socket;
        public long timeout;
        public int flags;
        public boolean read() {
            return ( flags & Poll.APR_POLLIN ) == Poll.APR_POLLIN;
        }
        public boolean write() {
            return ( flags & Poll.APR_POLLOUT ) == Poll.APR_POLLOUT;
        }
        public static int merge ( int flag1, int flag2 ) {
            return ( ( flag1 & Poll.APR_POLLIN ) | ( flag2 & Poll.APR_POLLIN ) )
                   | ( ( flag1 & Poll.APR_POLLOUT ) | ( flag2 & Poll.APR_POLLOUT ) );
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append ( "Socket: [" );
            sb.append ( socket );
            sb.append ( "], timeout: [" );
            sb.append ( timeout );
            sb.append ( "], flags: [" );
            sb.append ( flags );
            return sb.toString();
        }
    }
    public static class SocketTimeouts {
        protected int size;
        protected long[] sockets;
        protected long[] timeouts;
        protected int pos = 0;
        public SocketTimeouts ( int size ) {
            this.size = 0;
            sockets = new long[size];
            timeouts = new long[size];
        }
        public void add ( long socket, long timeout ) {
            sockets[size] = socket;
            timeouts[size] = timeout;
            size++;
        }
        public long remove ( long socket ) {
            long result = 0;
            for ( int i = 0; i < size; i++ ) {
                if ( sockets[i] == socket ) {
                    result = timeouts[i];
                    sockets[i] = sockets[size - 1];
                    timeouts[i] = timeouts[size - 1];
                    size--;
                    break;
                }
            }
            return result;
        }
        public long check ( long date ) {
            while ( pos < size ) {
                if ( date >= timeouts[pos] ) {
                    long result = sockets[pos];
                    sockets[pos] = sockets[size - 1];
                    timeouts[pos] = timeouts[size - 1];
                    size--;
                    return result;
                }
                pos++;
            }
            pos = 0;
            return 0;
        }
    }
    public static class SocketList {
        protected volatile int size;
        protected int pos;
        protected long[] sockets;
        protected long[] timeouts;
        protected int[] flags;
        protected SocketInfo info = new SocketInfo();
        public SocketList ( int size ) {
            this.size = 0;
            pos = 0;
            sockets = new long[size];
            timeouts = new long[size];
            flags = new int[size];
        }
        public int size() {
            return this.size;
        }
        public SocketInfo get() {
            if ( pos == size ) {
                return null;
            } else {
                info.socket = sockets[pos];
                info.timeout = timeouts[pos];
                info.flags = flags[pos];
                pos++;
                return info;
            }
        }
        public void clear() {
            size = 0;
            pos = 0;
        }
        public boolean add ( long socket, long timeout, int flag ) {
            if ( size == sockets.length ) {
                return false;
            } else {
                for ( int i = 0; i < size; i++ ) {
                    if ( sockets[i] == socket ) {
                        flags[i] = SocketInfo.merge ( flags[i], flag );
                        return true;
                    }
                }
                sockets[size] = socket;
                timeouts[size] = timeout;
                flags[size] = flag;
                size++;
                return true;
            }
        }
        public boolean remove ( long socket ) {
            for ( int i = 0; i < size; i++ ) {
                if ( sockets[i] == socket ) {
                    sockets[i] = sockets[size - 1];
                    timeouts[i] = timeouts[size - 1];
                    flags[size] = flags[size - 1];
                    size--;
                    return true;
                }
            }
            return false;
        }
        public void duplicate ( SocketList copy ) {
            copy.size = size;
            copy.pos = pos;
            System.arraycopy ( sockets, 0, copy.sockets, 0, size );
            System.arraycopy ( timeouts, 0, copy.timeouts, 0, size );
            System.arraycopy ( flags, 0, copy.flags, 0, size );
        }
    }
    public class Poller implements Runnable {
        private long[] pollers = null;
        private int actualPollerSize = 0;
        private int[] pollerSpace = null;
        private int pollerCount;
        private int pollerTime;
        private int nextPollerTime;
        private long pool = 0;
        private long[] desc;
        private SocketList addList = null;
        private SocketList closeList = null;
        private SocketTimeouts timeouts = null;
        private long lastMaintain = System.currentTimeMillis();
        private AtomicInteger connectionCount = new AtomicInteger ( 0 );
        public int getConnectionCount() {
            return connectionCount.get();
        }
        private volatile boolean pollerRunning = true;
        protected synchronized void init() {
            pool = Pool.create ( serverSockPool );
            int defaultPollerSize = getMaxConnections();
            if ( ( OS.IS_WIN32 || OS.IS_WIN64 ) && ( defaultPollerSize > 1024 ) ) {
                actualPollerSize = 1024;
            } else {
                actualPollerSize = defaultPollerSize;
            }
            timeouts = new SocketTimeouts ( defaultPollerSize );
            long pollset = allocatePoller ( actualPollerSize, pool, -1 );
            if ( pollset == 0 && actualPollerSize > 1024 ) {
                actualPollerSize = 1024;
                pollset = allocatePoller ( actualPollerSize, pool, -1 );
            }
            if ( pollset == 0 ) {
                actualPollerSize = 62;
                pollset = allocatePoller ( actualPollerSize, pool, -1 );
            }
            pollerCount = defaultPollerSize / actualPollerSize;
            pollerTime = pollTime / pollerCount;
            nextPollerTime = pollerTime;
            pollers = new long[pollerCount];
            pollers[0] = pollset;
            for ( int i = 1; i < pollerCount; i++ ) {
                pollers[i] = allocatePoller ( actualPollerSize, pool, -1 );
            }
            pollerSpace = new int[pollerCount];
            for ( int i = 0; i < pollerCount; i++ ) {
                pollerSpace[i] = actualPollerSize;
            }
            desc = new long[actualPollerSize * 4];
            connectionCount.set ( 0 );
            addList = new SocketList ( defaultPollerSize );
            closeList = new SocketList ( defaultPollerSize );
        }
        protected synchronized void stop() {
            pollerRunning = false;
        }
        protected synchronized void destroy() {
            try {
                this.notify();
                this.wait ( pollerCount * pollTime / 1000 );
            } catch ( InterruptedException e ) {
            }
            SocketInfo info = closeList.get();
            while ( info != null ) {
                addList.remove ( info.socket );
                removeFromPoller ( info.socket );
                destroySocket ( info.socket );
                info = closeList.get();
            }
            closeList.clear();
            info = addList.get();
            while ( info != null ) {
                removeFromPoller ( info.socket );
                destroySocket ( info.socket );
                info = addList.get();
            }
            addList.clear();
            for ( int i = 0; i < pollerCount; i++ ) {
                int rv = Poll.pollset ( pollers[i], desc );
                if ( rv > 0 ) {
                    for ( int n = 0; n < rv; n++ ) {
                        destroySocket ( desc[n * 2 + 1] );
                    }
                }
            }
            Pool.destroy ( pool );
            connectionCount.set ( 0 );
        }
        private void add ( long socket, long timeout, int flags ) {
            if ( log.isDebugEnabled() ) {
                String msg = sm.getString ( "endpoint.debug.pollerAdd",
                                            Long.valueOf ( socket ), Long.valueOf ( timeout ),
                                            Integer.valueOf ( flags ) );
                if ( log.isTraceEnabled() ) {
                    log.trace ( msg, new Exception() );
                } else {
                    log.debug ( msg );
                }
            }
            if ( timeout <= 0 ) {
                timeout = Integer.MAX_VALUE;
            }
            synchronized ( this ) {
                if ( addList.add ( socket, timeout, flags ) ) {
                    this.notify();
                }
            }
        }
        private boolean addToPoller ( long socket, int events ) {
            int rv = -1;
            for ( int i = 0; i < pollers.length; i++ ) {
                if ( pollerSpace[i] > 0 ) {
                    rv = Poll.add ( pollers[i], socket, events );
                    if ( rv == Status.APR_SUCCESS ) {
                        pollerSpace[i]--;
                        connectionCount.incrementAndGet();
                        return true;
                    }
                }
            }
            return false;
        }
        private synchronized void close ( long socket ) {
            closeList.add ( socket, 0, 0 );
            this.notify();
        }
        private void removeFromPoller ( long socket ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "endpoint.debug.pollerRemove",
                                           Long.valueOf ( socket ) ) );
            }
            int rv = -1;
            for ( int i = 0; i < pollers.length; i++ ) {
                if ( pollerSpace[i] < actualPollerSize ) {
                    rv = Poll.remove ( pollers[i], socket );
                    if ( rv != Status.APR_NOTFOUND ) {
                        pollerSpace[i]++;
                        connectionCount.decrementAndGet();
                        if ( log.isDebugEnabled() ) {
                            log.debug ( sm.getString ( "endpoint.debug.pollerRemoved",
                                                       Long.valueOf ( socket ) ) );
                        }
                        break;
                    }
                }
            }
            timeouts.remove ( socket );
        }
        private synchronized void maintain() {
            long date = System.currentTimeMillis();
            if ( ( date - lastMaintain ) < 1000L ) {
                return;
            } else {
                lastMaintain = date;
            }
            long socket = timeouts.check ( date );
            while ( socket != 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "endpoint.debug.socketTimeout",
                                               Long.valueOf ( socket ) ) );
                }
                SocketWrapperBase<Long> socketWrapper = connections.get ( Long.valueOf ( socket ) );
                socketWrapper.setError ( new SocketTimeoutException() );
                processSocket ( socketWrapper, SocketEvent.ERROR, true );
                socket = timeouts.check ( date );
            }
        }
        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append ( "Poller" );
            long[] res = new long[actualPollerSize * 2];
            for ( int i = 0; i < pollers.length; i++ ) {
                int count = Poll.pollset ( pollers[i], res );
                buf.append ( " [ " );
                for ( int j = 0; j < count; j++ ) {
                    buf.append ( desc[2 * j + 1] ).append ( " " );
                }
                buf.append ( "]" );
            }
            return buf.toString();
        }
        @Override
        public void run() {
            SocketList localAddList = new SocketList ( getMaxConnections() );
            SocketList localCloseList = new SocketList ( getMaxConnections() );
            while ( pollerRunning ) {
                while ( pollerRunning && connectionCount.get() < 1 &&
                        addList.size() < 1 && closeList.size() < 1 ) {
                    try {
                        if ( getConnectionTimeout() > 0 && pollerRunning ) {
                            maintain();
                        }
                        synchronized ( this ) {
                            if ( addList.size() < 1 && closeList.size() < 1 ) {
                                this.wait ( 10000 );
                            }
                        }
                    } catch ( InterruptedException e ) {
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                        getLog().warn ( sm.getString ( "endpoint.timeout.err" ) );
                    }
                }
                if ( !pollerRunning ) {
                    break;
                }
                try {
                    synchronized ( this ) {
                        if ( closeList.size() > 0 ) {
                            closeList.duplicate ( localCloseList );
                            closeList.clear();
                        } else {
                            localCloseList.clear();
                        }
                    }
                    synchronized ( this ) {
                        if ( addList.size() > 0 ) {
                            addList.duplicate ( localAddList );
                            addList.clear();
                        } else {
                            localAddList.clear();
                        }
                    }
                    if ( localCloseList.size() > 0 ) {
                        SocketInfo info = localCloseList.get();
                        while ( info != null ) {
                            localAddList.remove ( info.socket );
                            removeFromPoller ( info.socket );
                            destroySocket ( info.socket );
                            info = localCloseList.get();
                        }
                    }
                    if ( localAddList.size() > 0 ) {
                        SocketInfo info = localAddList.get();
                        while ( info != null ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( sm.getString (
                                                "endpoint.debug.pollerAddDo",
                                                Long.valueOf ( info.socket ) ) );
                            }
                            timeouts.remove ( info.socket );
                            AprSocketWrapper wrapper = connections.get (
                                                           Long.valueOf ( info.socket ) );
                            if ( wrapper == null ) {
                                continue;
                            }
                            if ( info.read() || info.write() ) {
                                wrapper.pollerFlags = wrapper.pollerFlags |
                                                      ( info.read() ? Poll.APR_POLLIN : 0 ) |
                                                      ( info.write() ? Poll.APR_POLLOUT : 0 );
                                removeFromPoller ( info.socket );
                                if ( !addToPoller ( info.socket, wrapper.pollerFlags ) ) {
                                    closeSocket ( info.socket );
                                } else {
                                    timeouts.add ( info.socket,
                                                   System.currentTimeMillis() +
                                                   info.timeout );
                                }
                            } else {
                                closeSocket ( info.socket );
                                getLog().warn ( sm.getString (
                                                    "endpoint.apr.pollAddInvalid", info ) );
                            }
                            info = localAddList.get();
                        }
                    }
                    for ( int i = 0; i < pollers.length; i++ ) {
                        boolean reset = false;
                        int rv = 0;
                        if ( pollerSpace[i] < actualPollerSize ) {
                            rv = Poll.poll ( pollers[i], nextPollerTime, desc, true );
                            nextPollerTime = pollerTime;
                        } else {
                            nextPollerTime += pollerTime;
                        }
                        if ( rv > 0 ) {
                            rv = mergeDescriptors ( desc, rv );
                            pollerSpace[i] += rv;
                            connectionCount.addAndGet ( -rv );
                            for ( int n = 0; n < rv; n++ ) {
                                long timeout = timeouts.remove ( desc[n * 2 + 1] );
                                AprSocketWrapper wrapper = connections.get (
                                                               Long.valueOf ( desc[n * 2 + 1] ) );
                                if ( getLog().isDebugEnabled() ) {
                                    log.debug ( sm.getString (
                                                    "endpoint.debug.pollerProcess",
                                                    Long.valueOf ( desc[n * 2 + 1] ),
                                                    Long.valueOf ( desc[n * 2] ) ) );
                                }
                                wrapper.pollerFlags = wrapper.pollerFlags & ~ ( ( int ) desc[n * 2] );
                                if ( ( ( desc[n * 2] & Poll.APR_POLLHUP ) == Poll.APR_POLLHUP )
                                        || ( ( desc[n * 2] & Poll.APR_POLLERR ) == Poll.APR_POLLERR )
                                        || ( ( desc[n * 2] & Poll.APR_POLLNVAL ) == Poll.APR_POLLNVAL ) ) {
                                    if ( ( desc[n * 2] & Poll.APR_POLLIN ) == Poll.APR_POLLIN ) {
                                        if ( !processSocket ( desc[n * 2 + 1], SocketEvent.OPEN_READ ) ) {
                                            closeSocket ( desc[n * 2 + 1] );
                                        }
                                    } else if ( ( desc[n * 2] & Poll.APR_POLLOUT ) == Poll.APR_POLLOUT ) {
                                        if ( !processSocket ( desc[n * 2 + 1], SocketEvent.OPEN_WRITE ) ) {
                                            closeSocket ( desc[n * 2 + 1] );
                                        }
                                    } else if ( ( wrapper.pollerFlags & Poll.APR_POLLIN ) == Poll.APR_POLLIN ) {
                                        if ( !processSocket ( desc[n * 2 + 1], SocketEvent.OPEN_READ ) ) {
                                            closeSocket ( desc[n * 2 + 1] );
                                        }
                                    } else if ( ( wrapper.pollerFlags & Poll.APR_POLLOUT ) == Poll.APR_POLLOUT ) {
                                        if ( !processSocket ( desc[n * 2 + 1], SocketEvent.OPEN_WRITE ) ) {
                                            closeSocket ( desc[n * 2 + 1] );
                                        }
                                    } else {
                                        closeSocket ( desc[n * 2 + 1] );
                                    }
                                } else if ( ( ( desc[n * 2] & Poll.APR_POLLIN ) == Poll.APR_POLLIN )
                                            || ( ( desc[n * 2] & Poll.APR_POLLOUT ) == Poll.APR_POLLOUT ) ) {
                                    boolean error = false;
                                    if ( ( ( desc[n * 2] & Poll.APR_POLLIN ) == Poll.APR_POLLIN ) &&
                                            !processSocket ( desc[n * 2 + 1], SocketEvent.OPEN_READ ) ) {
                                        error = true;
                                        closeSocket ( desc[n * 2 + 1] );
                                    }
                                    if ( !error &&
                                            ( ( desc[n * 2] & Poll.APR_POLLOUT ) == Poll.APR_POLLOUT ) &&
                                            !processSocket ( desc[n * 2 + 1], SocketEvent.OPEN_WRITE ) ) {
                                        error = true;
                                        closeSocket ( desc[n * 2 + 1] );
                                    }
                                    if ( !error && wrapper.pollerFlags != 0 ) {
                                        if ( timeout > 0 ) {
                                            timeout = timeout - System.currentTimeMillis();
                                        }
                                        if ( timeout <= 0 ) {
                                            timeout = 1;
                                        }
                                        if ( timeout > Integer.MAX_VALUE ) {
                                            timeout = Integer.MAX_VALUE;
                                        }
                                        add ( desc[n * 2 + 1], ( int ) timeout, wrapper.pollerFlags );
                                    }
                                } else {
                                    getLog().warn ( sm.getString (
                                                        "endpoint.apr.pollUnknownEvent",
                                                        Long.valueOf ( desc[n * 2] ) ) );
                                    closeSocket ( desc[n * 2 + 1] );
                                }
                            }
                        } else if ( rv < 0 ) {
                            int errn = -rv;
                            if ( ( errn != Status.TIMEUP ) && ( errn != Status.EINTR ) ) {
                                if ( errn >  Status.APR_OS_START_USERERR ) {
                                    errn -=  Status.APR_OS_START_USERERR;
                                }
                                getLog().error ( sm.getString (
                                                     "endpoint.apr.pollError",
                                                     Integer.valueOf ( errn ),
                                                     Error.strerror ( errn ) ) );
                                reset = true;
                            }
                        }
                        if ( reset && pollerRunning ) {
                            int count = Poll.pollset ( pollers[i], desc );
                            long newPoller = allocatePoller ( actualPollerSize, pool, -1 );
                            pollerSpace[i] = actualPollerSize;
                            connectionCount.addAndGet ( -count );
                            Poll.destroy ( pollers[i] );
                            pollers[i] = newPoller;
                        }
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    getLog().warn ( sm.getString ( "endpoint.poll.error" ), t );
                }
                try {
                    if ( getConnectionTimeout() > 0 && pollerRunning ) {
                        maintain();
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    getLog().warn ( sm.getString ( "endpoint.timeout.err" ), t );
                }
            }
            synchronized ( this ) {
                this.notifyAll();
            }
        }
        private int mergeDescriptors ( long[] desc, int startCount ) {
            HashMap<Long, Long> merged = new HashMap<> ( startCount );
            for ( int n = 0; n < startCount; n++ ) {
                Long newValue = merged.merge ( Long.valueOf ( desc[2 * n + 1] ), Long.valueOf ( desc[2 * n] ),
                                               ( v1, v2 ) -> Long.valueOf ( v1.longValue() | v2.longValue() ) );
                if ( log.isDebugEnabled() ) {
                    if ( newValue.longValue() != desc[2 * n] ) {
                        log.debug ( sm.getString ( "endpoint.apr.pollMergeEvents",
                                                   Long.valueOf ( desc[2 * n + 1] ), Long.valueOf ( desc[2 * n] ), newValue ) );
                    }
                }
            }
            int i = 0;
            for ( Map.Entry<Long, Long> entry : merged.entrySet() ) {
                desc[i++] = entry.getValue().longValue();
                desc[i++] = entry.getKey().longValue();
            }
            return merged.size();
        }
    }
    public static class SendfileData extends SendfileDataBase {
        protected long fd;
        protected long fdpool;
        protected long socket;
        public SendfileData ( String filename, long pos, long length ) {
            super ( filename, pos, length );
        }
    }
    public class Sendfile implements Runnable {
        protected long sendfilePollset = 0;
        protected long pool = 0;
        protected long[] desc;
        protected HashMap<Long, SendfileData> sendfileData;
        protected int sendfileCount;
        public int getSendfileCount() {
            return sendfileCount;
        }
        protected ArrayList<SendfileData> addS;
        private volatile boolean sendfileRunning = true;
        protected void init() {
            pool = Pool.create ( serverSockPool );
            int size = sendfileSize;
            if ( size <= 0 ) {
                size = ( OS.IS_WIN32 || OS.IS_WIN64 ) ? ( 1 * 1024 ) : ( 16 * 1024 );
            }
            sendfilePollset = allocatePoller ( size, pool, getConnectionTimeout() );
            if ( sendfilePollset == 0 && size > 1024 ) {
                size = 1024;
                sendfilePollset = allocatePoller ( size, pool, getConnectionTimeout() );
            }
            if ( sendfilePollset == 0 ) {
                size = 62;
                sendfilePollset = allocatePoller ( size, pool, getConnectionTimeout() );
            }
            desc = new long[size * 2];
            sendfileData = new HashMap<> ( size );
            addS = new ArrayList<>();
        }
        protected void destroy() {
            sendfileRunning = false;
            try {
                synchronized ( this ) {
                    this.notify();
                    this.wait ( pollTime / 1000 );
                }
            } catch ( InterruptedException e ) {
            }
            for ( int i = ( addS.size() - 1 ); i >= 0; i-- ) {
                SendfileData data = addS.get ( i );
                closeSocket ( data.socket );
            }
            int rv = Poll.pollset ( sendfilePollset, desc );
            if ( rv > 0 ) {
                for ( int n = 0; n < rv; n++ ) {
                    closeSocket ( desc[n * 2 + 1] );
                }
            }
            Pool.destroy ( pool );
            sendfileData.clear();
        }
        public SendfileState add ( SendfileData data ) {
            try {
                data.fdpool = Socket.pool ( data.socket );
                data.fd = File.open
                          ( data.fileName, File.APR_FOPEN_READ
                            | File.APR_FOPEN_SENDFILE_ENABLED | File.APR_FOPEN_BINARY,
                            0, data.fdpool );
                Socket.timeoutSet ( data.socket, 0 );
                while ( true ) {
                    long nw = Socket.sendfilen ( data.socket, data.fd,
                                                 data.pos, data.length, 0 );
                    if ( nw < 0 ) {
                        if ( ! ( -nw == Status.EAGAIN ) ) {
                            Pool.destroy ( data.fdpool );
                            data.socket = 0;
                            return SendfileState.ERROR;
                        } else {
                            break;
                        }
                    } else {
                        data.pos += nw;
                        data.length -= nw;
                        if ( data.length == 0 ) {
                            Pool.destroy ( data.fdpool );
                            Socket.timeoutSet ( data.socket, getConnectionTimeout() * 1000 );
                            return SendfileState.DONE;
                        }
                    }
                }
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.sendfile.error" ), e );
                return SendfileState.ERROR;
            }
            synchronized ( this ) {
                addS.add ( data );
                this.notify();
            }
            return SendfileState.PENDING;
        }
        protected void remove ( SendfileData data ) {
            int rv = Poll.remove ( sendfilePollset, data.socket );
            if ( rv == Status.APR_SUCCESS ) {
                sendfileCount--;
            }
            sendfileData.remove ( Long.valueOf ( data.socket ) );
        }
        @Override
        public void run() {
            long maintainTime = 0;
            while ( sendfileRunning ) {
                while ( sendfileRunning && paused ) {
                    try {
                        Thread.sleep ( 1000 );
                    } catch ( InterruptedException e ) {
                    }
                }
                while ( sendfileRunning && sendfileCount < 1 && addS.size() < 1 ) {
                    maintainTime = 0;
                    try {
                        synchronized ( this ) {
                            this.wait();
                        }
                    } catch ( InterruptedException e ) {
                    }
                }
                if ( !sendfileRunning ) {
                    break;
                }
                try {
                    if ( addS.size() > 0 ) {
                        synchronized ( this ) {
                            for ( int i = ( addS.size() - 1 ); i >= 0; i-- ) {
                                SendfileData data = addS.get ( i );
                                int rv = Poll.add ( sendfilePollset, data.socket, Poll.APR_POLLOUT );
                                if ( rv == Status.APR_SUCCESS ) {
                                    sendfileData.put ( Long.valueOf ( data.socket ), data );
                                    sendfileCount++;
                                } else {
                                    getLog().warn ( sm.getString (
                                                        "endpoint.sendfile.addfail",
                                                        Integer.valueOf ( rv ),
                                                        Error.strerror ( rv ) ) );
                                    closeSocket ( data.socket );
                                }
                            }
                            addS.clear();
                        }
                    }
                    maintainTime += pollTime;
                    int rv = Poll.poll ( sendfilePollset, pollTime, desc, false );
                    if ( rv > 0 ) {
                        for ( int n = 0; n < rv; n++ ) {
                            SendfileData state =
                                sendfileData.get ( Long.valueOf ( desc[n * 2 + 1] ) );
                            if ( ( ( desc[n * 2] & Poll.APR_POLLHUP ) == Poll.APR_POLLHUP )
                                    || ( ( desc[n * 2] & Poll.APR_POLLERR ) == Poll.APR_POLLERR ) ) {
                                remove ( state );
                                closeSocket ( state.socket );
                                continue;
                            }
                            long nw = Socket.sendfilen ( state.socket, state.fd,
                                                         state.pos,
                                                         state.length, 0 );
                            if ( nw < 0 ) {
                                remove ( state );
                                closeSocket ( state.socket );
                                continue;
                            }
                            state.pos += nw;
                            state.length -= nw;
                            if ( state.length == 0 ) {
                                remove ( state );
                                if ( state.keepAlive ) {
                                    Pool.destroy ( state.fdpool );
                                    Socket.timeoutSet ( state.socket,
                                                        getConnectionTimeout() * 1000 );
                                    getPoller().add (
                                        state.socket, getKeepAliveTimeout(),
                                        Poll.APR_POLLIN );
                                } else {
                                    closeSocket ( state.socket );
                                }
                            }
                        }
                    } else if ( rv < 0 ) {
                        int errn = -rv;
                        if ( ( errn != Status.TIMEUP ) && ( errn != Status.EINTR ) ) {
                            if ( errn >  Status.APR_OS_START_USERERR ) {
                                errn -=  Status.APR_OS_START_USERERR;
                            }
                            getLog().error ( sm.getString (
                                                 "endpoint.apr.pollError",
                                                 Integer.valueOf ( errn ),
                                                 Error.strerror ( errn ) ) );
                            synchronized ( this ) {
                                destroy();
                                init();
                            }
                            continue;
                        }
                    }
                    if ( getConnectionTimeout() > 0 &&
                            maintainTime > 1000000L && sendfileRunning ) {
                        rv = Poll.maintain ( sendfilePollset, desc, false );
                        maintainTime = 0;
                        if ( rv > 0 ) {
                            for ( int n = 0; n < rv; n++ ) {
                                SendfileData state = sendfileData.get ( Long.valueOf ( desc[n] ) );
                                remove ( state );
                                closeSocket ( state.socket );
                            }
                        }
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    getLog().error ( sm.getString ( "endpoint.poll.error" ), t );
                }
            }
            synchronized ( this ) {
                this.notifyAll();
            }
        }
    }
    protected class SocketWithOptionsProcessor implements Runnable {
        protected SocketWrapperBase<Long> socket = null;
        public SocketWithOptionsProcessor ( SocketWrapperBase<Long> socket ) {
            this.socket = socket;
        }
        @Override
        public void run() {
            synchronized ( socket ) {
                if ( !deferAccept ) {
                    if ( setSocketOptions ( socket ) ) {
                        getPoller().add ( socket.getSocket().longValue(),
                                          getConnectionTimeout(), Poll.APR_POLLIN );
                    } else {
                        closeSocket ( socket.getSocket().longValue() );
                        socket = null;
                    }
                } else {
                    if ( !setSocketOptions ( socket ) ) {
                        closeSocket ( socket.getSocket().longValue() );
                        socket = null;
                        return;
                    }
                    Handler.SocketState state = getHandler().process ( socket,
                                                SocketEvent.OPEN_READ );
                    if ( state == Handler.SocketState.CLOSED ) {
                        closeSocket ( socket.getSocket().longValue() );
                        socket = null;
                    }
                }
            }
        }
    }
    protected class SocketProcessor extends  SocketProcessorBase<Long> {
        public SocketProcessor ( SocketWrapperBase<Long> socketWrapper, SocketEvent event ) {
            super ( socketWrapper, event );
        }
        @Override
        protected void doRun() {
            try {
                SocketState state = getHandler().process ( socketWrapper, event );
                if ( state == Handler.SocketState.CLOSED ) {
                    closeSocket ( socketWrapper.getSocket().longValue() );
                }
            } finally {
                socketWrapper = null;
                event = null;
                if ( running && !paused ) {
                    processorCache.push ( this );
                }
            }
        }
    }
    public static class AprSocketWrapper extends SocketWrapperBase<Long> {
        private static final int SSL_OUTPUT_BUFFER_SIZE = 8192;
        private final ByteBuffer sslOutputBuffer;
        private final Object closedLock = new Object();
        private volatile boolean closed = false;
        private int pollerFlags = 0;
        public AprSocketWrapper ( Long socket, AprEndpoint endpoint ) {
            super ( socket, endpoint );
            if ( endpoint.isSSLEnabled() ) {
                sslOutputBuffer = ByteBuffer.allocateDirect ( SSL_OUTPUT_BUFFER_SIZE );
                sslOutputBuffer.position ( SSL_OUTPUT_BUFFER_SIZE );
            } else {
                sslOutputBuffer = null;
            }
            socketBufferHandler = new SocketBufferHandler ( 6 * 1500, 6 * 1500, true );
        }
        @Override
        public int read ( boolean block, byte[] b, int off, int len ) throws IOException {
            int nRead = populateReadBuffer ( b, off, len );
            if ( nRead > 0 ) {
                return nRead;
            }
            nRead = fillReadBuffer ( block );
            if ( nRead > 0 ) {
                socketBufferHandler.configureReadBufferForRead();
                nRead = Math.min ( nRead, len );
                socketBufferHandler.getReadBuffer().get ( b, off, nRead );
            }
            return nRead;
        }
        @Override
        public int read ( boolean block, ByteBuffer to ) throws IOException {
            int nRead = populateReadBuffer ( to );
            if ( nRead > 0 ) {
                return nRead;
            }
            int limit = socketBufferHandler.getReadBuffer().capacity();
            if ( to.isDirect() && to.remaining() >= limit ) {
                to.limit ( to.position() + limit );
                nRead = fillReadBuffer ( block, to );
            } else {
                nRead = fillReadBuffer ( block );
                if ( nRead > 0 ) {
                    nRead = populateReadBuffer ( to );
                }
            }
            return nRead;
        }
        private int fillReadBuffer ( boolean block ) throws IOException {
            socketBufferHandler.configureReadBufferForWrite();
            return fillReadBuffer ( block, socketBufferHandler.getReadBuffer() );
        }
        private int fillReadBuffer ( boolean block, ByteBuffer to ) throws IOException {
            if ( closed ) {
                throw new IOException ( sm.getString ( "socket.apr.closed", getSocket() ) );
            }
            Lock readLock = getBlockingStatusReadLock();
            WriteLock writeLock = getBlockingStatusWriteLock();
            boolean readDone = false;
            int result = 0;
            readLock.lock();
            try {
                if ( getBlockingStatus() == block ) {
                    if ( block ) {
                        Socket.timeoutSet ( getSocket().longValue(), getReadTimeout() * 1000 );
                    }
                    result = Socket.recvb ( getSocket().longValue(), to, to.position(),
                                            to.remaining() );
                    readDone = true;
                }
            } finally {
                readLock.unlock();
            }
            if ( !readDone ) {
                writeLock.lock();
                try {
                    setBlockingStatus ( block );
                    if ( block ) {
                        Socket.timeoutSet ( getSocket().longValue(), getReadTimeout() * 1000 );
                    } else {
                        Socket.timeoutSet ( getSocket().longValue(), 0 );
                    }
                    readLock.lock();
                    try {
                        writeLock.unlock();
                        result = Socket.recvb ( getSocket().longValue(), to, to.position(),
                                                to.remaining() );
                    } finally {
                        readLock.unlock();
                    }
                } finally {
                    if ( writeLock.isHeldByCurrentThread() ) {
                        writeLock.unlock();
                    }
                }
            }
            if ( result > 0 ) {
                to.position ( to.position() + result );
                return result;
            } else if ( result == 0 || -result == Status.EAGAIN ) {
                return 0;
            } else if ( -result == Status.APR_EGENERAL && isSecure() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "socket.apr.read.sslGeneralError", getSocket(), this ) );
                }
                return 0;
            } else if ( ( -result ) == Status.ETIMEDOUT || ( -result ) == Status.TIMEUP ) {
                if ( block ) {
                    throw new SocketTimeoutException ( sm.getString ( "iib.readtimeout" ) );
                } else {
                    return 0;
                }
            } else if ( -result == Status.APR_EOF ) {
                return -1;
            } else if ( ( OS.IS_WIN32 || OS.IS_WIN64 ) &&
                        ( -result == Status.APR_OS_START_SYSERR + 10053 ) ) {
                throw new EOFException ( sm.getString ( "socket.apr.clientAbort" ) );
            } else {
                throw new IOException ( sm.getString ( "socket.apr.read.error",
                                                       Integer.valueOf ( -result ), getSocket(), this ) );
            }
        }
        @Override
        public boolean isReadyForRead() throws IOException {
            socketBufferHandler.configureReadBufferForRead();
            if ( socketBufferHandler.getReadBuffer().remaining() > 0 ) {
                return true;
            }
            fillReadBuffer ( false );
            boolean isReady = socketBufferHandler.getReadBuffer().position() > 0;
            return isReady;
        }
        @Override
        public void close() {
            synchronized ( closedLock ) {
                if ( closed ) {
                    return;
                }
                closed = true;
                if ( sslOutputBuffer != null ) {
                    ByteBufferUtils.cleanDirectBuffer ( sslOutputBuffer );
                }
                ( ( AprEndpoint ) getEndpoint() ).getPoller().close ( getSocket().longValue() );
            }
        }
        @Override
        public boolean isClosed() {
            synchronized ( closedLock ) {
                return closed;
            }
        }
        @Override
        protected void writeByteBufferBlocking ( ByteBuffer from ) throws IOException {
            if ( from.isDirect() ) {
                super.writeByteBufferBlocking ( from );
            } else {
                ByteBuffer writeBuffer = socketBufferHandler.getWriteBuffer();
                int limit = writeBuffer.capacity();
                while ( from.remaining() >= limit ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    transfer ( from, writeBuffer );
                    doWrite ( true );
                }
                if ( from.remaining() > 0 ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    transfer ( from, writeBuffer );
                }
            }
        }
        @Override
        protected boolean writeByteBufferNonBlocking ( ByteBuffer from ) throws IOException {
            if ( from.isDirect() ) {
                return super.writeByteBufferNonBlocking ( from );
            } else {
                ByteBuffer writeBuffer = socketBufferHandler.getWriteBuffer();
                int limit = writeBuffer.capacity();
                while ( from.remaining() >= limit ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    transfer ( from, writeBuffer );
                    int newPosition = writeBuffer.position() + limit;
                    doWrite ( false );
                    if ( writeBuffer.position() != newPosition ) {
                        return true;
                    }
                }
                if ( from.remaining() > 0 ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    transfer ( from, writeBuffer );
                }
                return false;
            }
        }
        @Override
        protected void doWrite ( boolean block, ByteBuffer from ) throws IOException {
            if ( closed ) {
                throw new IOException ( sm.getString ( "socket.apr.closed", getSocket() ) );
            }
            Lock readLock = getBlockingStatusReadLock();
            WriteLock writeLock = getBlockingStatusWriteLock();
            readLock.lock();
            try {
                if ( getBlockingStatus() == block ) {
                    if ( block ) {
                        Socket.timeoutSet ( getSocket().longValue(), getWriteTimeout() * 1000 );
                    }
                    doWriteInternal ( from );
                    return;
                }
            } finally {
                readLock.unlock();
            }
            writeLock.lock();
            try {
                setBlockingStatus ( block );
                if ( block ) {
                    Socket.timeoutSet ( getSocket().longValue(), getWriteTimeout() * 1000 );
                } else {
                    Socket.timeoutSet ( getSocket().longValue(), 0 );
                }
                readLock.lock();
                try {
                    writeLock.unlock();
                    doWriteInternal ( from );
                } finally {
                    readLock.unlock();
                }
            } finally {
                if ( writeLock.isHeldByCurrentThread() ) {
                    writeLock.unlock();
                }
            }
        }
        private void doWriteInternal ( ByteBuffer from ) throws IOException {
            int thisTime;
            do {
                thisTime = 0;
                if ( getEndpoint().isSSLEnabled() ) {
                    if ( sslOutputBuffer.remaining() == 0 ) {
                        sslOutputBuffer.clear();
                        transfer ( from, sslOutputBuffer );
                        sslOutputBuffer.flip();
                    } else {
                    }
                    thisTime = Socket.sendb ( getSocket().longValue(), sslOutputBuffer,
                                              sslOutputBuffer.position(), sslOutputBuffer.limit() );
                    if ( thisTime > 0 ) {
                        sslOutputBuffer.position ( sslOutputBuffer.position() + thisTime );
                    }
                } else {
                    thisTime = Socket.sendb ( getSocket().longValue(), from, from.position(),
                                              from.remaining() );
                    if ( thisTime > 0 ) {
                        from.position ( from.position() + thisTime );
                    }
                }
                if ( Status.APR_STATUS_IS_EAGAIN ( -thisTime ) ) {
                    thisTime = 0;
                } else if ( -thisTime == Status.APR_EOF ) {
                    throw new EOFException ( sm.getString ( "socket.apr.clientAbort" ) );
                } else if ( ( OS.IS_WIN32 || OS.IS_WIN64 ) &&
                            ( -thisTime == Status.APR_OS_START_SYSERR + 10053 ) ) {
                    throw new EOFException ( sm.getString ( "socket.apr.clientAbort" ) );
                } else if ( thisTime < 0 ) {
                    throw new IOException ( sm.getString ( "socket.apr.write.error",
                                                           Integer.valueOf ( -thisTime ), getSocket(), this ) );
                }
            } while ( ( thisTime > 0 || getBlockingStatus() ) && from.hasRemaining() );
        }
        @Override
        public void registerReadInterest() {
            synchronized ( closedLock ) {
                if ( closed ) {
                    return;
                }
                Poller p = ( ( AprEndpoint ) getEndpoint() ).getPoller();
                if ( p != null ) {
                    p.add ( getSocket().longValue(), getReadTimeout(), Poll.APR_POLLIN );
                }
            }
        }
        @Override
        public void registerWriteInterest() {
            synchronized ( closedLock ) {
                if ( closed ) {
                    return;
                }
                ( ( AprEndpoint ) getEndpoint() ).getPoller().add (
                    getSocket().longValue(), getWriteTimeout(), Poll.APR_POLLOUT );
            }
        }
        @Override
        public SendfileDataBase createSendfileData ( String filename, long pos, long length ) {
            return new SendfileData ( filename, pos, length );
        }
        @Override
        public SendfileState processSendfile ( SendfileDataBase sendfileData ) {
            ( ( SendfileData ) sendfileData ).socket = getSocket().longValue();
            return ( ( AprEndpoint ) getEndpoint() ).getSendfile().add ( ( SendfileData ) sendfileData );
        }
        @Override
        protected void populateRemoteAddr() {
            if ( closed ) {
                return;
            }
            try {
                long socket = getSocket().longValue();
                long sa = Address.get ( Socket.APR_REMOTE, socket );
                remoteAddr = Address.getip ( sa );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.warn.noRemoteAddr", getSocket() ), e );
            }
        }
        @Override
        protected void populateRemoteHost() {
            if ( closed ) {
                return;
            }
            try {
                long socket = getSocket().longValue();
                long sa = Address.get ( Socket.APR_REMOTE, socket );
                remoteHost = Address.getnameinfo ( sa, 0 );
                if ( remoteAddr == null ) {
                    remoteAddr = Address.getip ( sa );
                }
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.warn.noRemoteHost", getSocket() ), e );
            }
        }
        @Override
        protected void populateRemotePort() {
            if ( closed ) {
                return;
            }
            try {
                long socket = getSocket().longValue();
                long sa = Address.get ( Socket.APR_REMOTE, socket );
                Sockaddr addr = Address.getInfo ( sa );
                remotePort = addr.port;
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.warn.noRemotePort", getSocket() ), e );
            }
        }
        @Override
        protected void populateLocalName() {
            if ( closed ) {
                return;
            }
            try {
                long socket = getSocket().longValue();
                long sa = Address.get ( Socket.APR_LOCAL, socket );
                localName = Address.getnameinfo ( sa, 0 );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.warn.noLocalName" ), e );
            }
        }
        @Override
        protected void populateLocalAddr() {
            if ( closed ) {
                return;
            }
            try {
                long socket = getSocket().longValue();
                long sa = Address.get ( Socket.APR_LOCAL, socket );
                localAddr = Address.getip ( sa );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.warn.noLocalAddr" ), e );
            }
        }
        @Override
        protected void populateLocalPort() {
            if ( closed ) {
                return;
            }
            try {
                long socket = getSocket().longValue();
                long sa = Address.get ( Socket.APR_LOCAL, socket );
                Sockaddr addr = Address.getInfo ( sa );
                localPort = addr.port;
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "endpoint.warn.noLocalPort" ), e );
            }
        }
        @Override
        public SSLSupport getSslSupport ( String clientCertProvider ) {
            if ( getEndpoint().isSSLEnabled() ) {
                return new  AprSSLSupport ( this, clientCertProvider );
            } else {
                return null;
            }
        }
        @Override
        public void doClientAuth ( SSLSupport sslSupport ) {
            long socket = getSocket().longValue();
            SSLSocket.setVerify ( socket, SSL.SSL_CVERIFY_REQUIRE, -1 );
            SSLSocket.renegotiate ( socket );
        }
        @Override
        public void setAppReadBufHandler ( ApplicationBufferHandler handler ) {
        }
    }
}

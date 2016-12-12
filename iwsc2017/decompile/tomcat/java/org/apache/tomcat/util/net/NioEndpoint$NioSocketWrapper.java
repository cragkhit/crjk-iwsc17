package org.apache.tomcat.util.net;
import javax.net.ssl.SSLEngine;
import org.apache.tomcat.util.net.jsse.JSSESupport;
import javax.net.ssl.SSLSession;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
public static class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
    private final NioSelectorPool pool;
    private Poller poller;
    private int interestOps;
    private CountDownLatch readLatch;
    private CountDownLatch writeLatch;
    private volatile SendfileData sendfileData;
    private volatile long lastRead;
    private volatile long lastWrite;
    public NioSocketWrapper ( final NioChannel channel, final NioEndpoint endpoint ) {
        super ( channel, endpoint );
        this.poller = null;
        this.interestOps = 0;
        this.readLatch = null;
        this.writeLatch = null;
        this.sendfileData = null;
        this.lastRead = System.currentTimeMillis();
        this.lastWrite = this.lastRead;
        this.pool = endpoint.getSelectorPool();
        this.socketBufferHandler = channel.getBufHandler();
    }
    public Poller getPoller() {
        return this.poller;
    }
    public void setPoller ( final Poller poller ) {
        this.poller = poller;
    }
    public int interestOps() {
        return this.interestOps;
    }
    public int interestOps ( final int ops ) {
        return this.interestOps = ops;
    }
    public CountDownLatch getReadLatch() {
        return this.readLatch;
    }
    public CountDownLatch getWriteLatch() {
        return this.writeLatch;
    }
    protected CountDownLatch resetLatch ( final CountDownLatch latch ) {
        if ( latch == null || latch.getCount() == 0L ) {
            return null;
        }
        throw new IllegalStateException ( "Latch must be at count 0" );
    }
    public void resetReadLatch() {
        this.readLatch = this.resetLatch ( this.readLatch );
    }
    public void resetWriteLatch() {
        this.writeLatch = this.resetLatch ( this.writeLatch );
    }
    protected CountDownLatch startLatch ( final CountDownLatch latch, final int cnt ) {
        if ( latch == null || latch.getCount() == 0L ) {
            return new CountDownLatch ( cnt );
        }
        throw new IllegalStateException ( "Latch must be at count 0 or null." );
    }
    public void startReadLatch ( final int cnt ) {
        this.readLatch = this.startLatch ( this.readLatch, cnt );
    }
    public void startWriteLatch ( final int cnt ) {
        this.writeLatch = this.startLatch ( this.writeLatch, cnt );
    }
    protected void awaitLatch ( final CountDownLatch latch, final long timeout, final TimeUnit unit ) throws InterruptedException {
        if ( latch == null ) {
            throw new IllegalStateException ( "Latch cannot be null" );
        }
        latch.await ( timeout, unit );
    }
    public void awaitReadLatch ( final long timeout, final TimeUnit unit ) throws InterruptedException {
        this.awaitLatch ( this.readLatch, timeout, unit );
    }
    public void awaitWriteLatch ( final long timeout, final TimeUnit unit ) throws InterruptedException {
        this.awaitLatch ( this.writeLatch, timeout, unit );
    }
    public void setSendfileData ( final SendfileData sf ) {
        this.sendfileData = sf;
    }
    public SendfileData getSendfileData() {
        return this.sendfileData;
    }
    public void updateLastWrite() {
        this.lastWrite = System.currentTimeMillis();
    }
    public long getLastWrite() {
        return this.lastWrite;
    }
    public void updateLastRead() {
        this.lastRead = System.currentTimeMillis();
    }
    public long getLastRead() {
        return this.lastRead;
    }
    @Override
    public boolean isReadyForRead() throws IOException {
        this.socketBufferHandler.configureReadBufferForRead();
        if ( this.socketBufferHandler.getReadBuffer().remaining() > 0 ) {
            return true;
        }
        this.fillReadBuffer ( false );
        final boolean isReady = this.socketBufferHandler.getReadBuffer().position() > 0;
        return isReady;
    }
    @Override
    public int read ( final boolean block, final byte[] b, final int off, final int len ) throws IOException {
        int nRead = this.populateReadBuffer ( b, off, len );
        if ( nRead > 0 ) {
            return nRead;
        }
        nRead = this.fillReadBuffer ( block );
        this.updateLastRead();
        if ( nRead > 0 ) {
            this.socketBufferHandler.configureReadBufferForRead();
            nRead = Math.min ( nRead, len );
            this.socketBufferHandler.getReadBuffer().get ( b, off, nRead );
        }
        return nRead;
    }
    @Override
    public int read ( final boolean block, final ByteBuffer to ) throws IOException {
        int nRead = this.populateReadBuffer ( to );
        if ( nRead > 0 ) {
            return nRead;
        }
        final int limit = this.socketBufferHandler.getReadBuffer().capacity();
        if ( to.remaining() >= limit ) {
            to.limit ( to.position() + limit );
            nRead = this.fillReadBuffer ( block, to );
            this.updateLastRead();
        } else {
            nRead = this.fillReadBuffer ( block );
            this.updateLastRead();
            if ( nRead > 0 ) {
                nRead = this.populateReadBuffer ( to );
            }
        }
        return nRead;
    }
    @Override
    public void close() throws IOException {
        this.getSocket().close();
    }
    @Override
    public boolean isClosed() {
        return !this.getSocket().isOpen();
    }
    private int fillReadBuffer ( final boolean block ) throws IOException {
        this.socketBufferHandler.configureReadBufferForWrite();
        return this.fillReadBuffer ( block, this.socketBufferHandler.getReadBuffer() );
    }
    private int fillReadBuffer ( final boolean block, final ByteBuffer to ) throws IOException {
        final NioChannel channel = this.getSocket();
        int nRead;
        if ( block ) {
            Selector selector = null;
            try {
                selector = this.pool.get();
            } catch ( IOException ex ) {}
            try {
                final NioSocketWrapper att = ( NioSocketWrapper ) channel.getAttachment();
                if ( att == null ) {
                    throw new IOException ( "Key must be cancelled." );
                }
                nRead = this.pool.read ( to, channel, selector, att.getReadTimeout() );
            } finally {
                if ( selector != null ) {
                    this.pool.put ( selector );
                }
            }
        } else {
            nRead = channel.read ( to );
            if ( nRead == -1 ) {
                throw new EOFException();
            }
        }
        return nRead;
    }
    @Override
    protected void doWrite ( final boolean block, final ByteBuffer from ) throws IOException {
        final long writeTimeout = this.getWriteTimeout();
        Selector selector = null;
        try {
            selector = this.pool.get();
        } catch ( IOException ex ) {}
        try {
            this.pool.write ( from, this.getSocket(), selector, writeTimeout, block );
            if ( block ) {
                while ( !this.getSocket().flush ( true, selector, writeTimeout ) ) {}
            }
            this.updateLastWrite();
        } finally {
            if ( selector != null ) {
                this.pool.put ( selector );
            }
        }
    }
    @Override
    public void registerReadInterest() {
        this.getPoller().add ( this.getSocket(), 1 );
    }
    @Override
    public void registerWriteInterest() {
        this.getPoller().add ( this.getSocket(), 4 );
    }
    @Override
    public SendfileDataBase createSendfileData ( final String filename, final long pos, final long length ) {
        return new SendfileData ( filename, pos, length );
    }
    @Override
    public SendfileState processSendfile ( final SendfileDataBase sendfileData ) {
        this.setSendfileData ( ( SendfileData ) sendfileData );
        final SelectionKey key = this.getSocket().getIOChannel().keyFor ( this.getSocket().getPoller().getSelector() );
        return this.getSocket().getPoller().processSendfile ( key, this, true );
    }
    @Override
    protected void populateRemoteAddr() {
        final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getInetAddress();
        if ( inetAddr != null ) {
            this.remoteAddr = inetAddr.getHostAddress();
        }
    }
    @Override
    protected void populateRemoteHost() {
        final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getInetAddress();
        if ( inetAddr != null ) {
            this.remoteHost = inetAddr.getHostName();
            if ( this.remoteAddr == null ) {
                this.remoteAddr = inetAddr.getHostAddress();
            }
        }
    }
    @Override
    protected void populateRemotePort() {
        this.remotePort = this.getSocket().getIOChannel().socket().getPort();
    }
    @Override
    protected void populateLocalName() {
        final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getLocalAddress();
        if ( inetAddr != null ) {
            this.localName = inetAddr.getHostName();
        }
    }
    @Override
    protected void populateLocalAddr() {
        final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getLocalAddress();
        if ( inetAddr != null ) {
            this.localAddr = inetAddr.getHostAddress();
        }
    }
    @Override
    protected void populateLocalPort() {
        this.localPort = this.getSocket().getIOChannel().socket().getLocalPort();
    }
    @Override
    public SSLSupport getSslSupport ( final String clientCertProvider ) {
        if ( this.getSocket() instanceof SecureNioChannel ) {
            final SecureNioChannel ch = ( ( SocketWrapperBase<SecureNioChannel> ) this ).getSocket();
            final SSLSession session = ch.getSslEngine().getSession();
            return ( ( NioEndpoint ) this.getEndpoint() ).getSslImplementation().getSSLSupport ( session );
        }
        return null;
    }
    @Override
    public void doClientAuth ( final SSLSupport sslSupport ) {
        final SecureNioChannel sslChannel = ( ( SocketWrapperBase<SecureNioChannel> ) this ).getSocket();
        final SSLEngine engine = sslChannel.getSslEngine();
        if ( !engine.getNeedClientAuth() ) {
            engine.setNeedClientAuth ( true );
            try {
                sslChannel.rehandshake ( this.getEndpoint().getConnectionTimeout() );
                ( ( JSSESupport ) sslSupport ).setSession ( engine.getSession() );
            } catch ( IOException ioe ) {
                NioEndpoint.access$200().warn ( NioSocketWrapper.sm.getString ( "socket.sslreneg", ioe ) );
            }
        }
    }
    @Override
    public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
        this.getSocket().setAppReadBufHandler ( handler );
    }
}

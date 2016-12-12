package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
import java.nio.channels.ByteChannel;
public class NioChannel implements ByteChannel {
    protected static final StringManager sm;
    protected static final ByteBuffer emptyBuf;
    protected SocketChannel sc;
    protected SocketWrapperBase<NioChannel> socketWrapper;
    protected final SocketBufferHandler bufHandler;
    protected NioEndpoint.Poller poller;
    private ApplicationBufferHandler appReadBufHandler;
    public NioChannel ( final SocketChannel channel, final SocketBufferHandler bufHandler ) {
        this.sc = null;
        this.socketWrapper = null;
        this.sc = channel;
        this.bufHandler = bufHandler;
    }
    public void reset() throws IOException {
        this.bufHandler.reset();
    }
    void setSocketWrapper ( final SocketWrapperBase<NioChannel> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    public void free() {
        this.bufHandler.free();
    }
    public boolean flush ( final boolean block, final Selector s, final long timeout ) throws IOException {
        return true;
    }
    @Override
    public void close() throws IOException {
        this.getIOChannel().socket().close();
        this.getIOChannel().close();
    }
    public void close ( final boolean force ) throws IOException {
        if ( this.isOpen() || force ) {
            this.close();
        }
    }
    @Override
    public boolean isOpen() {
        return this.sc.isOpen();
    }
    @Override
    public int write ( final ByteBuffer src ) throws IOException {
        this.checkInterruptStatus();
        return this.sc.write ( src );
    }
    @Override
    public int read ( final ByteBuffer dst ) throws IOException {
        return this.sc.read ( dst );
    }
    public Object getAttachment() {
        final NioEndpoint.Poller pol = this.getPoller();
        final Selector sel = ( pol != null ) ? pol.getSelector() : null;
        final SelectionKey key = ( sel != null ) ? this.getIOChannel().keyFor ( sel ) : null;
        final Object att = ( key != null ) ? key.attachment() : null;
        return att;
    }
    public SocketBufferHandler getBufHandler() {
        return this.bufHandler;
    }
    public NioEndpoint.Poller getPoller() {
        return this.poller;
    }
    public SocketChannel getIOChannel() {
        return this.sc;
    }
    public boolean isClosing() {
        return false;
    }
    public boolean isHandshakeComplete() {
        return true;
    }
    public int handshake ( final boolean read, final boolean write ) throws IOException {
        return 0;
    }
    public void setPoller ( final NioEndpoint.Poller poller ) {
        this.poller = poller;
    }
    public void setIOChannel ( final SocketChannel IOChannel ) {
        this.sc = IOChannel;
    }
    @Override
    public String toString() {
        return super.toString() + ":" + this.sc.toString();
    }
    public int getOutboundRemaining() {
        return 0;
    }
    public boolean flushOutbound() throws IOException {
        return false;
    }
    protected void checkInterruptStatus() throws IOException {
        if ( Thread.interrupted() ) {
            throw new IOException ( NioChannel.sm.getString ( "channel.nio.interrupted" ) );
        }
    }
    public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
        this.appReadBufHandler = handler;
    }
    protected ApplicationBufferHandler getAppReadBufHandler() {
        return this.appReadBufHandler;
    }
    static {
        sm = StringManager.getManager ( NioChannel.class );
        emptyBuf = ByteBuffer.allocate ( 0 );
    }
}

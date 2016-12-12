package org.apache.tomcat.util.net;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.apache.tomcat.util.net.NioEndpoint.Poller;
import org.apache.tomcat.util.res.StringManager;
public class NioChannel implements ByteChannel {
    protected static final StringManager sm = StringManager.getManager ( NioChannel.class );
    protected static final ByteBuffer emptyBuf = ByteBuffer.allocate ( 0 );
    protected SocketChannel sc = null;
    protected SocketWrapperBase<NioChannel> socketWrapper = null;
    protected final SocketBufferHandler bufHandler;
    protected Poller poller;
    public NioChannel ( SocketChannel channel, SocketBufferHandler bufHandler ) {
        this.sc = channel;
        this.bufHandler = bufHandler;
    }
    public void reset() throws IOException {
        bufHandler.reset();
    }
    void setSocketWrapper ( SocketWrapperBase<NioChannel> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    public void free() {
        bufHandler.free();
    }
    public boolean flush ( boolean block, Selector s, long timeout )
    throws IOException {
        return true;
    }
    @Override
    public void close() throws IOException {
        getIOChannel().socket().close();
        getIOChannel().close();
    }
    public void close ( boolean force ) throws IOException {
        if ( isOpen() || force ) {
            close();
        }
    }
    @Override
    public boolean isOpen() {
        return sc.isOpen();
    }
    @Override
    public int write ( ByteBuffer src ) throws IOException {
        checkInterruptStatus();
        return sc.write ( src );
    }
    @Override
    public int read ( ByteBuffer dst ) throws IOException {
        return sc.read ( dst );
    }
    public Object getAttachment() {
        Poller pol = getPoller();
        Selector sel = pol != null ? pol.getSelector() : null;
        SelectionKey key = sel != null ? getIOChannel().keyFor ( sel ) : null;
        Object att = key != null ? key.attachment() : null;
        return att;
    }
    public SocketBufferHandler getBufHandler() {
        return bufHandler;
    }
    public Poller getPoller() {
        return poller;
    }
    public SocketChannel getIOChannel() {
        return sc;
    }
    public boolean isClosing() {
        return false;
    }
    public boolean isHandshakeComplete() {
        return true;
    }
    public int handshake ( boolean read, boolean write ) throws IOException {
        return 0;
    }
    public void setPoller ( Poller poller ) {
        this.poller = poller;
    }
    public void setIOChannel ( SocketChannel IOChannel ) {
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
            throw new IOException ( sm.getString ( "channel.nio.interrupted" ) );
        }
    }
    private ApplicationBufferHandler appReadBufHandler;
    public void setAppReadBufHandler ( ApplicationBufferHandler handler ) {
        this.appReadBufHandler = handler;
    }
    protected ApplicationBufferHandler getAppReadBufHandler() {
        return appReadBufHandler;
    }
}

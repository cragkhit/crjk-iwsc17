package org.apache.catalina.tribes.io;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.ChannelMessage;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
public class ObjectReader {
    private static final Log log;
    protected static final StringManager sm;
    private XByteBuffer buffer;
    protected long lastAccess;
    protected boolean accessed;
    private volatile boolean cancelled;
    public ObjectReader ( final int packetSize ) {
        this.lastAccess = System.currentTimeMillis();
        this.accessed = false;
        this.buffer = new XByteBuffer ( packetSize, true );
    }
    public ObjectReader ( final SocketChannel channel ) {
        this ( channel.socket() );
    }
    public ObjectReader ( final Socket socket ) {
        this.lastAccess = System.currentTimeMillis();
        this.accessed = false;
        try {
            this.buffer = new XByteBuffer ( socket.getReceiveBufferSize(), true );
        } catch ( IOException x ) {
            ObjectReader.log.warn ( ObjectReader.sm.getString ( "objectReader.retrieveFailed.socketReceiverBufferSize" ) );
            this.buffer = new XByteBuffer ( 43800, true );
        }
    }
    public synchronized void access() {
        this.accessed = true;
        this.lastAccess = System.currentTimeMillis();
    }
    public synchronized void finish() {
        this.accessed = false;
        this.lastAccess = System.currentTimeMillis();
    }
    public synchronized boolean isAccessed() {
        return this.accessed;
    }
    public int append ( final ByteBuffer data, final int len, final boolean count ) {
        this.buffer.append ( data, len );
        int pkgCnt = -1;
        if ( count ) {
            pkgCnt = this.buffer.countPackages();
        }
        return pkgCnt;
    }
    public int append ( final byte[] data, final int off, final int len, final boolean count ) {
        this.buffer.append ( data, off, len );
        int pkgCnt = -1;
        if ( count ) {
            pkgCnt = this.buffer.countPackages();
        }
        return pkgCnt;
    }
    public ChannelMessage[] execute() {
        final int pkgCnt = this.buffer.countPackages();
        final ChannelMessage[] result = new ChannelMessage[pkgCnt];
        for ( int i = 0; i < pkgCnt; ++i ) {
            final ChannelMessage data = this.buffer.extractPackage ( true );
            result[i] = data;
        }
        return result;
    }
    public int bufferSize() {
        return this.buffer.getLength();
    }
    public boolean hasPackage() {
        return this.buffer.countPackages ( true ) > 0;
    }
    public int count() {
        return this.buffer.countPackages();
    }
    public void close() {
        this.buffer = null;
    }
    public synchronized long getLastAccess() {
        return this.lastAccess;
    }
    public boolean isCancelled() {
        return this.cancelled;
    }
    public synchronized void setLastAccess ( final long lastAccess ) {
        this.lastAccess = lastAccess;
    }
    public void setCancelled ( final boolean cancelled ) {
        this.cancelled = cancelled;
    }
    static {
        log = LogFactory.getLog ( ObjectReader.class );
        sm = StringManager.getManager ( ObjectReader.class );
    }
}

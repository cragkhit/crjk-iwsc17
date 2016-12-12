package org.apache.tomcat.websocket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.OutputStream;
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
            throw new IllegalStateException ( WsRemoteEndpointImplBase.access$400().getString ( "wsRemoteEndpoint.closedOutputStream" ) );
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
            throw new IllegalStateException ( WsRemoteEndpointImplBase.access$400().getString ( "wsRemoteEndpoint.closedOutputStream" ) );
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
            throw new IllegalStateException ( WsRemoteEndpointImplBase.access$400().getString ( "wsRemoteEndpoint.closedOutputStream" ) );
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
        WsRemoteEndpointImplBase.access$200 ( this.endpoint ).complete ( last );
        this.buffer.clear();
    }
}

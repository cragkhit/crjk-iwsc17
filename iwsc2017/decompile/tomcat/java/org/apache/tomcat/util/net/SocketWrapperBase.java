package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.buf.ByteBufferHolder;
import java.util.concurrent.LinkedBlockingDeque;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public abstract class SocketWrapperBase<E> {
    private static final Log log;
    protected static final StringManager sm;
    private final E socket;
    private final AbstractEndpoint<E> endpoint;
    private volatile long readTimeout;
    private volatile long writeTimeout;
    private volatile int keepAliveLeft;
    private volatile boolean upgraded;
    private boolean secure;
    private String negotiatedProtocol;
    protected String localAddr;
    protected String localName;
    protected int localPort;
    protected String remoteAddr;
    protected String remoteHost;
    protected int remotePort;
    private volatile boolean blockingStatus;
    private final Lock blockingStatusReadLock;
    private final ReentrantReadWriteLock.WriteLock blockingStatusWriteLock;
    private volatile IOException error;
    protected volatile SocketBufferHandler socketBufferHandler;
    protected final LinkedBlockingDeque<ByteBufferHolder> bufferedWrites;
    protected int bufferedWriteSize;
    public static final CompletionCheck COMPLETE_WRITE;
    public static final CompletionCheck READ_DATA;
    public SocketWrapperBase ( final E socket, final AbstractEndpoint<E> endpoint ) {
        this.readTimeout = -1L;
        this.writeTimeout = -1L;
        this.keepAliveLeft = 100;
        this.upgraded = false;
        this.secure = false;
        this.negotiatedProtocol = null;
        this.localAddr = null;
        this.localName = null;
        this.localPort = -1;
        this.remoteAddr = null;
        this.remoteHost = null;
        this.remotePort = -1;
        this.blockingStatus = true;
        this.error = null;
        this.socketBufferHandler = null;
        this.bufferedWrites = new LinkedBlockingDeque<ByteBufferHolder>();
        this.bufferedWriteSize = 65536;
        this.socket = socket;
        this.endpoint = endpoint;
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock = lock.writeLock();
    }
    public E getSocket() {
        return this.socket;
    }
    public AbstractEndpoint<E> getEndpoint() {
        return this.endpoint;
    }
    public IOException getError() {
        return this.error;
    }
    public void setError ( final IOException error ) {
        if ( this.error != null ) {
            return;
        }
        this.error = error;
    }
    public void checkError() throws IOException {
        if ( this.error != null ) {
            throw this.error;
        }
    }
    public boolean isUpgraded() {
        return this.upgraded;
    }
    public void setUpgraded ( final boolean upgraded ) {
        this.upgraded = upgraded;
    }
    public boolean isSecure() {
        return this.secure;
    }
    public void setSecure ( final boolean secure ) {
        this.secure = secure;
    }
    public String getNegotiatedProtocol() {
        return this.negotiatedProtocol;
    }
    public void setNegotiatedProtocol ( final String negotiatedProtocol ) {
        this.negotiatedProtocol = negotiatedProtocol;
    }
    public void setReadTimeout ( final long readTimeout ) {
        if ( readTimeout > 0L ) {
            this.readTimeout = readTimeout;
        } else {
            this.readTimeout = -1L;
        }
    }
    public long getReadTimeout() {
        return this.readTimeout;
    }
    public void setWriteTimeout ( final long writeTimeout ) {
        if ( writeTimeout > 0L ) {
            this.writeTimeout = writeTimeout;
        } else {
            this.writeTimeout = -1L;
        }
    }
    public long getWriteTimeout() {
        return this.writeTimeout;
    }
    public void setKeepAliveLeft ( final int keepAliveLeft ) {
        this.keepAliveLeft = keepAliveLeft;
    }
    public int decrementKeepAlive() {
        return --this.keepAliveLeft;
    }
    public String getRemoteHost() {
        if ( this.remoteHost == null ) {
            this.populateRemoteHost();
        }
        return this.remoteHost;
    }
    protected abstract void populateRemoteHost();
    public String getRemoteAddr() {
        if ( this.remoteAddr == null ) {
            this.populateRemoteAddr();
        }
        return this.remoteAddr;
    }
    protected abstract void populateRemoteAddr();
    public int getRemotePort() {
        if ( this.remotePort == -1 ) {
            this.populateRemotePort();
        }
        return this.remotePort;
    }
    protected abstract void populateRemotePort();
    public String getLocalName() {
        if ( this.localName == null ) {
            this.populateLocalName();
        }
        return this.localName;
    }
    protected abstract void populateLocalName();
    public String getLocalAddr() {
        if ( this.localAddr == null ) {
            this.populateLocalAddr();
        }
        return this.localAddr;
    }
    protected abstract void populateLocalAddr();
    public int getLocalPort() {
        if ( this.localPort == -1 ) {
            this.populateLocalPort();
        }
        return this.localPort;
    }
    protected abstract void populateLocalPort();
    public boolean getBlockingStatus() {
        return this.blockingStatus;
    }
    public void setBlockingStatus ( final boolean blockingStatus ) {
        this.blockingStatus = blockingStatus;
    }
    public Lock getBlockingStatusReadLock() {
        return this.blockingStatusReadLock;
    }
    public ReentrantReadWriteLock.WriteLock getBlockingStatusWriteLock() {
        return this.blockingStatusWriteLock;
    }
    public SocketBufferHandler getSocketBufferHandler() {
        return this.socketBufferHandler;
    }
    public boolean hasDataToWrite() {
        return !this.socketBufferHandler.isWriteBufferEmpty() || this.bufferedWrites.size() > 0;
    }
    public boolean isReadyForWrite() {
        final boolean result = this.canWrite();
        if ( !result ) {
            this.registerWriteInterest();
        }
        return result;
    }
    public boolean canWrite() {
        if ( this.socketBufferHandler == null ) {
            throw new IllegalStateException ( SocketWrapperBase.sm.getString ( "socket.closed" ) );
        }
        return this.socketBufferHandler.isWriteBufferWritable() && this.bufferedWrites.size() == 0;
    }
    @Override
    public String toString() {
        return super.toString() + ":" + String.valueOf ( this.socket );
    }
    public abstract int read ( final boolean p0, final byte[] p1, final int p2, final int p3 ) throws IOException;
    public abstract int read ( final boolean p0, final ByteBuffer p1 ) throws IOException;
    public abstract boolean isReadyForRead() throws IOException;
    public abstract void setAppReadBufHandler ( final ApplicationBufferHandler p0 );
    protected int populateReadBuffer ( final byte[] b, final int off, final int len ) {
        this.socketBufferHandler.configureReadBufferForRead();
        final ByteBuffer readBuffer = this.socketBufferHandler.getReadBuffer();
        int remaining = readBuffer.remaining();
        if ( remaining > 0 ) {
            remaining = Math.min ( remaining, len );
            readBuffer.get ( b, off, remaining );
            if ( SocketWrapperBase.log.isDebugEnabled() ) {
                SocketWrapperBase.log.debug ( "Socket: [" + this + "], Read from buffer: [" + remaining + "]" );
            }
        }
        return remaining;
    }
    protected int populateReadBuffer ( final ByteBuffer to ) {
        this.socketBufferHandler.configureReadBufferForRead();
        final int nRead = transfer ( this.socketBufferHandler.getReadBuffer(), to );
        if ( SocketWrapperBase.log.isDebugEnabled() ) {
            SocketWrapperBase.log.debug ( "Socket: [" + this + "], Read from buffer: [" + nRead + "]" );
        }
        return nRead;
    }
    public void unRead ( final ByteBuffer returnedInput ) {
        if ( returnedInput != null ) {
            this.socketBufferHandler.configureReadBufferForWrite();
            this.socketBufferHandler.getReadBuffer().put ( returnedInput );
        }
    }
    public abstract void close() throws IOException;
    public abstract boolean isClosed();
    public final void write ( final boolean block, final byte[] buf, final int off, final int len ) throws IOException {
        if ( len == 0 || buf == null ) {
            return;
        }
        if ( block ) {
            this.writeBlocking ( buf, off, len );
        } else {
            this.writeNonBlocking ( buf, off, len );
        }
    }
    public final void write ( final boolean block, final ByteBuffer from ) throws IOException {
        if ( from == null || from.remaining() == 0 ) {
            return;
        }
        if ( block ) {
            this.writeBlocking ( from );
        } else {
            this.writeNonBlocking ( from );
        }
    }
    protected void writeBlocking ( final byte[] buf, int off, int len ) throws IOException {
        this.socketBufferHandler.configureWriteBufferForWrite();
        int thisTime = transfer ( buf, off, len, this.socketBufferHandler.getWriteBuffer() );
        while ( this.socketBufferHandler.getWriteBuffer().remaining() == 0 ) {
            len -= thisTime;
            off += thisTime;
            this.doWrite ( true );
            this.socketBufferHandler.configureWriteBufferForWrite();
            thisTime = transfer ( buf, off, len, this.socketBufferHandler.getWriteBuffer() );
        }
    }
    protected void writeBlocking ( final ByteBuffer from ) throws IOException {
        if ( this.socketBufferHandler.isWriteBufferEmpty() ) {
            this.writeByteBufferBlocking ( from );
        } else {
            this.socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, this.socketBufferHandler.getWriteBuffer() );
            if ( !this.socketBufferHandler.isWriteBufferWritable() ) {
                this.doWrite ( true );
                this.writeByteBufferBlocking ( from );
            }
        }
    }
    protected void writeByteBufferBlocking ( final ByteBuffer from ) throws IOException {
        final int limit = this.socketBufferHandler.getWriteBuffer().capacity();
        final int fromLimit = from.limit();
        while ( from.remaining() >= limit ) {
            from.limit ( from.position() + limit );
            this.doWrite ( true, from );
            from.limit ( fromLimit );
        }
        if ( from.remaining() > 0 ) {
            this.socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, this.socketBufferHandler.getWriteBuffer() );
        }
    }
    protected void writeNonBlocking ( final byte[] buf, int off, int len ) throws IOException {
        if ( this.bufferedWrites.size() == 0 && this.socketBufferHandler.isWriteBufferWritable() ) {
            this.socketBufferHandler.configureWriteBufferForWrite();
            int thisTime = transfer ( buf, off, len, this.socketBufferHandler.getWriteBuffer() );
            len -= thisTime;
            while ( !this.socketBufferHandler.isWriteBufferWritable() ) {
                off += thisTime;
                this.doWrite ( false );
                if ( len <= 0 || !this.socketBufferHandler.isWriteBufferWritable() ) {
                    break;
                }
                this.socketBufferHandler.configureWriteBufferForWrite();
                thisTime = transfer ( buf, off, len, this.socketBufferHandler.getWriteBuffer() );
                len -= thisTime;
            }
        }
        if ( len > 0 ) {
            this.addToBuffers ( buf, off, len );
        }
    }
    protected void writeNonBlocking ( final ByteBuffer from ) throws IOException {
        if ( this.bufferedWrites.size() == 0 && this.socketBufferHandler.isWriteBufferWritable() ) {
            this.writeNonBlockingInternal ( from );
        }
        if ( from.remaining() > 0 ) {
            this.addToBuffers ( from );
        }
    }
    private boolean writeNonBlockingInternal ( final ByteBuffer from ) throws IOException {
        if ( this.socketBufferHandler.isWriteBufferEmpty() ) {
            return this.writeByteBufferNonBlocking ( from );
        }
        this.socketBufferHandler.configureWriteBufferForWrite();
        transfer ( from, this.socketBufferHandler.getWriteBuffer() );
        if ( !this.socketBufferHandler.isWriteBufferWritable() ) {
            this.doWrite ( false );
            if ( this.socketBufferHandler.isWriteBufferWritable() ) {
                return this.writeByteBufferNonBlocking ( from );
            }
        }
        return !this.socketBufferHandler.isWriteBufferWritable();
    }
    protected boolean writeByteBufferNonBlocking ( final ByteBuffer from ) throws IOException {
        final int limit = this.socketBufferHandler.getWriteBuffer().capacity();
        final int fromLimit = from.limit();
        while ( from.remaining() >= limit ) {
            final int newLimit = from.position() + limit;
            from.limit ( newLimit );
            this.doWrite ( false, from );
            from.limit ( fromLimit );
            if ( from.position() != newLimit ) {
                return true;
            }
        }
        if ( from.remaining() > 0 ) {
            this.socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, this.socketBufferHandler.getWriteBuffer() );
        }
        return false;
    }
    public boolean flush ( final boolean block ) throws IOException {
        boolean result = false;
        if ( block ) {
            this.flushBlocking();
        } else {
            result = this.flushNonBlocking();
        }
        return result;
    }
    protected void flushBlocking() throws IOException {
        this.doWrite ( true );
        if ( this.bufferedWrites.size() > 0 ) {
            final Iterator<ByteBufferHolder> bufIter = this.bufferedWrites.iterator();
            while ( bufIter.hasNext() ) {
                final ByteBufferHolder buffer = bufIter.next();
                buffer.flip();
                this.writeBlocking ( buffer.getBuf() );
                if ( buffer.getBuf().remaining() == 0 ) {
                    bufIter.remove();
                }
            }
            if ( !this.socketBufferHandler.isWriteBufferEmpty() ) {
                this.doWrite ( true );
            }
        }
    }
    protected boolean flushNonBlocking() throws IOException {
        boolean dataLeft = !this.socketBufferHandler.isWriteBufferEmpty();
        if ( dataLeft ) {
            this.doWrite ( false );
            dataLeft = !this.socketBufferHandler.isWriteBufferEmpty();
        }
        if ( !dataLeft && this.bufferedWrites.size() > 0 ) {
            final Iterator<ByteBufferHolder> bufIter = this.bufferedWrites.iterator();
            while ( !dataLeft && bufIter.hasNext() ) {
                final ByteBufferHolder buffer = bufIter.next();
                buffer.flip();
                dataLeft = this.writeNonBlockingInternal ( buffer.getBuf() );
                if ( buffer.getBuf().remaining() == 0 ) {
                    bufIter.remove();
                }
            }
            if ( !dataLeft && !this.socketBufferHandler.isWriteBufferEmpty() ) {
                this.doWrite ( false );
                dataLeft = !this.socketBufferHandler.isWriteBufferEmpty();
            }
        }
        return dataLeft;
    }
    protected void doWrite ( final boolean block ) throws IOException {
        this.socketBufferHandler.configureWriteBufferForRead();
        this.doWrite ( block, this.socketBufferHandler.getWriteBuffer() );
    }
    protected abstract void doWrite ( final boolean p0, final ByteBuffer p1 ) throws IOException;
    protected void addToBuffers ( final byte[] buf, final int offset, final int length ) {
        final ByteBufferHolder holder = this.getByteBufferHolder ( length );
        holder.getBuf().put ( buf, offset, length );
    }
    protected void addToBuffers ( final ByteBuffer from ) {
        final ByteBufferHolder holder = this.getByteBufferHolder ( from.remaining() );
        holder.getBuf().put ( from );
    }
    private ByteBufferHolder getByteBufferHolder ( final int capacity ) {
        ByteBufferHolder holder = this.bufferedWrites.peekLast();
        if ( holder == null || holder.isFlipped() || holder.getBuf().remaining() < capacity ) {
            final ByteBuffer buffer = ByteBuffer.allocate ( Math.max ( this.bufferedWriteSize, capacity ) );
            holder = new ByteBufferHolder ( buffer, false );
            this.bufferedWrites.add ( holder );
        }
        return holder;
    }
    public void processSocket ( final SocketEvent socketStatus, final boolean dispatch ) {
        this.endpoint.processSocket ( this, socketStatus, dispatch );
    }
    public synchronized void executeNonBlockingDispatches ( final Iterator<DispatchType> dispatches ) {
        while ( dispatches != null && dispatches.hasNext() ) {
            final DispatchType dispatchType = dispatches.next();
            this.processSocket ( dispatchType.getSocketStatus(), false );
        }
    }
    public abstract void registerReadInterest();
    public abstract void registerWriteInterest();
    public abstract SendfileDataBase createSendfileData ( final String p0, final long p1, final long p2 );
    public abstract SendfileState processSendfile ( final SendfileDataBase p0 );
    public abstract void doClientAuth ( final SSLSupport p0 );
    public abstract SSLSupport getSslSupport ( final String p0 );
    public boolean hasAsyncIO() {
        return false;
    }
    public boolean isReadPending() {
        return false;
    }
    public boolean isWritePending() {
        return false;
    }
    public boolean awaitReadComplete ( final long timeout, final TimeUnit unit ) {
        return true;
    }
    public boolean awaitWriteComplete ( final long timeout, final TimeUnit unit ) {
        return true;
    }
    public final <A> CompletionState read ( final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler, final ByteBuffer... dsts ) {
        if ( dsts == null ) {
            throw new IllegalArgumentException();
        }
        return this.read ( dsts, 0, dsts.length, block, timeout, unit, attachment, check, handler );
    }
    public <A> CompletionState read ( final ByteBuffer[] dsts, final int offset, final int length, final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
        throw new UnsupportedOperationException();
    }
    public final <A> CompletionState write ( final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler, final ByteBuffer... srcs ) {
        if ( srcs == null ) {
            throw new IllegalArgumentException();
        }
        return this.write ( srcs, 0, srcs.length, block, timeout, unit, attachment, check, handler );
    }
    public <A> CompletionState write ( final ByteBuffer[] srcs, final int offset, final int length, final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
        throw new UnsupportedOperationException();
    }
    protected static int transfer ( final byte[] from, final int offset, final int length, final ByteBuffer to ) {
        final int max = Math.min ( length, to.remaining() );
        if ( max > 0 ) {
            to.put ( from, offset, max );
        }
        return max;
    }
    protected static int transfer ( final ByteBuffer from, final ByteBuffer to ) {
        final int max = Math.min ( from.remaining(), to.remaining() );
        if ( max > 0 ) {
            final int fromLimit = from.limit();
            from.limit ( from.position() + max );
            to.put ( from );
            from.limit ( fromLimit );
        }
        return max;
    }
    static {
        log = LogFactory.getLog ( SocketWrapperBase.class );
        sm = StringManager.getManager ( SocketWrapperBase.class );
        COMPLETE_WRITE = new CompletionCheck() {
            @Override
            public CompletionHandlerCall callHandler ( final CompletionState state, final ByteBuffer[] buffers, final int offset, final int length ) {
                for ( int i = 0; i < offset; ++i ) {
                    if ( buffers[i].remaining() > 0 ) {
                        return CompletionHandlerCall.CONTINUE;
                    }
                }
                return ( state == CompletionState.DONE ) ? CompletionHandlerCall.DONE : CompletionHandlerCall.NONE;
            }
        };
        READ_DATA = new CompletionCheck() {
            @Override
            public CompletionHandlerCall callHandler ( final CompletionState state, final ByteBuffer[] buffers, final int offset, final int length ) {
                return ( state == CompletionState.DONE ) ? CompletionHandlerCall.DONE : CompletionHandlerCall.NONE;
            }
        };
    }
    public enum CompletionState {
        PENDING,
        INLINE,
        ERROR,
        DONE;
    }
    public enum CompletionHandlerCall {
        CONTINUE,
        NONE,
        DONE;
    }
    public interface CompletionCheck {
        CompletionHandlerCall callHandler ( CompletionState p0, ByteBuffer[] p1, int p2, int p3 );
    }
}

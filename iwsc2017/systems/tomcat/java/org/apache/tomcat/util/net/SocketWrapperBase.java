package org.apache.tomcat.util.net;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteBufferHolder;
import org.apache.tomcat.util.res.StringManager;
public abstract class SocketWrapperBase<E> {
    private static final Log log = LogFactory.getLog ( SocketWrapperBase.class );
    protected static final StringManager sm = StringManager.getManager ( SocketWrapperBase.class );
    private final E socket;
    private final AbstractEndpoint<E> endpoint;
    private volatile long readTimeout = -1;
    private volatile long writeTimeout = -1;
    private volatile int keepAliveLeft = 100;
    private volatile boolean upgraded = false;
    private boolean secure = false;
    private String negotiatedProtocol = null;
    protected String localAddr = null;
    protected String localName = null;
    protected int localPort = -1;
    protected String remoteAddr = null;
    protected String remoteHost = null;
    protected int remotePort = -1;
    private volatile boolean blockingStatus = true;
    private final Lock blockingStatusReadLock;
    private final WriteLock blockingStatusWriteLock;
    private volatile IOException error = null;
    protected volatile SocketBufferHandler socketBufferHandler = null;
    protected final LinkedBlockingDeque<ByteBufferHolder> bufferedWrites = new LinkedBlockingDeque<>();
    protected int bufferedWriteSize = 64 * 1024;
    public SocketWrapperBase ( E socket, AbstractEndpoint<E> endpoint ) {
        this.socket = socket;
        this.endpoint = endpoint;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock = lock.writeLock();
    }
    public E getSocket() {
        return socket;
    }
    public AbstractEndpoint<E> getEndpoint() {
        return endpoint;
    }
    public IOException getError() {
        return error;
    }
    public void setError ( IOException error ) {
        if ( this.error != null ) {
            return;
        }
        this.error = error;
    }
    public void checkError() throws IOException {
        if ( error != null ) {
            throw error;
        }
    }
    public boolean isUpgraded() {
        return upgraded;
    }
    public void setUpgraded ( boolean upgraded ) {
        this.upgraded = upgraded;
    }
    public boolean isSecure() {
        return secure;
    }
    public void setSecure ( boolean secure ) {
        this.secure = secure;
    }
    public String getNegotiatedProtocol() {
        return negotiatedProtocol;
    }
    public void setNegotiatedProtocol ( String negotiatedProtocol ) {
        this.negotiatedProtocol = negotiatedProtocol;
    }
    public void setReadTimeout ( long readTimeout ) {
        if ( readTimeout > 0 ) {
            this.readTimeout = readTimeout;
        } else {
            this.readTimeout = -1;
        }
    }
    public long getReadTimeout() {
        return this.readTimeout;
    }
    public void setWriteTimeout ( long writeTimeout ) {
        if ( writeTimeout > 0 ) {
            this.writeTimeout = writeTimeout;
        } else {
            this.writeTimeout = -1;
        }
    }
    public long getWriteTimeout() {
        return this.writeTimeout;
    }
    public void setKeepAliveLeft ( int keepAliveLeft ) {
        this.keepAliveLeft = keepAliveLeft;
    }
    public int decrementKeepAlive() {
        return ( --keepAliveLeft );
    }
    public String getRemoteHost() {
        if ( remoteHost == null ) {
            populateRemoteHost();
        }
        return remoteHost;
    }
    protected abstract void populateRemoteHost();
    public String getRemoteAddr() {
        if ( remoteAddr == null ) {
            populateRemoteAddr();
        }
        return remoteAddr;
    }
    protected abstract void populateRemoteAddr();
    public int getRemotePort() {
        if ( remotePort == -1 ) {
            populateRemotePort();
        }
        return remotePort;
    }
    protected abstract void populateRemotePort();
    public String getLocalName() {
        if ( localName == null ) {
            populateLocalName();
        }
        return localName;
    }
    protected abstract void populateLocalName();
    public String getLocalAddr() {
        if ( localAddr == null ) {
            populateLocalAddr();
        }
        return localAddr;
    }
    protected abstract void populateLocalAddr();
    public int getLocalPort() {
        if ( localPort == -1 ) {
            populateLocalPort();
        }
        return localPort;
    }
    protected abstract void populateLocalPort();
    public boolean getBlockingStatus() {
        return blockingStatus;
    }
    public void setBlockingStatus ( boolean blockingStatus ) {
        this.blockingStatus = blockingStatus;
    }
    public Lock getBlockingStatusReadLock() {
        return blockingStatusReadLock;
    }
    public WriteLock getBlockingStatusWriteLock() {
        return blockingStatusWriteLock;
    }
    public SocketBufferHandler getSocketBufferHandler() {
        return socketBufferHandler;
    }
    public boolean hasDataToWrite() {
        return !socketBufferHandler.isWriteBufferEmpty() || bufferedWrites.size() > 0;
    }
    public boolean isReadyForWrite() {
        boolean result = canWrite();
        if ( !result ) {
            registerWriteInterest();
        }
        return result;
    }
    public boolean canWrite() {
        if ( socketBufferHandler == null ) {
            throw new IllegalStateException ( sm.getString ( "socket.closed" ) );
        }
        return socketBufferHandler.isWriteBufferWritable() && bufferedWrites.size() == 0;
    }
    @Override
    public String toString() {
        return super.toString() + ":" + String.valueOf ( socket );
    }
    public abstract int read ( boolean block, byte[] b, int off, int len ) throws IOException;
    public abstract int read ( boolean block, ByteBuffer to ) throws IOException;
    public abstract boolean isReadyForRead() throws IOException;
    public abstract void setAppReadBufHandler ( ApplicationBufferHandler handler );
    protected int populateReadBuffer ( byte[] b, int off, int len ) {
        socketBufferHandler.configureReadBufferForRead();
        ByteBuffer readBuffer = socketBufferHandler.getReadBuffer();
        int remaining = readBuffer.remaining();
        if ( remaining > 0 ) {
            remaining = Math.min ( remaining, len );
            readBuffer.get ( b, off, remaining );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Socket: [" + this + "], Read from buffer: [" + remaining + "]" );
            }
        }
        return remaining;
    }
    protected int populateReadBuffer ( ByteBuffer to ) {
        socketBufferHandler.configureReadBufferForRead();
        int nRead = transfer ( socketBufferHandler.getReadBuffer(), to );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Socket: [" + this + "], Read from buffer: [" + nRead + "]" );
        }
        return nRead;
    }
    public void unRead ( ByteBuffer returnedInput ) {
        if ( returnedInput != null ) {
            socketBufferHandler.configureReadBufferForWrite();
            socketBufferHandler.getReadBuffer().put ( returnedInput );
        }
    }
    public abstract void close() throws IOException;
    public abstract boolean isClosed();
    public final void write ( boolean block, byte[] buf, int off, int len ) throws IOException {
        if ( len == 0 || buf == null ) {
            return;
        }
        if ( block ) {
            writeBlocking ( buf, off, len );
        } else {
            writeNonBlocking ( buf, off, len );
        }
    }
    public final void write ( boolean block, ByteBuffer from ) throws IOException {
        if ( from == null || from.remaining() == 0 ) {
            return;
        }
        if ( block ) {
            writeBlocking ( from );
        } else {
            writeNonBlocking ( from );
        }
    }
    protected void writeBlocking ( byte[] buf, int off, int len ) throws IOException {
        socketBufferHandler.configureWriteBufferForWrite();
        int thisTime = transfer ( buf, off, len, socketBufferHandler.getWriteBuffer() );
        while ( socketBufferHandler.getWriteBuffer().remaining() == 0 ) {
            len = len - thisTime;
            off = off + thisTime;
            doWrite ( true );
            socketBufferHandler.configureWriteBufferForWrite();
            thisTime = transfer ( buf, off, len, socketBufferHandler.getWriteBuffer() );
        }
    }
    protected void writeBlocking ( ByteBuffer from ) throws IOException {
        if ( socketBufferHandler.isWriteBufferEmpty() ) {
            writeByteBufferBlocking ( from );
        } else {
            socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, socketBufferHandler.getWriteBuffer() );
            if ( !socketBufferHandler.isWriteBufferWritable() ) {
                doWrite ( true );
                writeByteBufferBlocking ( from );
            }
        }
    }
    protected void writeByteBufferBlocking ( ByteBuffer from ) throws IOException {
        int limit = socketBufferHandler.getWriteBuffer().capacity();
        int fromLimit = from.limit();
        while ( from.remaining() >= limit ) {
            from.limit ( from.position() + limit );
            doWrite ( true, from );
            from.limit ( fromLimit );
        }
        if ( from.remaining() > 0 ) {
            socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, socketBufferHandler.getWriteBuffer() );
        }
    }
    protected void writeNonBlocking ( byte[] buf, int off, int len ) throws IOException {
        if ( bufferedWrites.size() == 0 && socketBufferHandler.isWriteBufferWritable() ) {
            socketBufferHandler.configureWriteBufferForWrite();
            int thisTime = transfer ( buf, off, len, socketBufferHandler.getWriteBuffer() );
            len = len - thisTime;
            while ( !socketBufferHandler.isWriteBufferWritable() ) {
                off = off + thisTime;
                doWrite ( false );
                if ( len > 0 && socketBufferHandler.isWriteBufferWritable() ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    thisTime = transfer ( buf, off, len, socketBufferHandler.getWriteBuffer() );
                } else {
                    break;
                }
                len = len - thisTime;
            }
        }
        if ( len > 0 ) {
            addToBuffers ( buf, off, len );
        }
    }
    protected void writeNonBlocking ( ByteBuffer from ) throws IOException {
        if ( bufferedWrites.size() == 0 && socketBufferHandler.isWriteBufferWritable() ) {
            writeNonBlockingInternal ( from );
        }
        if ( from.remaining() > 0 ) {
            addToBuffers ( from );
        }
    }
    private boolean writeNonBlockingInternal ( ByteBuffer from ) throws IOException {
        if ( socketBufferHandler.isWriteBufferEmpty() ) {
            return writeByteBufferNonBlocking ( from );
        } else {
            socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, socketBufferHandler.getWriteBuffer() );
            if ( !socketBufferHandler.isWriteBufferWritable() ) {
                doWrite ( false );
                if ( socketBufferHandler.isWriteBufferWritable() ) {
                    return writeByteBufferNonBlocking ( from );
                }
            }
        }
        return !socketBufferHandler.isWriteBufferWritable();
    }
    protected boolean writeByteBufferNonBlocking ( ByteBuffer from ) throws IOException {
        int limit = socketBufferHandler.getWriteBuffer().capacity();
        int fromLimit = from.limit();
        while ( from.remaining() >= limit ) {
            int newLimit = from.position() + limit;
            from.limit ( newLimit );
            doWrite ( false, from );
            from.limit ( fromLimit );
            if ( from.position() != newLimit ) {
                return true;
            }
        }
        if ( from.remaining() > 0 ) {
            socketBufferHandler.configureWriteBufferForWrite();
            transfer ( from, socketBufferHandler.getWriteBuffer() );
        }
        return false;
    }
    public boolean flush ( boolean block ) throws IOException {
        boolean result = false;
        if ( block ) {
            flushBlocking();
        } else {
            result = flushNonBlocking();
        }
        return result;
    }
    protected void flushBlocking() throws IOException {
        doWrite ( true );
        if ( bufferedWrites.size() > 0 ) {
            Iterator<ByteBufferHolder> bufIter = bufferedWrites.iterator();
            while ( bufIter.hasNext() ) {
                ByteBufferHolder buffer = bufIter.next();
                buffer.flip();
                writeBlocking ( buffer.getBuf() );
                if ( buffer.getBuf().remaining() == 0 ) {
                    bufIter.remove();
                }
            }
            if ( !socketBufferHandler.isWriteBufferEmpty() ) {
                doWrite ( true );
            }
        }
    }
    protected boolean flushNonBlocking() throws IOException {
        boolean dataLeft = !socketBufferHandler.isWriteBufferEmpty();
        if ( dataLeft ) {
            doWrite ( false );
            dataLeft = !socketBufferHandler.isWriteBufferEmpty();
        }
        if ( !dataLeft && bufferedWrites.size() > 0 ) {
            Iterator<ByteBufferHolder> bufIter = bufferedWrites.iterator();
            while ( !dataLeft && bufIter.hasNext() ) {
                ByteBufferHolder buffer = bufIter.next();
                buffer.flip();
                dataLeft = writeNonBlockingInternal ( buffer.getBuf() );
                if ( buffer.getBuf().remaining() == 0 ) {
                    bufIter.remove();
                }
            }
            if ( !dataLeft && !socketBufferHandler.isWriteBufferEmpty() ) {
                doWrite ( false );
                dataLeft = !socketBufferHandler.isWriteBufferEmpty();
            }
        }
        return dataLeft;
    }
    protected void doWrite ( boolean block ) throws IOException {
        socketBufferHandler.configureWriteBufferForRead();
        doWrite ( block, socketBufferHandler.getWriteBuffer() );
    }
    protected abstract void doWrite ( boolean block, ByteBuffer from ) throws IOException;
    protected void addToBuffers ( byte[] buf, int offset, int length ) {
        ByteBufferHolder holder = getByteBufferHolder ( length );
        holder.getBuf().put ( buf, offset, length );
    }
    protected void addToBuffers ( ByteBuffer from ) {
        ByteBufferHolder holder = getByteBufferHolder ( from.remaining() );
        holder.getBuf().put ( from );
    }
    private ByteBufferHolder getByteBufferHolder ( int capacity ) {
        ByteBufferHolder holder = bufferedWrites.peekLast();
        if ( holder == null || holder.isFlipped() || holder.getBuf().remaining() < capacity ) {
            ByteBuffer buffer = ByteBuffer.allocate ( Math.max ( bufferedWriteSize, capacity ) );
            holder = new ByteBufferHolder ( buffer, false );
            bufferedWrites.add ( holder );
        }
        return holder;
    }
    public void processSocket ( SocketEvent socketStatus, boolean dispatch ) {
        endpoint.processSocket ( this, socketStatus, dispatch );
    }
    public synchronized void executeNonBlockingDispatches ( Iterator<DispatchType> dispatches ) {
        while ( dispatches != null && dispatches.hasNext() ) {
            DispatchType dispatchType = dispatches.next();
            processSocket ( dispatchType.getSocketStatus(), false );
        }
    }
    public abstract void registerReadInterest();
    public abstract void registerWriteInterest();
    public abstract SendfileDataBase createSendfileData ( String filename, long pos, long length );
    public abstract SendfileState processSendfile ( SendfileDataBase sendfileData );
    public abstract void doClientAuth ( SSLSupport sslSupport );
    public abstract SSLSupport getSslSupport ( String clientCertProvider );
    public enum CompletionState {
        PENDING,
        INLINE,
        ERROR,
        DONE
    }
    public enum CompletionHandlerCall {
        CONTINUE,
        NONE,
        DONE
    }
    public interface CompletionCheck {
        public CompletionHandlerCall callHandler ( CompletionState state, ByteBuffer[] buffers,
                int offset, int length );
    }
    public static final CompletionCheck COMPLETE_WRITE = new CompletionCheck() {
        @Override
        public CompletionHandlerCall callHandler ( CompletionState state, ByteBuffer[] buffers,
                int offset, int length ) {
            for ( int i = 0; i < offset; i++ ) {
                if ( buffers[i].remaining() > 0 ) {
                    return CompletionHandlerCall.CONTINUE;
                }
            }
            return ( state == CompletionState.DONE ) ? CompletionHandlerCall.DONE
                   : CompletionHandlerCall.NONE;
        }
    };
    public static final CompletionCheck READ_DATA = new CompletionCheck() {
        @Override
        public CompletionHandlerCall callHandler ( CompletionState state, ByteBuffer[] buffers,
                int offset, int length ) {
            return ( state == CompletionState.DONE ) ? CompletionHandlerCall.DONE
                   : CompletionHandlerCall.NONE;
        }
    };
    public boolean hasAsyncIO() {
        return false;
    }
    public boolean isReadPending() {
        return false;
    }
    public boolean isWritePending() {
        return false;
    }
    public boolean awaitReadComplete ( long timeout, TimeUnit unit ) {
        return true;
    }
    public boolean awaitWriteComplete ( long timeout, TimeUnit unit ) {
        return true;
    }
    public final <A> CompletionState read ( boolean block, long timeout, TimeUnit unit, A attachment,
                                            CompletionCheck check, CompletionHandler<Long, ? super A> handler, ByteBuffer... dsts ) {
        if ( dsts == null ) {
            throw new IllegalArgumentException();
        }
        return read ( dsts, 0, dsts.length, block, timeout, unit, attachment, check, handler );
    }
    public <A> CompletionState read ( ByteBuffer[] dsts, int offset, int length, boolean block,
                                      long timeout, TimeUnit unit, A attachment, CompletionCheck check,
                                      CompletionHandler<Long, ? super A> handler ) {
        throw new UnsupportedOperationException();
    }
    public final <A> CompletionState write ( boolean block, long timeout, TimeUnit unit, A attachment,
            CompletionCheck check, CompletionHandler<Long, ? super A> handler, ByteBuffer... srcs ) {
        if ( srcs == null ) {
            throw new IllegalArgumentException();
        }
        return write ( srcs, 0, srcs.length, block, timeout, unit, attachment, check, handler );
    }
    public <A> CompletionState write ( ByteBuffer[] srcs, int offset, int length, boolean block,
                                       long timeout, TimeUnit unit, A attachment, CompletionCheck check,
                                       CompletionHandler<Long, ? super A> handler ) {
        throw new UnsupportedOperationException();
    }
    protected static int transfer ( byte[] from, int offset, int length, ByteBuffer to ) {
        int max = Math.min ( length, to.remaining() );
        if ( max > 0 ) {
            to.put ( from, offset, max );
        }
        return max;
    }
    protected static int transfer ( ByteBuffer from, ByteBuffer to ) {
        int max = Math.min ( from.remaining(), to.remaining() );
        if ( max > 0 ) {
            int fromLimit = from.limit();
            from.limit ( from.position() + max );
            to.put ( from );
            from.limit ( fromLimit );
        }
        return max;
    }
}

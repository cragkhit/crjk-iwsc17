package org.apache.catalina.tribes.io;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
public class BufferPool {
    private static final Log log;
    public static final int DEFAULT_POOL_SIZE = 104857600;
    protected static final StringManager sm;
    protected static volatile BufferPool instance;
    protected final BufferPoolAPI pool;
    private BufferPool ( final BufferPoolAPI pool ) {
        this.pool = pool;
    }
    public XByteBuffer getBuffer ( final int minSize, final boolean discard ) {
        if ( this.pool != null ) {
            return this.pool.getBuffer ( minSize, discard );
        }
        return new XByteBuffer ( minSize, discard );
    }
    public void returnBuffer ( final XByteBuffer buffer ) {
        if ( this.pool != null ) {
            this.pool.returnBuffer ( buffer );
        }
    }
    public void clear() {
        if ( this.pool != null ) {
            this.pool.clear();
        }
    }
    public static BufferPool getBufferPool() {
        if ( BufferPool.instance == null ) {
            synchronized ( BufferPool.class ) {
                if ( BufferPool.instance == null ) {
                    final BufferPoolAPI pool = new BufferPool15Impl();
                    pool.setMaxSize ( 104857600 );
                    BufferPool.log.info ( BufferPool.sm.getString ( "bufferPool.created", Integer.toString ( 104857600 ), pool.getClass().getName() ) );
                    BufferPool.instance = new BufferPool ( pool );
                }
            }
        }
        return BufferPool.instance;
    }
    static {
        log = LogFactory.getLog ( BufferPool.class );
        sm = StringManager.getManager ( BufferPool.class );
        BufferPool.instance = null;
    }
    public interface BufferPoolAPI {
        void setMaxSize ( int p0 );
        XByteBuffer getBuffer ( int p0, boolean p1 );
        void returnBuffer ( XByteBuffer p0 );
        void clear();
    }
}

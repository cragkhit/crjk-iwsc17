package org.apache.catalina.tribes.transport;
import java.util.LinkedList;
import java.util.List;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.Member;
import java.io.IOException;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
public abstract class PooledSender extends AbstractSender implements MultiPointSender {
    private static final Log log;
    protected static final StringManager sm;
    private final SenderQueue queue;
    private int poolSize;
    private long maxWait;
    public PooledSender() {
        this.poolSize = 25;
        this.maxWait = 3000L;
        this.queue = new SenderQueue ( this, this.poolSize );
    }
    public abstract DataSender getNewDataSender();
    public DataSender getSender() {
        return this.queue.getSender ( this.getMaxWait() );
    }
    public void returnSender ( final DataSender sender ) {
        sender.keepalive();
        this.queue.returnSender ( sender );
    }
    @Override
    public synchronized void connect() throws IOException {
        this.queue.open();
        this.setConnected ( true );
    }
    @Override
    public synchronized void disconnect() {
        this.queue.close();
        this.setConnected ( false );
    }
    public int getInPoolSize() {
        return this.queue.getInPoolSize();
    }
    public int getInUsePoolSize() {
        return this.queue.getInUsePoolSize();
    }
    public void setPoolSize ( final int poolSize ) {
        this.poolSize = poolSize;
        this.queue.setLimit ( poolSize );
    }
    public int getPoolSize() {
        return this.poolSize;
    }
    public long getMaxWait() {
        return this.maxWait;
    }
    public void setMaxWait ( final long maxWait ) {
        this.maxWait = maxWait;
    }
    @Override
    public boolean keepalive() {
        return this.queue != null && this.queue.checkIdleKeepAlive();
    }
    @Override
    public void add ( final Member member ) {
    }
    @Override
    public void remove ( final Member member ) {
    }
    static {
        log = LogFactory.getLog ( PooledSender.class );
        sm = StringManager.getManager ( "org.apache.catalina.tribes.transport" );
    }
    private static class SenderQueue {
        private int limit;
        PooledSender parent;
        private List<DataSender> notinuse;
        private List<DataSender> inuse;
        private boolean isOpen;
        public SenderQueue ( final PooledSender parent, final int limit ) {
            this.limit = 25;
            this.parent = null;
            this.notinuse = null;
            this.inuse = null;
            this.isOpen = true;
            this.limit = limit;
            this.parent = parent;
            this.notinuse = new LinkedList<DataSender>();
            this.inuse = new LinkedList<DataSender>();
        }
        public int getLimit() {
            return this.limit;
        }
        public void setLimit ( final int limit ) {
            this.limit = limit;
        }
        public int getInUsePoolSize() {
            return this.inuse.size();
        }
        public int getInPoolSize() {
            return this.notinuse.size();
        }
        public synchronized boolean checkIdleKeepAlive() {
            final DataSender[] list = new DataSender[this.notinuse.size()];
            this.notinuse.toArray ( list );
            boolean result = false;
            for ( int i = 0; i < list.length; ++i ) {
                result |= list[i].keepalive();
            }
            return result;
        }
        public synchronized DataSender getSender ( final long timeout ) {
            final long start = System.currentTimeMillis();
            while ( this.isOpen ) {
                DataSender sender = null;
                if ( this.notinuse.size() == 0 && this.inuse.size() < this.limit ) {
                    sender = this.parent.getNewDataSender();
                } else if ( this.notinuse.size() > 0 ) {
                    sender = this.notinuse.remove ( 0 );
                }
                if ( sender != null ) {
                    this.inuse.add ( sender );
                    return sender;
                }
                final long delta = System.currentTimeMillis() - start;
                if ( delta > timeout && timeout > 0L ) {
                    return null;
                }
                try {
                    this.wait ( Math.max ( timeout - delta, 1L ) );
                } catch ( InterruptedException ex ) {}
            }
            throw new IllegalStateException ( PooledSender.sm.getString ( "pooledSender.closed.queue" ) );
        }
        public synchronized void returnSender ( final DataSender sender ) {
            if ( !this.isOpen ) {
                sender.disconnect();
                return;
            }
            this.inuse.remove ( sender );
            if ( this.notinuse.size() < this.getLimit() ) {
                this.notinuse.add ( sender );
            } else {
                try {
                    sender.disconnect();
                } catch ( Exception e ) {
                    if ( PooledSender.log.isDebugEnabled() ) {
                        PooledSender.log.debug ( PooledSender.sm.getString ( "PooledSender.senderDisconnectFail" ), e );
                    }
                }
            }
            this.notify();
        }
        public synchronized void close() {
            this.isOpen = false;
            final Object[] unused = this.notinuse.toArray();
            final Object[] used = this.inuse.toArray();
            for ( int i = 0; i < unused.length; ++i ) {
                final DataSender sender = ( DataSender ) unused[i];
                sender.disconnect();
            }
            for ( int i = 0; i < used.length; ++i ) {
                final DataSender sender = ( DataSender ) used[i];
                sender.disconnect();
            }
            this.notinuse.clear();
            this.inuse.clear();
            this.notify();
        }
        public synchronized void open() {
            this.isOpen = true;
            this.notify();
        }
    }
}

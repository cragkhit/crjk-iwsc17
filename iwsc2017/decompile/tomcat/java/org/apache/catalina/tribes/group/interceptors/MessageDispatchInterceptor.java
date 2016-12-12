package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.ErrorHandler;
import org.apache.catalina.tribes.UniqueId;
import java.util.concurrent.ThreadFactory;
import org.apache.catalina.tribes.util.ExecutorFactory;
import org.apache.catalina.tribes.util.TcclThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class MessageDispatchInterceptor extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    protected long maxQueueSize;
    protected volatile boolean run;
    protected boolean useDeepClone;
    protected boolean alwaysSend;
    protected final AtomicLong currentSize;
    protected ExecutorService executor;
    protected int maxThreads;
    protected int maxSpareThreads;
    protected long keepAliveTime;
    public MessageDispatchInterceptor() {
        this.maxQueueSize = 67108864L;
        this.run = false;
        this.useDeepClone = true;
        this.alwaysSend = true;
        this.currentSize = new AtomicLong ( 0L );
        this.executor = null;
        this.maxThreads = 10;
        this.maxSpareThreads = 2;
        this.keepAliveTime = 5000L;
        this.setOptionFlag ( 8 );
    }
    @Override
    public void sendMessage ( final Member[] destination, ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        final boolean async = ( msg.getOptions() & 0x8 ) == 0x8;
        if ( async && this.run ) {
            if ( this.getCurrentSize() + msg.getMessage().getLength() > this.maxQueueSize ) {
                if ( this.alwaysSend ) {
                    super.sendMessage ( destination, msg, payload );
                    return;
                }
                throw new ChannelException ( MessageDispatchInterceptor.sm.getString ( "messageDispatchInterceptor.queue.full", Long.toString ( this.maxQueueSize ), Long.toString ( this.getCurrentSize() ) ) );
            } else {
                if ( this.useDeepClone ) {
                    msg = ( ChannelMessage ) msg.deepclone();
                }
                if ( !this.addToQueue ( msg, destination, payload ) ) {
                    throw new ChannelException ( MessageDispatchInterceptor.sm.getString ( "messageDispatchInterceptor.unableAdd.queue" ) );
                }
                this.addAndGetCurrentSize ( msg.getMessage().getLength() );
            }
        } else {
            super.sendMessage ( destination, msg, payload );
        }
    }
    public boolean addToQueue ( final ChannelMessage msg, final Member[] destination, final InterceptorPayload payload ) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                MessageDispatchInterceptor.this.sendAsyncData ( msg, destination, payload );
            }
        };
        this.executor.execute ( r );
        return true;
    }
    public void startQueue() {
        if ( this.run ) {
            return;
        }
        String channelName = "";
        if ( this.getChannel().getName() != null ) {
            channelName = "[" + this.getChannel().getName() + "]";
        }
        this.executor = ExecutorFactory.newThreadPool ( this.maxSpareThreads, this.maxThreads, this.keepAliveTime, TimeUnit.MILLISECONDS, new TcclThreadFactory ( "MessageDispatchInterceptor.MessageDispatchThread" + channelName ) );
        this.run = true;
    }
    public void stopQueue() {
        this.run = false;
        this.executor.shutdownNow();
        this.setAndGetCurrentSize ( 0L );
    }
    @Override
    public void setOptionFlag ( final int flag ) {
        if ( flag != 8 ) {
            MessageDispatchInterceptor.log.warn ( MessageDispatchInterceptor.sm.getString ( "messageDispatchInterceptor.warning.optionflag" ) );
        }
        super.setOptionFlag ( flag );
    }
    public void setMaxQueueSize ( final long maxQueueSize ) {
        this.maxQueueSize = maxQueueSize;
    }
    public void setUseDeepClone ( final boolean useDeepClone ) {
        this.useDeepClone = useDeepClone;
    }
    public long getMaxQueueSize() {
        return this.maxQueueSize;
    }
    public boolean getUseDeepClone() {
        return this.useDeepClone;
    }
    public long getCurrentSize() {
        return this.currentSize.get();
    }
    public long addAndGetCurrentSize ( final long inc ) {
        return this.currentSize.addAndGet ( inc );
    }
    public long setAndGetCurrentSize ( final long value ) {
        this.currentSize.set ( value );
        return value;
    }
    public long getKeepAliveTime() {
        return this.keepAliveTime;
    }
    public int getMaxSpareThreads() {
        return this.maxSpareThreads;
    }
    public int getMaxThreads() {
        return this.maxThreads;
    }
    public void setKeepAliveTime ( final long keepAliveTime ) {
        this.keepAliveTime = keepAliveTime;
    }
    public void setMaxSpareThreads ( final int maxSpareThreads ) {
        this.maxSpareThreads = maxSpareThreads;
    }
    public void setMaxThreads ( final int maxThreads ) {
        this.maxThreads = maxThreads;
    }
    public boolean isAlwaysSend() {
        return this.alwaysSend;
    }
    public void setAlwaysSend ( final boolean alwaysSend ) {
        this.alwaysSend = alwaysSend;
    }
    @Override
    public void start ( final int svc ) throws ChannelException {
        if ( !this.run ) {
            synchronized ( this ) {
                if ( !this.run && ( svc & 0x2 ) == 0x2 ) {
                    this.startQueue();
                }
            }
        }
        super.start ( svc );
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        if ( this.run ) {
            synchronized ( this ) {
                if ( this.run && ( svc & 0x2 ) == 0x2 ) {
                    this.stopQueue();
                }
            }
        }
        super.stop ( svc );
    }
    protected void sendAsyncData ( final ChannelMessage msg, final Member[] destination, final InterceptorPayload payload ) {
        ErrorHandler handler = null;
        if ( payload != null ) {
            handler = payload.getErrorHandler();
        }
        try {
            super.sendMessage ( destination, msg, null );
            try {
                if ( handler != null ) {
                    handler.handleCompletion ( new UniqueId ( msg.getUniqueId() ) );
                }
            } catch ( Exception ex ) {
                MessageDispatchInterceptor.log.error ( MessageDispatchInterceptor.sm.getString ( "messageDispatchInterceptor.completeMessage.failed" ), ex );
            }
        } catch ( Exception x ) {
            ChannelException cx = null;
            if ( x instanceof ChannelException ) {
                cx = ( ChannelException ) x;
            } else {
                cx = new ChannelException ( x );
            }
            if ( MessageDispatchInterceptor.log.isDebugEnabled() ) {
                MessageDispatchInterceptor.log.debug ( MessageDispatchInterceptor.sm.getString ( "messageDispatchInterceptor.AsyncMessage.failed" ), x );
            }
            try {
                if ( handler != null ) {
                    handler.handleError ( cx, new UniqueId ( msg.getUniqueId() ) );
                }
            } catch ( Exception ex2 ) {
                MessageDispatchInterceptor.log.error ( MessageDispatchInterceptor.sm.getString ( "messageDispatchInterceptor.errorMessage.failed" ), ex2 );
            }
        } finally {
            this.addAndGetCurrentSize ( -msg.getMessage().getLength() );
        }
    }
    static {
        log = LogFactory.getLog ( MessageDispatchInterceptor.class );
        sm = StringManager.getManager ( MessageDispatchInterceptor.class );
    }
}

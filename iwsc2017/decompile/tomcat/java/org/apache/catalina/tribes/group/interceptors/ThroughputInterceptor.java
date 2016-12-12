package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class ThroughputInterceptor extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    double mbTx;
    double mbAppTx;
    double mbRx;
    double timeTx;
    double lastCnt;
    final AtomicLong msgTxCnt;
    final AtomicLong msgRxCnt;
    final AtomicLong msgTxErr;
    int interval;
    final AtomicInteger access;
    long txStart;
    long rxStart;
    final DecimalFormat df;
    public ThroughputInterceptor() {
        this.mbTx = 0.0;
        this.mbAppTx = 0.0;
        this.mbRx = 0.0;
        this.timeTx = 0.0;
        this.lastCnt = 0.0;
        this.msgTxCnt = new AtomicLong ( 1L );
        this.msgRxCnt = new AtomicLong ( 0L );
        this.msgTxErr = new AtomicLong ( 0L );
        this.interval = 10000;
        this.access = new AtomicInteger ( 0 );
        this.txStart = 0L;
        this.rxStart = 0L;
        this.df = new DecimalFormat ( "#0.00" );
    }
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        if ( this.access.addAndGet ( 1 ) == 1 ) {
            this.txStart = System.currentTimeMillis();
        }
        final long bytes = XByteBuffer.getDataPackageLength ( ( ( ChannelData ) msg ).getDataPackageLength() );
        try {
            super.sendMessage ( destination, msg, payload );
        } catch ( ChannelException x ) {
            this.msgTxErr.addAndGet ( 1L );
            if ( this.access.get() == 1 ) {
                this.access.addAndGet ( -1 );
            }
            throw x;
        }
        this.mbTx += bytes * destination.length / 1048576.0;
        this.mbAppTx += bytes / 1048576.0;
        if ( this.access.addAndGet ( -1 ) == 0 ) {
            final long stop = System.currentTimeMillis();
            this.timeTx += ( stop - this.txStart ) / 1000.0;
            if ( this.msgTxCnt.get() / this.interval >= this.lastCnt ) {
                ++this.lastCnt;
                this.report ( this.timeTx );
            }
        }
        this.msgTxCnt.addAndGet ( 1L );
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( this.rxStart == 0L ) {
            this.rxStart = System.currentTimeMillis();
        }
        final long bytes = XByteBuffer.getDataPackageLength ( ( ( ChannelData ) msg ).getDataPackageLength() );
        this.mbRx += bytes / 1048576.0;
        this.msgRxCnt.addAndGet ( 1L );
        if ( this.msgRxCnt.get() % this.interval == 0L ) {
            this.report ( this.timeTx );
        }
        super.messageReceived ( msg );
    }
    public void report ( final double timeTx ) {
        if ( ThroughputInterceptor.log.isInfoEnabled() ) {
            ThroughputInterceptor.log.info ( ThroughputInterceptor.sm.getString ( "throughputInterceptor.report", this.msgTxCnt, this.df.format ( this.mbTx ), this.df.format ( this.mbAppTx ), this.df.format ( timeTx ), this.df.format ( this.mbTx / timeTx ), this.df.format ( this.mbAppTx / timeTx ), this.msgTxErr, this.msgRxCnt, this.df.format ( this.mbRx / ( ( System.currentTimeMillis() - this.rxStart ) / 1000L ) ), this.df.format ( this.mbRx ) ) );
        }
    }
    public void setInterval ( final int interval ) {
        this.interval = interval;
    }
    public int getInterval() {
        return this.interval;
    }
    public double getLastCnt() {
        return this.lastCnt;
    }
    public double getMbAppTx() {
        return this.mbAppTx;
    }
    public double getMbRx() {
        return this.mbRx;
    }
    public double getMbTx() {
        return this.mbTx;
    }
    public AtomicLong getMsgRxCnt() {
        return this.msgRxCnt;
    }
    public AtomicLong getMsgTxCnt() {
        return this.msgTxCnt;
    }
    public AtomicLong getMsgTxErr() {
        return this.msgTxErr;
    }
    public long getRxStart() {
        return this.rxStart;
    }
    public double getTimeTx() {
        return this.timeTx;
    }
    public long getTxStart() {
        return this.txStart;
    }
    static {
        log = LogFactory.getLog ( ThroughputInterceptor.class );
        sm = StringManager.getManager ( ThroughputInterceptor.class );
    }
}

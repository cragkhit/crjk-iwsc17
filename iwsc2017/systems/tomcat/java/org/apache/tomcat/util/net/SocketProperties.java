package org.apache.tomcat.util.net;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
public class SocketProperties {
    protected int processorCache = 500;
    protected int eventCache = 500;
    protected boolean directBuffer = false;
    protected boolean directSslBuffer = false;
    protected Integer rxBufSize = null;
    protected Integer txBufSize = null;
    protected int appReadBufSize = 8192;
    protected int appWriteBufSize = 8192;
    protected int bufferPool = 500;
    protected int bufferPoolSize = 1024 * 1024 * 100;
    protected Boolean tcpNoDelay = Boolean.TRUE;
    protected Boolean soKeepAlive = null;
    protected Boolean ooBInline = null;
    protected Boolean soReuseAddress = null;
    protected Boolean soLingerOn = null;
    protected Integer soLingerTime = null;
    protected Integer soTimeout = Integer.valueOf ( 20000 );
    protected Integer performanceConnectionTime = null;
    protected Integer performanceLatency = null;
    protected Integer performanceBandwidth = null;
    protected long timeoutInterval = 1000;
    protected int unlockTimeout = 250;
    public void setProperties ( Socket socket ) throws SocketException {
        if ( rxBufSize != null ) {
            socket.setReceiveBufferSize ( rxBufSize.intValue() );
        }
        if ( txBufSize != null ) {
            socket.setSendBufferSize ( txBufSize.intValue() );
        }
        if ( ooBInline != null ) {
            socket.setOOBInline ( ooBInline.booleanValue() );
        }
        if ( soKeepAlive != null ) {
            socket.setKeepAlive ( soKeepAlive.booleanValue() );
        }
        if ( performanceConnectionTime != null && performanceLatency != null &&
                performanceBandwidth != null )
            socket.setPerformancePreferences (
                performanceConnectionTime.intValue(),
                performanceLatency.intValue(),
                performanceBandwidth.intValue() );
        if ( soReuseAddress != null ) {
            socket.setReuseAddress ( soReuseAddress.booleanValue() );
        }
        if ( soLingerOn != null && soLingerTime != null )
            socket.setSoLinger ( soLingerOn.booleanValue(),
                                 soLingerTime.intValue() );
        if ( soTimeout != null && soTimeout.intValue() >= 0 ) {
            socket.setSoTimeout ( soTimeout.intValue() );
        }
        if ( tcpNoDelay != null ) {
            socket.setTcpNoDelay ( tcpNoDelay.booleanValue() );
        }
    }
    public void setProperties ( ServerSocket socket ) throws SocketException {
        if ( rxBufSize != null ) {
            socket.setReceiveBufferSize ( rxBufSize.intValue() );
        }
        if ( performanceConnectionTime != null && performanceLatency != null &&
                performanceBandwidth != null )
            socket.setPerformancePreferences (
                performanceConnectionTime.intValue(),
                performanceLatency.intValue(),
                performanceBandwidth.intValue() );
        if ( soReuseAddress != null ) {
            socket.setReuseAddress ( soReuseAddress.booleanValue() );
        }
        if ( soTimeout != null && soTimeout.intValue() >= 0 ) {
            socket.setSoTimeout ( soTimeout.intValue() );
        }
    }
    public void setProperties ( AsynchronousSocketChannel socket ) throws IOException {
        if ( rxBufSize != null ) {
            socket.setOption ( StandardSocketOptions.SO_RCVBUF, rxBufSize );
        }
        if ( txBufSize != null ) {
            socket.setOption ( StandardSocketOptions.SO_SNDBUF, txBufSize );
        }
        if ( soKeepAlive != null ) {
            socket.setOption ( StandardSocketOptions.SO_KEEPALIVE, soKeepAlive );
        }
        if ( soReuseAddress != null ) {
            socket.setOption ( StandardSocketOptions.SO_REUSEADDR, soReuseAddress );
        }
        if ( soLingerOn != null && soLingerOn.booleanValue() && soLingerTime != null ) {
            socket.setOption ( StandardSocketOptions.SO_LINGER, soLingerTime );
        }
        if ( tcpNoDelay != null ) {
            socket.setOption ( StandardSocketOptions.TCP_NODELAY, tcpNoDelay );
        }
    }
    public void setProperties ( AsynchronousServerSocketChannel socket ) throws IOException {
        if ( rxBufSize != null ) {
            socket.setOption ( StandardSocketOptions.SO_RCVBUF, rxBufSize );
        }
        if ( soReuseAddress != null ) {
            socket.setOption ( StandardSocketOptions.SO_REUSEADDR, soReuseAddress );
        }
    }
    public boolean getDirectBuffer() {
        return directBuffer;
    }
    public boolean getDirectSslBuffer() {
        return directSslBuffer;
    }
    public boolean getOoBInline() {
        return ooBInline.booleanValue();
    }
    public int getPerformanceBandwidth() {
        return performanceBandwidth.intValue();
    }
    public int getPerformanceConnectionTime() {
        return performanceConnectionTime.intValue();
    }
    public int getPerformanceLatency() {
        return performanceLatency.intValue();
    }
    public int getRxBufSize() {
        return rxBufSize.intValue();
    }
    public boolean getSoKeepAlive() {
        return soKeepAlive.booleanValue();
    }
    public boolean getSoLingerOn() {
        return soLingerOn.booleanValue();
    }
    public int getSoLingerTime() {
        return soLingerTime.intValue();
    }
    public boolean getSoReuseAddress() {
        return soReuseAddress.booleanValue();
    }
    public int getSoTimeout() {
        return soTimeout.intValue();
    }
    public boolean getTcpNoDelay() {
        return tcpNoDelay.booleanValue();
    }
    public int getTxBufSize() {
        return txBufSize.intValue();
    }
    public int getBufferPool() {
        return bufferPool;
    }
    public int getBufferPoolSize() {
        return bufferPoolSize;
    }
    public int getEventCache() {
        return eventCache;
    }
    public int getAppReadBufSize() {
        return appReadBufSize;
    }
    public int getAppWriteBufSize() {
        return appWriteBufSize;
    }
    public int getProcessorCache() {
        return processorCache;
    }
    public long getTimeoutInterval() {
        return timeoutInterval;
    }
    public int getDirectBufferPool() {
        return bufferPool;
    }
    public void setPerformanceConnectionTime ( int performanceConnectionTime ) {
        this.performanceConnectionTime =
            Integer.valueOf ( performanceConnectionTime );
    }
    public void setTxBufSize ( int txBufSize ) {
        this.txBufSize = Integer.valueOf ( txBufSize );
    }
    public void setTcpNoDelay ( boolean tcpNoDelay ) {
        this.tcpNoDelay = Boolean.valueOf ( tcpNoDelay );
    }
    public void setSoTimeout ( int soTimeout ) {
        this.soTimeout = Integer.valueOf ( soTimeout );
    }
    public void setSoReuseAddress ( boolean soReuseAddress ) {
        this.soReuseAddress = Boolean.valueOf ( soReuseAddress );
    }
    public void setSoLingerTime ( int soLingerTime ) {
        this.soLingerTime = Integer.valueOf ( soLingerTime );
    }
    public void setSoKeepAlive ( boolean soKeepAlive ) {
        this.soKeepAlive = Boolean.valueOf ( soKeepAlive );
    }
    public void setRxBufSize ( int rxBufSize ) {
        this.rxBufSize = Integer.valueOf ( rxBufSize );
    }
    public void setPerformanceLatency ( int performanceLatency ) {
        this.performanceLatency = Integer.valueOf ( performanceLatency );
    }
    public void setPerformanceBandwidth ( int performanceBandwidth ) {
        this.performanceBandwidth = Integer.valueOf ( performanceBandwidth );
    }
    public void setOoBInline ( boolean ooBInline ) {
        this.ooBInline = Boolean.valueOf ( ooBInline );
    }
    public void setDirectBuffer ( boolean directBuffer ) {
        this.directBuffer = directBuffer;
    }
    public void setDirectSslBuffer ( boolean directSslBuffer ) {
        this.directSslBuffer = directSslBuffer;
    }
    public void setSoLingerOn ( boolean soLingerOn ) {
        this.soLingerOn = Boolean.valueOf ( soLingerOn );
    }
    public void setBufferPool ( int bufferPool ) {
        this.bufferPool = bufferPool;
    }
    public void setBufferPoolSize ( int bufferPoolSize ) {
        this.bufferPoolSize = bufferPoolSize;
    }
    public void setEventCache ( int eventCache ) {
        this.eventCache = eventCache;
    }
    public void setAppReadBufSize ( int appReadBufSize ) {
        this.appReadBufSize = appReadBufSize;
    }
    public void setAppWriteBufSize ( int appWriteBufSize ) {
        this.appWriteBufSize = appWriteBufSize;
    }
    public void setProcessorCache ( int processorCache ) {
        this.processorCache = processorCache;
    }
    public void setTimeoutInterval ( long timeoutInterval ) {
        this.timeoutInterval = timeoutInterval;
    }
    public void setDirectBufferPool ( int directBufferPool ) {
        this.bufferPool = directBufferPool;
    }
    public int getUnlockTimeout() {
        return unlockTimeout;
    }
    public void setUnlockTimeout ( int unlockTimeout ) {
        this.unlockTimeout = unlockTimeout;
    }
}

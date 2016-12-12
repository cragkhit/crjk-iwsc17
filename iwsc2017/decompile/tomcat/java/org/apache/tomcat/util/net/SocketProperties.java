package org.apache.tomcat.util.net;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;
public class SocketProperties {
    protected int processorCache;
    protected int eventCache;
    protected boolean directBuffer;
    protected boolean directSslBuffer;
    protected Integer rxBufSize;
    protected Integer txBufSize;
    protected int appReadBufSize;
    protected int appWriteBufSize;
    protected int bufferPool;
    protected int bufferPoolSize;
    protected Boolean tcpNoDelay;
    protected Boolean soKeepAlive;
    protected Boolean ooBInline;
    protected Boolean soReuseAddress;
    protected Boolean soLingerOn;
    protected Integer soLingerTime;
    protected Integer soTimeout;
    protected Integer performanceConnectionTime;
    protected Integer performanceLatency;
    protected Integer performanceBandwidth;
    protected long timeoutInterval;
    protected int unlockTimeout;
    public SocketProperties() {
        this.processorCache = 500;
        this.eventCache = 500;
        this.directBuffer = false;
        this.directSslBuffer = false;
        this.rxBufSize = null;
        this.txBufSize = null;
        this.appReadBufSize = 8192;
        this.appWriteBufSize = 8192;
        this.bufferPool = 500;
        this.bufferPoolSize = 104857600;
        this.tcpNoDelay = Boolean.TRUE;
        this.soKeepAlive = null;
        this.ooBInline = null;
        this.soReuseAddress = null;
        this.soLingerOn = null;
        this.soLingerTime = null;
        this.soTimeout = 20000;
        this.performanceConnectionTime = null;
        this.performanceLatency = null;
        this.performanceBandwidth = null;
        this.timeoutInterval = 1000L;
        this.unlockTimeout = 250;
    }
    public void setProperties ( final Socket socket ) throws SocketException {
        if ( this.rxBufSize != null ) {
            socket.setReceiveBufferSize ( this.rxBufSize );
        }
        if ( this.txBufSize != null ) {
            socket.setSendBufferSize ( this.txBufSize );
        }
        if ( this.ooBInline != null ) {
            socket.setOOBInline ( this.ooBInline );
        }
        if ( this.soKeepAlive != null ) {
            socket.setKeepAlive ( this.soKeepAlive );
        }
        if ( this.performanceConnectionTime != null && this.performanceLatency != null && this.performanceBandwidth != null ) {
            socket.setPerformancePreferences ( this.performanceConnectionTime, this.performanceLatency, this.performanceBandwidth );
        }
        if ( this.soReuseAddress != null ) {
            socket.setReuseAddress ( this.soReuseAddress );
        }
        if ( this.soLingerOn != null && this.soLingerTime != null ) {
            socket.setSoLinger ( this.soLingerOn, this.soLingerTime );
        }
        if ( this.soTimeout != null && this.soTimeout >= 0 ) {
            socket.setSoTimeout ( this.soTimeout );
        }
        if ( this.tcpNoDelay != null ) {
            socket.setTcpNoDelay ( this.tcpNoDelay );
        }
    }
    public void setProperties ( final ServerSocket socket ) throws SocketException {
        if ( this.rxBufSize != null ) {
            socket.setReceiveBufferSize ( this.rxBufSize );
        }
        if ( this.performanceConnectionTime != null && this.performanceLatency != null && this.performanceBandwidth != null ) {
            socket.setPerformancePreferences ( this.performanceConnectionTime, this.performanceLatency, this.performanceBandwidth );
        }
        if ( this.soReuseAddress != null ) {
            socket.setReuseAddress ( this.soReuseAddress );
        }
        if ( this.soTimeout != null && this.soTimeout >= 0 ) {
            socket.setSoTimeout ( this.soTimeout );
        }
    }
    public void setProperties ( final AsynchronousSocketChannel socket ) throws IOException {
        if ( this.rxBufSize != null ) {
            socket.setOption ( StandardSocketOptions.SO_RCVBUF, this.rxBufSize );
        }
        if ( this.txBufSize != null ) {
            socket.setOption ( StandardSocketOptions.SO_SNDBUF, this.txBufSize );
        }
        if ( this.soKeepAlive != null ) {
            socket.setOption ( StandardSocketOptions.SO_KEEPALIVE, this.soKeepAlive );
        }
        if ( this.soReuseAddress != null ) {
            socket.setOption ( StandardSocketOptions.SO_REUSEADDR, this.soReuseAddress );
        }
        if ( this.soLingerOn != null && this.soLingerOn && this.soLingerTime != null ) {
            socket.setOption ( StandardSocketOptions.SO_LINGER, this.soLingerTime );
        }
        if ( this.tcpNoDelay != null ) {
            socket.setOption ( StandardSocketOptions.TCP_NODELAY, this.tcpNoDelay );
        }
    }
    public void setProperties ( final AsynchronousServerSocketChannel socket ) throws IOException {
        if ( this.rxBufSize != null ) {
            socket.setOption ( StandardSocketOptions.SO_RCVBUF, this.rxBufSize );
        }
        if ( this.soReuseAddress != null ) {
            socket.setOption ( StandardSocketOptions.SO_REUSEADDR, this.soReuseAddress );
        }
    }
    public boolean getDirectBuffer() {
        return this.directBuffer;
    }
    public boolean getDirectSslBuffer() {
        return this.directSslBuffer;
    }
    public boolean getOoBInline() {
        return this.ooBInline;
    }
    public int getPerformanceBandwidth() {
        return this.performanceBandwidth;
    }
    public int getPerformanceConnectionTime() {
        return this.performanceConnectionTime;
    }
    public int getPerformanceLatency() {
        return this.performanceLatency;
    }
    public int getRxBufSize() {
        return this.rxBufSize;
    }
    public boolean getSoKeepAlive() {
        return this.soKeepAlive;
    }
    public boolean getSoLingerOn() {
        return this.soLingerOn;
    }
    public int getSoLingerTime() {
        return this.soLingerTime;
    }
    public boolean getSoReuseAddress() {
        return this.soReuseAddress;
    }
    public int getSoTimeout() {
        return this.soTimeout;
    }
    public boolean getTcpNoDelay() {
        return this.tcpNoDelay;
    }
    public int getTxBufSize() {
        return this.txBufSize;
    }
    public int getBufferPool() {
        return this.bufferPool;
    }
    public int getBufferPoolSize() {
        return this.bufferPoolSize;
    }
    public int getEventCache() {
        return this.eventCache;
    }
    public int getAppReadBufSize() {
        return this.appReadBufSize;
    }
    public int getAppWriteBufSize() {
        return this.appWriteBufSize;
    }
    public int getProcessorCache() {
        return this.processorCache;
    }
    public long getTimeoutInterval() {
        return this.timeoutInterval;
    }
    public int getDirectBufferPool() {
        return this.bufferPool;
    }
    public void setPerformanceConnectionTime ( final int performanceConnectionTime ) {
        this.performanceConnectionTime = performanceConnectionTime;
    }
    public void setTxBufSize ( final int txBufSize ) {
        this.txBufSize = txBufSize;
    }
    public void setTcpNoDelay ( final boolean tcpNoDelay ) {
        this.tcpNoDelay = tcpNoDelay;
    }
    public void setSoTimeout ( final int soTimeout ) {
        this.soTimeout = soTimeout;
    }
    public void setSoReuseAddress ( final boolean soReuseAddress ) {
        this.soReuseAddress = soReuseAddress;
    }
    public void setSoLingerTime ( final int soLingerTime ) {
        this.soLingerTime = soLingerTime;
    }
    public void setSoKeepAlive ( final boolean soKeepAlive ) {
        this.soKeepAlive = soKeepAlive;
    }
    public void setRxBufSize ( final int rxBufSize ) {
        this.rxBufSize = rxBufSize;
    }
    public void setPerformanceLatency ( final int performanceLatency ) {
        this.performanceLatency = performanceLatency;
    }
    public void setPerformanceBandwidth ( final int performanceBandwidth ) {
        this.performanceBandwidth = performanceBandwidth;
    }
    public void setOoBInline ( final boolean ooBInline ) {
        this.ooBInline = ooBInline;
    }
    public void setDirectBuffer ( final boolean directBuffer ) {
        this.directBuffer = directBuffer;
    }
    public void setDirectSslBuffer ( final boolean directSslBuffer ) {
        this.directSslBuffer = directSslBuffer;
    }
    public void setSoLingerOn ( final boolean soLingerOn ) {
        this.soLingerOn = soLingerOn;
    }
    public void setBufferPool ( final int bufferPool ) {
        this.bufferPool = bufferPool;
    }
    public void setBufferPoolSize ( final int bufferPoolSize ) {
        this.bufferPoolSize = bufferPoolSize;
    }
    public void setEventCache ( final int eventCache ) {
        this.eventCache = eventCache;
    }
    public void setAppReadBufSize ( final int appReadBufSize ) {
        this.appReadBufSize = appReadBufSize;
    }
    public void setAppWriteBufSize ( final int appWriteBufSize ) {
        this.appWriteBufSize = appWriteBufSize;
    }
    public void setProcessorCache ( final int processorCache ) {
        this.processorCache = processorCache;
    }
    public void setTimeoutInterval ( final long timeoutInterval ) {
        this.timeoutInterval = timeoutInterval;
    }
    public void setDirectBufferPool ( final int directBufferPool ) {
        this.bufferPool = directBufferPool;
    }
    public int getUnlockTimeout() {
        return this.unlockTimeout;
    }
    public void setUnlockTimeout ( final int unlockTimeout ) {
        this.unlockTimeout = unlockTimeout;
    }
}

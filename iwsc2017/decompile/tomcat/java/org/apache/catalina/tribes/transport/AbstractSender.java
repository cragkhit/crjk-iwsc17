package org.apache.catalina.tribes.transport;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.catalina.tribes.Member;
public abstract class AbstractSender implements DataSender {
    private volatile boolean connected;
    private int rxBufSize;
    private int txBufSize;
    private int udpRxBufSize;
    private int udpTxBufSize;
    private boolean directBuffer;
    private int keepAliveCount;
    private int requestCount;
    private long connectTime;
    private long keepAliveTime;
    private long timeout;
    private Member destination;
    private InetAddress address;
    private int port;
    private int maxRetryAttempts;
    private int attempt;
    private boolean tcpNoDelay;
    private boolean soKeepAlive;
    private boolean ooBInline;
    private boolean soReuseAddress;
    private boolean soLingerOn;
    private int soLingerTime;
    private int soTrafficClass;
    private boolean throwOnFailedAck;
    private boolean udpBased;
    private int udpPort;
    public static void transferProperties ( final AbstractSender from, final AbstractSender to ) {
        to.rxBufSize = from.rxBufSize;
        to.txBufSize = from.txBufSize;
        to.directBuffer = from.directBuffer;
        to.keepAliveCount = from.keepAliveCount;
        to.keepAliveTime = from.keepAliveTime;
        to.timeout = from.timeout;
        to.destination = from.destination;
        to.address = from.address;
        to.port = from.port;
        to.maxRetryAttempts = from.maxRetryAttempts;
        to.tcpNoDelay = from.tcpNoDelay;
        to.soKeepAlive = from.soKeepAlive;
        to.ooBInline = from.ooBInline;
        to.soReuseAddress = from.soReuseAddress;
        to.soLingerOn = from.soLingerOn;
        to.soLingerTime = from.soLingerTime;
        to.soTrafficClass = from.soTrafficClass;
        to.throwOnFailedAck = from.throwOnFailedAck;
        to.udpBased = from.udpBased;
        to.udpPort = from.udpPort;
    }
    public AbstractSender() {
        this.connected = false;
        this.rxBufSize = 25188;
        this.txBufSize = 43800;
        this.udpRxBufSize = 25188;
        this.udpTxBufSize = 43800;
        this.directBuffer = false;
        this.keepAliveCount = -1;
        this.requestCount = 0;
        this.keepAliveTime = -1L;
        this.timeout = 3000L;
        this.maxRetryAttempts = 1;
        this.tcpNoDelay = true;
        this.soKeepAlive = false;
        this.ooBInline = true;
        this.soReuseAddress = true;
        this.soLingerOn = false;
        this.soLingerTime = 3;
        this.soTrafficClass = 28;
        this.throwOnFailedAck = true;
        this.udpBased = false;
        this.udpPort = -1;
    }
    @Override
    public abstract void connect() throws IOException;
    @Override
    public abstract void disconnect();
    @Override
    public boolean keepalive() {
        boolean disconnect = false;
        if ( this.isUdpBased() ) {
            disconnect = true;
        } else if ( this.keepAliveCount >= 0 && this.requestCount > this.keepAliveCount ) {
            disconnect = true;
        } else if ( this.keepAliveTime >= 0L && System.currentTimeMillis() - this.connectTime > this.keepAliveTime ) {
            disconnect = true;
        }
        if ( disconnect ) {
            this.disconnect();
        }
        return disconnect;
    }
    protected void setConnected ( final boolean connected ) {
        this.connected = connected;
    }
    @Override
    public boolean isConnected() {
        return this.connected;
    }
    @Override
    public long getConnectTime() {
        return this.connectTime;
    }
    public Member getDestination() {
        return this.destination;
    }
    public int getKeepAliveCount() {
        return this.keepAliveCount;
    }
    public long getKeepAliveTime() {
        return this.keepAliveTime;
    }
    @Override
    public int getRequestCount() {
        return this.requestCount;
    }
    public int getRxBufSize() {
        return this.rxBufSize;
    }
    public long getTimeout() {
        return this.timeout;
    }
    public int getTxBufSize() {
        return this.txBufSize;
    }
    public InetAddress getAddress() {
        return this.address;
    }
    public int getPort() {
        return this.port;
    }
    public int getMaxRetryAttempts() {
        return this.maxRetryAttempts;
    }
    public void setDirectBuffer ( final boolean directBuffer ) {
        this.directBuffer = directBuffer;
    }
    public boolean getDirectBuffer() {
        return this.directBuffer;
    }
    public int getAttempt() {
        return this.attempt;
    }
    public boolean getTcpNoDelay() {
        return this.tcpNoDelay;
    }
    public boolean getSoKeepAlive() {
        return this.soKeepAlive;
    }
    public boolean getOoBInline() {
        return this.ooBInline;
    }
    public boolean getSoReuseAddress() {
        return this.soReuseAddress;
    }
    public boolean getSoLingerOn() {
        return this.soLingerOn;
    }
    public int getSoLingerTime() {
        return this.soLingerTime;
    }
    public int getSoTrafficClass() {
        return this.soTrafficClass;
    }
    public boolean getThrowOnFailedAck() {
        return this.throwOnFailedAck;
    }
    @Override
    public void setKeepAliveCount ( final int keepAliveCount ) {
        this.keepAliveCount = keepAliveCount;
    }
    @Override
    public void setKeepAliveTime ( final long keepAliveTime ) {
        this.keepAliveTime = keepAliveTime;
    }
    public void setRequestCount ( final int requestCount ) {
        this.requestCount = requestCount;
    }
    @Override
    public void setRxBufSize ( final int rxBufSize ) {
        this.rxBufSize = rxBufSize;
    }
    @Override
    public void setTimeout ( final long timeout ) {
        this.timeout = timeout;
    }
    @Override
    public void setTxBufSize ( final int txBufSize ) {
        this.txBufSize = txBufSize;
    }
    public void setConnectTime ( final long connectTime ) {
        this.connectTime = connectTime;
    }
    public void setMaxRetryAttempts ( final int maxRetryAttempts ) {
        this.maxRetryAttempts = maxRetryAttempts;
    }
    public void setAttempt ( final int attempt ) {
        this.attempt = attempt;
    }
    public void setTcpNoDelay ( final boolean tcpNoDelay ) {
        this.tcpNoDelay = tcpNoDelay;
    }
    public void setSoKeepAlive ( final boolean soKeepAlive ) {
        this.soKeepAlive = soKeepAlive;
    }
    public void setOoBInline ( final boolean ooBInline ) {
        this.ooBInline = ooBInline;
    }
    public void setSoReuseAddress ( final boolean soReuseAddress ) {
        this.soReuseAddress = soReuseAddress;
    }
    public void setSoLingerOn ( final boolean soLingerOn ) {
        this.soLingerOn = soLingerOn;
    }
    public void setSoLingerTime ( final int soLingerTime ) {
        this.soLingerTime = soLingerTime;
    }
    public void setSoTrafficClass ( final int soTrafficClass ) {
        this.soTrafficClass = soTrafficClass;
    }
    public void setThrowOnFailedAck ( final boolean throwOnFailedAck ) {
        this.throwOnFailedAck = throwOnFailedAck;
    }
    public void setDestination ( final Member destination ) throws UnknownHostException {
        this.destination = destination;
        this.address = InetAddress.getByAddress ( destination.getHost() );
        this.port = destination.getPort();
        this.udpPort = destination.getUdpPort();
    }
    public void setPort ( final int port ) {
        this.port = port;
    }
    public void setAddress ( final InetAddress address ) {
        this.address = address;
    }
    public boolean isUdpBased() {
        return this.udpBased;
    }
    public void setUdpBased ( final boolean udpBased ) {
        this.udpBased = udpBased;
    }
    public int getUdpPort() {
        return this.udpPort;
    }
    public void setUdpPort ( final int udpPort ) {
        this.udpPort = udpPort;
    }
    public int getUdpRxBufSize() {
        return this.udpRxBufSize;
    }
    public void setUdpRxBufSize ( final int udpRxBufSize ) {
        this.udpRxBufSize = udpRxBufSize;
    }
    public int getUdpTxBufSize() {
        return this.udpTxBufSize;
    }
    public void setUdpTxBufSize ( final int udpTxBufSize ) {
        this.udpTxBufSize = udpTxBufSize;
    }
}

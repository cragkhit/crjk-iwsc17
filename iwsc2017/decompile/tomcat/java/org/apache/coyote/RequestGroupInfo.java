package org.apache.coyote;
import java.util.Iterator;
import java.util.ArrayList;
public class RequestGroupInfo {
    private final ArrayList<RequestInfo> processors;
    private long deadMaxTime;
    private long deadProcessingTime;
    private int deadRequestCount;
    private int deadErrorCount;
    private long deadBytesReceived;
    private long deadBytesSent;
    public RequestGroupInfo() {
        this.processors = new ArrayList<RequestInfo>();
        this.deadMaxTime = 0L;
        this.deadProcessingTime = 0L;
        this.deadRequestCount = 0;
        this.deadErrorCount = 0;
        this.deadBytesReceived = 0L;
        this.deadBytesSent = 0L;
    }
    public synchronized void addRequestProcessor ( final RequestInfo rp ) {
        this.processors.add ( rp );
    }
    public synchronized void removeRequestProcessor ( final RequestInfo rp ) {
        if ( rp != null ) {
            if ( this.deadMaxTime < rp.getMaxTime() ) {
                this.deadMaxTime = rp.getMaxTime();
            }
            this.deadProcessingTime += rp.getProcessingTime();
            this.deadRequestCount += rp.getRequestCount();
            this.deadErrorCount += rp.getErrorCount();
            this.deadBytesReceived += rp.getBytesReceived();
            this.deadBytesSent += rp.getBytesSent();
            this.processors.remove ( rp );
        }
    }
    public synchronized long getMaxTime() {
        long maxTime = this.deadMaxTime;
        for ( final RequestInfo rp : this.processors ) {
            if ( maxTime < rp.getMaxTime() ) {
                maxTime = rp.getMaxTime();
            }
        }
        return maxTime;
    }
    public synchronized void setMaxTime ( final long maxTime ) {
        this.deadMaxTime = maxTime;
        for ( final RequestInfo rp : this.processors ) {
            rp.setMaxTime ( maxTime );
        }
    }
    public synchronized long getProcessingTime() {
        long time = this.deadProcessingTime;
        for ( final RequestInfo rp : this.processors ) {
            time += rp.getProcessingTime();
        }
        return time;
    }
    public synchronized void setProcessingTime ( final long totalTime ) {
        this.deadProcessingTime = totalTime;
        for ( final RequestInfo rp : this.processors ) {
            rp.setProcessingTime ( totalTime );
        }
    }
    public synchronized int getRequestCount() {
        int requestCount = this.deadRequestCount;
        for ( final RequestInfo rp : this.processors ) {
            requestCount += rp.getRequestCount();
        }
        return requestCount;
    }
    public synchronized void setRequestCount ( final int requestCount ) {
        this.deadRequestCount = requestCount;
        for ( final RequestInfo rp : this.processors ) {
            rp.setRequestCount ( requestCount );
        }
    }
    public synchronized int getErrorCount() {
        int requestCount = this.deadErrorCount;
        for ( final RequestInfo rp : this.processors ) {
            requestCount += rp.getErrorCount();
        }
        return requestCount;
    }
    public synchronized void setErrorCount ( final int errorCount ) {
        this.deadErrorCount = errorCount;
        for ( final RequestInfo rp : this.processors ) {
            rp.setErrorCount ( errorCount );
        }
    }
    public synchronized long getBytesReceived() {
        long bytes = this.deadBytesReceived;
        for ( final RequestInfo rp : this.processors ) {
            bytes += rp.getBytesReceived();
        }
        return bytes;
    }
    public synchronized void setBytesReceived ( final long bytesReceived ) {
        this.deadBytesReceived = bytesReceived;
        for ( final RequestInfo rp : this.processors ) {
            rp.setBytesReceived ( bytesReceived );
        }
    }
    public synchronized long getBytesSent() {
        long bytes = this.deadBytesSent;
        for ( final RequestInfo rp : this.processors ) {
            bytes += rp.getBytesSent();
        }
        return bytes;
    }
    public synchronized void setBytesSent ( final long bytesSent ) {
        this.deadBytesSent = bytesSent;
        for ( final RequestInfo rp : this.processors ) {
            rp.setBytesSent ( bytesSent );
        }
    }
    public void resetCounters() {
        this.setBytesReceived ( 0L );
        this.setBytesSent ( 0L );
        this.setRequestCount ( 0 );
        this.setProcessingTime ( 0L );
        this.setMaxTime ( 0L );
        this.setErrorCount ( 0 );
    }
}

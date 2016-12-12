package org.apache.coyote.http2;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
private class PingManager {
    private final long pingIntervalNano = 10000000000L;
    private int sequence;
    private long lastPingNanoTime;
    private Queue<PingRecord> inflightPings;
    private Queue<Long> roundTripTimes;
    private PingManager() {
        this.sequence = 0;
        this.lastPingNanoTime = Long.MIN_VALUE;
        this.inflightPings = new ConcurrentLinkedQueue<PingRecord>();
        this.roundTripTimes = new ConcurrentLinkedQueue<Long>();
    }
    public void sendPing ( final boolean force ) throws IOException {
        final long now = System.nanoTime();
        if ( force || now - this.lastPingNanoTime > 10000000000L ) {
            this.lastPingNanoTime = now;
            final byte[] payload = new byte[8];
            synchronized ( Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ) ) {
                final int sentSequence = ++this.sequence;
                final PingRecord pingRecord = new PingRecord ( sentSequence, now );
                this.inflightPings.add ( pingRecord );
                ByteUtil.set31Bits ( payload, 4, sentSequence );
                Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ).write ( true, Http2UpgradeHandler.access$200(), 0, Http2UpgradeHandler.access$200().length );
                Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ).write ( true, payload, 0, payload.length );
                Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ).flush ( true );
            }
        }
    }
    public void receivePing ( final byte[] payload, final boolean ack ) throws IOException {
        if ( ack ) {
            int receivedSequence;
            PingRecord pingRecord;
            for ( receivedSequence = ByteUtil.get31Bits ( payload, 4 ), pingRecord = this.inflightPings.poll(); pingRecord != null && pingRecord.getSequence() < receivedSequence; pingRecord = this.inflightPings.poll() ) {}
            if ( pingRecord != null ) {
                final long roundTripTime = System.nanoTime() - pingRecord.getSentNanoTime();
                this.roundTripTimes.add ( roundTripTime );
                while ( this.roundTripTimes.size() > 3 ) {
                    this.roundTripTimes.poll();
                }
                if ( Http2UpgradeHandler.access$300().isDebugEnabled() ) {
                    Http2UpgradeHandler.access$300().debug ( Http2UpgradeHandler.access$500().getString ( "pingManager.roundTripTime", Http2UpgradeHandler.access$400 ( Http2UpgradeHandler.this ), roundTripTime ) );
                }
            }
        } else {
            synchronized ( Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ) ) {
                Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ).write ( true, Http2UpgradeHandler.access$600(), 0, Http2UpgradeHandler.access$600().length );
                Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ).write ( true, payload, 0, payload.length );
                Http2UpgradeHandler.access$100 ( Http2UpgradeHandler.this ).flush ( true );
            }
        }
    }
    public long getRoundTripTimeNano() {
        return ( long ) this.roundTripTimes.stream().mapToLong ( x -> x ).average().orElse ( 0.0 );
    }
}

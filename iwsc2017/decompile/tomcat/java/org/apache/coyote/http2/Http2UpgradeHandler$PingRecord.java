package org.apache.coyote.http2;
private static class PingRecord {
    private final int sequence;
    private final long sentNanoTime;
    public PingRecord ( final int sequence, final long sentNanoTime ) {
        this.sequence = sequence;
        this.sentNanoTime = sentNanoTime;
    }
    public int getSequence() {
        return this.sequence;
    }
    public long getSentNanoTime() {
        return this.sentNanoTime;
    }
}

package org.apache.catalina.authenticator;
public static class NonceInfo {
    private final long timestamp;
    private final boolean[] seen;
    private final int offset;
    private int count;
    public NonceInfo ( final long currentTime, final int seenWindowSize ) {
        this.count = 0;
        this.timestamp = currentTime;
        this.seen = new boolean[seenWindowSize];
        this.offset = seenWindowSize / 2;
    }
    public synchronized boolean nonceCountValid ( final long nonceCount ) {
        if ( this.count - this.offset >= nonceCount || nonceCount > this.count - this.offset + this.seen.length ) {
            return false;
        }
        final int checkIndex = ( int ) ( ( nonceCount + this.offset ) % this.seen.length );
        if ( this.seen[checkIndex] ) {
            return false;
        }
        this.seen[checkIndex] = true;
        this.seen[this.count % this.seen.length] = false;
        ++this.count;
        return true;
    }
    public long getTimestamp() {
        return this.timestamp;
    }
}

package org.apache.catalina.session;
protected static final class SessionTiming {
    private final long timestamp;
    private final int duration;
    public SessionTiming ( final long timestamp, final int duration ) {
        this.timestamp = timestamp;
        this.duration = duration;
    }
    public long getTimestamp() {
        return this.timestamp;
    }
    public int getDuration() {
        return this.duration;
    }
}

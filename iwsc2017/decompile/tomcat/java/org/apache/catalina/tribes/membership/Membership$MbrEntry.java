package org.apache.catalina.tribes.membership;
import org.apache.catalina.tribes.Member;
protected static class MbrEntry {
    protected final Member mbr;
    protected long lastHeardFrom;
    public MbrEntry ( final Member mbr ) {
        this.mbr = mbr;
    }
    public void accessed() {
        this.lastHeardFrom = System.currentTimeMillis();
    }
    public Member getMember() {
        return this.mbr;
    }
    public boolean hasExpired ( final long maxtime ) {
        final long delta = System.currentTimeMillis() - this.lastHeardFrom;
        return delta > maxtime;
    }
}

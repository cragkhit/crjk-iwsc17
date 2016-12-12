package org.apache.tomcat.dbcp.pool2;
private static final class ErodingFactor {
    private final float factor;
    private transient volatile long nextShrink;
    private transient volatile int idleHighWaterMark;
    public ErodingFactor ( final float factor ) {
        this.factor = factor;
        this.nextShrink = System.currentTimeMillis() + ( long ) ( 900000.0f * factor );
        this.idleHighWaterMark = 1;
    }
    public void update ( final long now, final int numIdle ) {
        final int idle = Math.max ( 0, numIdle );
        this.idleHighWaterMark = Math.max ( idle, this.idleHighWaterMark );
        final float maxInterval = 15.0f;
        final float minutes = 15.0f + -14.0f / this.idleHighWaterMark * idle;
        this.nextShrink = now + ( long ) ( minutes * 60000.0f * this.factor );
    }
    public long getNextShrink() {
        return this.nextShrink;
    }
    @Override
    public String toString() {
        return "ErodingFactor{factor=" + this.factor + ", idleHighWaterMark=" + this.idleHighWaterMark + '}';
    }
}

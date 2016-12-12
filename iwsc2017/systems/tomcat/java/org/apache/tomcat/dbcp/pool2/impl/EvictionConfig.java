package org.apache.tomcat.dbcp.pool2.impl;
public class EvictionConfig {
    private final long idleEvictTime;
    private final long idleSoftEvictTime;
    private final int minIdle;
    public EvictionConfig ( final long poolIdleEvictTime, final long poolIdleSoftEvictTime,
                            final int minIdle ) {
        if ( poolIdleEvictTime > 0 ) {
            idleEvictTime = poolIdleEvictTime;
        } else {
            idleEvictTime = Long.MAX_VALUE;
        }
        if ( poolIdleSoftEvictTime > 0 ) {
            idleSoftEvictTime = poolIdleSoftEvictTime;
        } else {
            idleSoftEvictTime  = Long.MAX_VALUE;
        }
        this.minIdle = minIdle;
    }
    public long getIdleEvictTime() {
        return idleEvictTime;
    }
    public long getIdleSoftEvictTime() {
        return idleSoftEvictTime;
    }
    public int getMinIdle() {
        return minIdle;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "EvictionConfig [idleEvictTime=" );
        builder.append ( idleEvictTime );
        builder.append ( ", idleSoftEvictTime=" );
        builder.append ( idleSoftEvictTime );
        builder.append ( ", minIdle=" );
        builder.append ( minIdle );
        builder.append ( "]" );
        return builder.toString();
    }
}

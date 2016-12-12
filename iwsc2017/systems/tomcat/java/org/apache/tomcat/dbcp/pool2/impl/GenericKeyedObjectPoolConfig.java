package org.apache.tomcat.dbcp.pool2.impl;
public class GenericKeyedObjectPoolConfig extends BaseObjectPoolConfig {
    public static final int DEFAULT_MAX_TOTAL_PER_KEY = 8;
    public static final int DEFAULT_MAX_TOTAL = -1;
    public static final int DEFAULT_MIN_IDLE_PER_KEY = 0;
    public static final int DEFAULT_MAX_IDLE_PER_KEY = 8;
    private int minIdlePerKey = DEFAULT_MIN_IDLE_PER_KEY;
    private int maxIdlePerKey = DEFAULT_MAX_IDLE_PER_KEY;
    private int maxTotalPerKey = DEFAULT_MAX_TOTAL_PER_KEY;
    private int maxTotal = DEFAULT_MAX_TOTAL;
    public GenericKeyedObjectPoolConfig() {
    }
    public int getMaxTotal() {
        return maxTotal;
    }
    public void setMaxTotal ( final int maxTotal ) {
        this.maxTotal = maxTotal;
    }
    public int getMaxTotalPerKey() {
        return maxTotalPerKey;
    }
    public void setMaxTotalPerKey ( final int maxTotalPerKey ) {
        this.maxTotalPerKey = maxTotalPerKey;
    }
    public int getMinIdlePerKey() {
        return minIdlePerKey;
    }
    public void setMinIdlePerKey ( final int minIdlePerKey ) {
        this.minIdlePerKey = minIdlePerKey;
    }
    public int getMaxIdlePerKey() {
        return maxIdlePerKey;
    }
    public void setMaxIdlePerKey ( final int maxIdlePerKey ) {
        this.maxIdlePerKey = maxIdlePerKey;
    }
    @Override
    public GenericKeyedObjectPoolConfig clone() {
        try {
            return ( GenericKeyedObjectPoolConfig ) super.clone();
        } catch ( final CloneNotSupportedException e ) {
            throw new AssertionError();
        }
    }
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        super.toStringAppendFields ( builder );
        builder.append ( ", minIdlePerKey=" );
        builder.append ( minIdlePerKey );
        builder.append ( ", maxIdlePerKey=" );
        builder.append ( maxIdlePerKey );
        builder.append ( ", maxTotalPerKey=" );
        builder.append ( maxTotalPerKey );
        builder.append ( ", maxTotal=" );
        builder.append ( maxTotal );
    }
}

package org.apache.tomcat.dbcp.pool2.impl;
public class GenericObjectPoolConfig extends BaseObjectPoolConfig {
    public static final int DEFAULT_MAX_TOTAL = 8;
    public static final int DEFAULT_MAX_IDLE = 8;
    public static final int DEFAULT_MIN_IDLE = 0;
    private int maxTotal = DEFAULT_MAX_TOTAL;
    private int maxIdle = DEFAULT_MAX_IDLE;
    private int minIdle = DEFAULT_MIN_IDLE;
    public int getMaxTotal() {
        return maxTotal;
    }
    public void setMaxTotal ( final int maxTotal ) {
        this.maxTotal = maxTotal;
    }
    public int getMaxIdle() {
        return maxIdle;
    }
    public void setMaxIdle ( final int maxIdle ) {
        this.maxIdle = maxIdle;
    }
    public int getMinIdle() {
        return minIdle;
    }
    public void setMinIdle ( final int minIdle ) {
        this.minIdle = minIdle;
    }
    @Override
    public GenericObjectPoolConfig clone() {
        try {
            return ( GenericObjectPoolConfig ) super.clone();
        } catch ( final CloneNotSupportedException e ) {
            throw new AssertionError();
        }
    }
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        super.toStringAppendFields ( builder );
        builder.append ( ", maxTotal=" );
        builder.append ( maxTotal );
        builder.append ( ", maxIdle=" );
        builder.append ( maxIdle );
        builder.append ( ", minIdle=" );
        builder.append ( minIdle );
    }
}

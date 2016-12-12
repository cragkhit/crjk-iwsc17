package org.apache.tomcat.dbcp.pool2.impl;
import org.apache.tomcat.dbcp.pool2.BaseObject;
public abstract class BaseObjectPoolConfig extends BaseObject implements Cloneable {
    public static final boolean DEFAULT_LIFO = true;
    public static final boolean DEFAULT_FAIRNESS = false;
    public static final long DEFAULT_MAX_WAIT_MILLIS = -1L;
    public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS =
        1000L * 60L * 30L;
    public static final long DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS = -1;
    public static final int DEFAULT_NUM_TESTS_PER_EVICTION_RUN = 3;
    public static final boolean DEFAULT_TEST_ON_CREATE = false;
    public static final boolean DEFAULT_TEST_ON_BORROW = false;
    public static final boolean DEFAULT_TEST_ON_RETURN = false;
    public static final boolean DEFAULT_TEST_WHILE_IDLE = false;
    public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = -1L;
    public static final boolean DEFAULT_BLOCK_WHEN_EXHAUSTED = true;
    public static final boolean DEFAULT_JMX_ENABLE = true;
    public static final String DEFAULT_JMX_NAME_PREFIX = "pool";
    public static final String DEFAULT_JMX_NAME_BASE = null;
    public static final String DEFAULT_EVICTION_POLICY_CLASS_NAME =
        "org.apache.tomcat.dbcp.pool2.impl.DefaultEvictionPolicy";
    private boolean lifo = DEFAULT_LIFO;
    private boolean fairness = DEFAULT_FAIRNESS;
    private long maxWaitMillis = DEFAULT_MAX_WAIT_MILLIS;
    private long minEvictableIdleTimeMillis =
        DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private long softMinEvictableIdleTimeMillis =
        DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private int numTestsPerEvictionRun =
        DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    private String evictionPolicyClassName = DEFAULT_EVICTION_POLICY_CLASS_NAME;
    private boolean testOnCreate = DEFAULT_TEST_ON_CREATE;
    private boolean testOnBorrow = DEFAULT_TEST_ON_BORROW;
    private boolean testOnReturn = DEFAULT_TEST_ON_RETURN;
    private boolean testWhileIdle = DEFAULT_TEST_WHILE_IDLE;
    private long timeBetweenEvictionRunsMillis =
        DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private boolean blockWhenExhausted = DEFAULT_BLOCK_WHEN_EXHAUSTED;
    private boolean jmxEnabled = DEFAULT_JMX_ENABLE;
    private String jmxNamePrefix = DEFAULT_JMX_NAME_PREFIX;
    private String jmxNameBase = DEFAULT_JMX_NAME_BASE;
    public boolean getLifo() {
        return lifo;
    }
    public boolean getFairness() {
        return fairness;
    }
    public void setLifo ( final boolean lifo ) {
        this.lifo = lifo;
    }
    public void setFairness ( final boolean fairness ) {
        this.fairness = fairness;
    }
    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }
    public void setMaxWaitMillis ( final long maxWaitMillis ) {
        this.maxWaitMillis = maxWaitMillis;
    }
    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }
    public void setMinEvictableIdleTimeMillis ( final long minEvictableIdleTimeMillis ) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }
    public void setSoftMinEvictableIdleTimeMillis (
        final long softMinEvictableIdleTimeMillis ) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }
    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }
    public void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public boolean getTestOnCreate() {
        return testOnCreate;
    }
    public void setTestOnCreate ( final boolean testOnCreate ) {
        this.testOnCreate = testOnCreate;
    }
    public boolean getTestOnBorrow() {
        return testOnBorrow;
    }
    public void setTestOnBorrow ( final boolean testOnBorrow ) {
        this.testOnBorrow = testOnBorrow;
    }
    public boolean getTestOnReturn() {
        return testOnReturn;
    }
    public void setTestOnReturn ( final boolean testOnReturn ) {
        this.testOnReturn = testOnReturn;
    }
    public boolean getTestWhileIdle() {
        return testWhileIdle;
    }
    public void setTestWhileIdle ( final boolean testWhileIdle ) {
        this.testWhileIdle = testWhileIdle;
    }
    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }
    public void setTimeBetweenEvictionRunsMillis (
        final long timeBetweenEvictionRunsMillis ) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
    public String getEvictionPolicyClassName() {
        return evictionPolicyClassName;
    }
    public void setEvictionPolicyClassName ( final String evictionPolicyClassName ) {
        this.evictionPolicyClassName = evictionPolicyClassName;
    }
    public boolean getBlockWhenExhausted() {
        return blockWhenExhausted;
    }
    public void setBlockWhenExhausted ( final boolean blockWhenExhausted ) {
        this.blockWhenExhausted = blockWhenExhausted;
    }
    public boolean getJmxEnabled() {
        return jmxEnabled;
    }
    public void setJmxEnabled ( final boolean jmxEnabled ) {
        this.jmxEnabled = jmxEnabled;
    }
    public String getJmxNameBase() {
        return jmxNameBase;
    }
    public void setJmxNameBase ( final String jmxNameBase ) {
        this.jmxNameBase = jmxNameBase;
    }
    public String getJmxNamePrefix() {
        return jmxNamePrefix;
    }
    public void setJmxNamePrefix ( final String jmxNamePrefix ) {
        this.jmxNamePrefix = jmxNamePrefix;
    }
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        builder.append ( "lifo=" );
        builder.append ( lifo );
        builder.append ( ", fairness=" );
        builder.append ( fairness );
        builder.append ( ", maxWaitMillis=" );
        builder.append ( maxWaitMillis );
        builder.append ( ", minEvictableIdleTimeMillis=" );
        builder.append ( minEvictableIdleTimeMillis );
        builder.append ( ", softMinEvictableIdleTimeMillis=" );
        builder.append ( softMinEvictableIdleTimeMillis );
        builder.append ( ", numTestsPerEvictionRun=" );
        builder.append ( numTestsPerEvictionRun );
        builder.append ( ", evictionPolicyClassName=" );
        builder.append ( evictionPolicyClassName );
        builder.append ( ", testOnCreate=" );
        builder.append ( testOnCreate );
        builder.append ( ", testOnBorrow=" );
        builder.append ( testOnBorrow );
        builder.append ( ", testOnReturn=" );
        builder.append ( testOnReturn );
        builder.append ( ", testWhileIdle=" );
        builder.append ( testWhileIdle );
        builder.append ( ", timeBetweenEvictionRunsMillis=" );
        builder.append ( timeBetweenEvictionRunsMillis );
        builder.append ( ", blockWhenExhausted=" );
        builder.append ( blockWhenExhausted );
        builder.append ( ", jmxEnabled=" );
        builder.append ( jmxEnabled );
        builder.append ( ", jmxNamePrefix=" );
        builder.append ( jmxNamePrefix );
        builder.append ( ", jmxNameBase=" );
        builder.append ( jmxNameBase );
    }
}

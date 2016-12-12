// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import javax.management.openmbean.SimpleType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.management.openmbean.OpenType;

public static class QueryStats
{
    static final String[] FIELD_NAMES;
    static final String[] FIELD_DESCRIPTIONS;
    static final OpenType<?>[] FIELD_TYPES;
    private final String query;
    private volatile int nrOfInvocations;
    private volatile long maxInvocationTime;
    private volatile long maxInvocationDate;
    private volatile long minInvocationTime;
    private volatile long minInvocationDate;
    private volatile long totalInvocationTime;
    private volatile long failures;
    private volatile int prepareCount;
    private volatile long prepareTime;
    private volatile long lastInvocation;
    
    public static String[] getFieldNames() {
        return QueryStats.FIELD_NAMES;
    }
    
    public static String[] getFieldDescriptions() {
        return QueryStats.FIELD_DESCRIPTIONS;
    }
    
    public static OpenType<?>[] getFieldTypes() {
        return QueryStats.FIELD_TYPES;
    }
    
    @Override
    public String toString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final StringBuilder buf = new StringBuilder("QueryStats[query:");
        buf.append(this.query);
        buf.append(", nrOfInvocations:");
        buf.append(this.nrOfInvocations);
        buf.append(", maxInvocationTime:");
        buf.append(this.maxInvocationTime);
        buf.append(", maxInvocationDate:");
        buf.append(sdf.format(new Date(this.maxInvocationDate)));
        buf.append(", minInvocationTime:");
        buf.append(this.minInvocationTime);
        buf.append(", minInvocationDate:");
        buf.append(sdf.format(new Date(this.minInvocationDate)));
        buf.append(", totalInvocationTime:");
        buf.append(this.totalInvocationTime);
        buf.append(", averageInvocationTime:");
        buf.append(this.totalInvocationTime / this.nrOfInvocations);
        buf.append(", failures:");
        buf.append(this.failures);
        buf.append(", prepareCount:");
        buf.append(this.prepareCount);
        buf.append(", prepareTime:");
        buf.append(this.prepareTime);
        buf.append("]");
        return buf.toString();
    }
    
    public CompositeDataSupport getCompositeData(final CompositeType type) throws OpenDataException {
        final Object[] values = { this.query, this.nrOfInvocations, this.maxInvocationTime, this.maxInvocationDate, this.minInvocationTime, this.minInvocationDate, this.totalInvocationTime, this.failures, this.prepareCount, this.prepareTime, this.lastInvocation };
        return new CompositeDataSupport(type, QueryStats.FIELD_NAMES, values);
    }
    
    public QueryStats(final String query) {
        this.maxInvocationTime = Long.MIN_VALUE;
        this.minInvocationTime = Long.MAX_VALUE;
        this.lastInvocation = 0L;
        this.query = query;
    }
    
    public void prepare(final long invocationTime) {
        ++this.prepareCount;
        this.prepareTime += invocationTime;
    }
    
    public void add(final long invocationTime, final long now) {
        this.maxInvocationTime = Math.max(invocationTime, this.maxInvocationTime);
        if (this.maxInvocationTime == invocationTime) {
            this.maxInvocationDate = now;
        }
        this.minInvocationTime = Math.min(invocationTime, this.minInvocationTime);
        if (this.minInvocationTime == invocationTime) {
            this.minInvocationDate = now;
        }
        ++this.nrOfInvocations;
        this.totalInvocationTime += invocationTime;
        this.lastInvocation = now;
    }
    
    public void failure(final long invocationTime, final long now) {
        this.add(invocationTime, now);
        ++this.failures;
    }
    
    public String getQuery() {
        return this.query;
    }
    
    public int getNrOfInvocations() {
        return this.nrOfInvocations;
    }
    
    public long getMaxInvocationTime() {
        return this.maxInvocationTime;
    }
    
    public long getMaxInvocationDate() {
        return this.maxInvocationDate;
    }
    
    public long getMinInvocationTime() {
        return this.minInvocationTime;
    }
    
    public long getMinInvocationDate() {
        return this.minInvocationDate;
    }
    
    public long getTotalInvocationTime() {
        return this.totalInvocationTime;
    }
    
    @Override
    public int hashCode() {
        return this.query.hashCode();
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other instanceof QueryStats) {
            final QueryStats qs = (QueryStats)other;
            return qs.query.equals(this.query);
        }
        return false;
    }
    
    public boolean isOlderThan(final QueryStats other) {
        return this.lastInvocation < other.lastInvocation;
    }
    
    static {
        FIELD_NAMES = new String[] { "query", "nrOfInvocations", "maxInvocationTime", "maxInvocationDate", "minInvocationTime", "minInvocationDate", "totalInvocationTime", "failures", "prepareCount", "prepareTime", "lastInvocation" };
        FIELD_DESCRIPTIONS = new String[] { "The SQL query", "The number of query invocations, a call to executeXXX", "The longest time for this query in milliseconds", "The time and date for when the longest query took place", "The shortest time for this query in milliseconds", "The time and date for when the shortest query took place", "The total amount of milliseconds spent executing this query", "The number of failures for this query", "The number of times this query was prepared (prepareStatement/prepareCall)", "The total number of milliseconds spent preparing this query", "The date and time of the last invocation" };
        FIELD_TYPES = new OpenType[] { SimpleType.STRING, SimpleType.INTEGER, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.INTEGER, SimpleType.LONG, SimpleType.LONG };
    }
}

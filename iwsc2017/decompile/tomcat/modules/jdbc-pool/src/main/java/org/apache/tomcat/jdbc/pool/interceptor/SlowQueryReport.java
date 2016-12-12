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
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.juli.logging.Log;

public class SlowQueryReport extends AbstractQueryReport
{
    private static final Log log;
    protected static final ConcurrentHashMap<String, ConcurrentHashMap<String, QueryStats>> perPoolStats;
    protected volatile ConcurrentHashMap<String, QueryStats> queries;
    protected int maxQueries;
    protected boolean logSlow;
    protected boolean logFailed;
    protected final Comparator<QueryStats> queryStatsComparator;
    
    public static ConcurrentHashMap<String, QueryStats> getPoolStats(final String poolname) {
        return SlowQueryReport.perPoolStats.get(poolname);
    }
    
    public SlowQueryReport() {
        this.queries = null;
        this.maxQueries = 1000;
        this.logSlow = true;
        this.logFailed = false;
        this.queryStatsComparator = new QueryStatsComparator();
    }
    
    public void setMaxQueries(final int maxQueries) {
        this.maxQueries = maxQueries;
    }
    
    @Override
    protected String reportFailedQuery(final String query, final Object[] args, final String name, final long start, final Throwable t) {
        final String sql = super.reportFailedQuery(query, args, name, start, t);
        if (this.maxQueries > 0) {
            final long now = System.currentTimeMillis();
            final long delta = now - start;
            final QueryStats qs = this.getQueryStats(sql);
            if (qs != null) {
                qs.failure(delta, now);
                if (this.isLogFailed() && SlowQueryReport.log.isWarnEnabled()) {
                    SlowQueryReport.log.warn((Object)("Failed Query Report SQL=" + sql + "; time=" + delta + " ms;"));
                }
            }
        }
        return sql;
    }
    
    @Override
    protected String reportQuery(final String query, final Object[] args, final String name, final long start, final long delta) {
        final String sql = super.reportQuery(query, args, name, start, delta);
        if (this.maxQueries > 0) {
            final QueryStats qs = this.getQueryStats(sql);
            if (qs != null) {
                qs.add(delta, start);
            }
        }
        return sql;
    }
    
    @Override
    protected String reportSlowQuery(final String query, final Object[] args, final String name, final long start, final long delta) {
        final String sql = super.reportSlowQuery(query, args, name, start, delta);
        if (this.maxQueries > 0) {
            final QueryStats qs = this.getQueryStats(sql);
            if (qs != null) {
                qs.add(delta, start);
                if (this.isLogSlow() && SlowQueryReport.log.isWarnEnabled()) {
                    SlowQueryReport.log.warn((Object)("Slow Query Report SQL=" + sql + "; time=" + delta + " ms;"));
                }
            }
        }
        return sql;
    }
    
    @Override
    public void closeInvoked() {
    }
    
    public void prepareStatement(final String sql, final long time) {
        if (this.maxQueries > 0) {
            final QueryStats qs = this.getQueryStats(sql);
            if (qs != null) {
                qs.prepare(time);
            }
        }
    }
    
    public void prepareCall(final String sql, final long time) {
        if (this.maxQueries > 0) {
            final QueryStats qs = this.getQueryStats(sql);
            if (qs != null) {
                qs.prepare(time);
            }
        }
    }
    
    @Override
    public void poolStarted(final ConnectionPool pool) {
        super.poolStarted(pool);
        this.queries = SlowQueryReport.perPoolStats.get(pool.getName());
        if (this.queries == null) {
            this.queries = new ConcurrentHashMap<String, QueryStats>();
            if (SlowQueryReport.perPoolStats.putIfAbsent(pool.getName(), this.queries) != null) {
                this.queries = SlowQueryReport.perPoolStats.get(pool.getName());
            }
        }
    }
    
    @Override
    public void poolClosed(final ConnectionPool pool) {
        SlowQueryReport.perPoolStats.remove(pool.getName());
        super.poolClosed(pool);
    }
    
    protected QueryStats getQueryStats(String sql) {
        if (sql == null) {
            sql = "";
        }
        final ConcurrentHashMap<String, QueryStats> queries = this.queries;
        if (queries == null) {
            if (SlowQueryReport.log.isWarnEnabled()) {
                SlowQueryReport.log.warn((Object)"Connection has already been closed or abandoned");
            }
            return null;
        }
        QueryStats qs = queries.get(sql);
        if (qs == null) {
            qs = new QueryStats(sql);
            if (queries.putIfAbsent(sql, qs) != null) {
                qs = queries.get(sql);
            }
            else if (queries.size() > this.maxQueries) {
                this.removeOldest(queries);
            }
        }
        return qs;
    }
    
    protected void removeOldest(final ConcurrentHashMap<String, QueryStats> queries) {
        final ArrayList<QueryStats> list = new ArrayList<QueryStats>(queries.values());
        Collections.sort(list, this.queryStatsComparator);
        int removeIndex = 0;
        while (queries.size() > this.maxQueries) {
            final String sql = list.get(removeIndex).getQuery();
            queries.remove(sql);
            if (SlowQueryReport.log.isDebugEnabled()) {
                SlowQueryReport.log.debug((Object)("Removing slow query, capacity reached:" + sql));
            }
            ++removeIndex;
        }
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
        super.reset(parent, con);
        if (parent != null) {
            this.queries = SlowQueryReport.perPoolStats.get(parent.getName());
        }
        else {
            this.queries = null;
        }
    }
    
    public boolean isLogSlow() {
        return this.logSlow;
    }
    
    public void setLogSlow(final boolean logSlow) {
        this.logSlow = logSlow;
    }
    
    public boolean isLogFailed() {
        return this.logFailed;
    }
    
    public void setLogFailed(final boolean logFailed) {
        this.logFailed = logFailed;
    }
    
    @Override
    public void setProperties(final Map<String, PoolProperties.InterceptorProperty> properties) {
        super.setProperties(properties);
        final String threshold = "threshold";
        final String maxqueries = "maxQueries";
        final String logslow = "logSlow";
        final String logfailed = "logFailed";
        final PoolProperties.InterceptorProperty p1 = properties.get("threshold");
        final PoolProperties.InterceptorProperty p2 = properties.get("maxQueries");
        final PoolProperties.InterceptorProperty p3 = properties.get("logSlow");
        final PoolProperties.InterceptorProperty p4 = properties.get("logFailed");
        if (p1 != null) {
            this.setThreshold(Long.parseLong(p1.getValue()));
        }
        if (p2 != null) {
            this.setMaxQueries(Integer.parseInt(p2.getValue()));
        }
        if (p3 != null) {
            this.setLogSlow(Boolean.parseBoolean(p3.getValue()));
        }
        if (p4 != null) {
            this.setLogFailed(Boolean.parseBoolean(p4.getValue()));
        }
    }
    
    static {
        log = LogFactory.getLog((Class)SlowQueryReport.class);
        perPoolStats = new ConcurrentHashMap<String, ConcurrentHashMap<String, QueryStats>>();
    }
    
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
    
    private static class QueryStatsComparator implements Comparator<QueryStats>
    {
        @Override
        public int compare(final QueryStats stats1, final QueryStats stats2) {
            return Long.compare(handleZero(stats1.lastInvocation), handleZero(stats2.lastInvocation));
        }
        
        private static long handleZero(final long value) {
            return (value == 0L) ? Long.MAX_VALUE : value;
        }
    }
}

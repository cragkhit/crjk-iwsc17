// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import org.apache.juli.logging.LogFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeData;
import java.util.Iterator;
import java.util.Set;
import javax.management.RuntimeOperationsException;
import javax.management.Notification;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import javax.management.openmbean.OpenDataException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.NotificationBroadcasterSupport;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.juli.logging.Log;
import javax.management.openmbean.CompositeType;
import javax.management.NotificationEmitter;

public class SlowQueryReportJmx extends SlowQueryReport implements NotificationEmitter, SlowQueryReportJmxMBean
{
    public static final String SLOW_QUERY_NOTIFICATION = "SLOW QUERY";
    public static final String FAILED_QUERY_NOTIFICATION = "FAILED QUERY";
    public static final String objectNameAttribute = "objectName";
    protected static volatile CompositeType SLOW_QUERY_TYPE;
    private static final Log log;
    protected static final ConcurrentHashMap<String, SlowQueryReportJmxMBean> mbeans;
    protected volatile NotificationBroadcasterSupport notifier;
    protected String poolName;
    protected static final AtomicLong notifySequence;
    protected boolean notifyPool;
    protected ConnectionPool pool;
    
    public SlowQueryReportJmx() {
        this.notifier = new NotificationBroadcasterSupport();
        this.poolName = null;
        this.notifyPool = true;
        this.pool = null;
    }
    
    @Override
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
        this.notifier.addNotificationListener(listener, filter, handback);
    }
    
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return this.notifier.getNotificationInfo();
    }
    
    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        this.notifier.removeNotificationListener(listener);
    }
    
    @Override
    public void removeNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws ListenerNotFoundException {
        this.notifier.removeNotificationListener(listener, filter, handback);
    }
    
    protected static CompositeType getCompositeType() {
        if (SlowQueryReportJmx.SLOW_QUERY_TYPE == null) {
            try {
                SlowQueryReportJmx.SLOW_QUERY_TYPE = new CompositeType(SlowQueryReportJmx.class.getName(), "Composite data type for query statistics", QueryStats.getFieldNames(), QueryStats.getFieldDescriptions(), QueryStats.getFieldTypes());
            }
            catch (OpenDataException x) {
                SlowQueryReportJmx.log.warn((Object)"Unable to initialize composite data type for JMX stats and notifications.", (Throwable)x);
            }
        }
        return SlowQueryReportJmx.SLOW_QUERY_TYPE;
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
        super.reset(parent, con);
        if (parent != null) {
            this.poolName = parent.getName();
            this.pool = parent;
            this.registerJmx();
        }
    }
    
    @Override
    public void poolClosed(final ConnectionPool pool) {
        this.poolName = pool.getName();
        this.deregisterJmx();
        super.poolClosed(pool);
    }
    
    @Override
    public void poolStarted(final ConnectionPool pool) {
        super.poolStarted(this.pool = pool);
        this.poolName = pool.getName();
    }
    
    @Override
    protected String reportFailedQuery(String query, final Object[] args, final String name, final long start, final Throwable t) {
        query = super.reportFailedQuery(query, args, name, start, t);
        if (this.isLogFailed()) {
            this.notifyJmx(query, "FAILED QUERY");
        }
        return query;
    }
    
    protected void notifyJmx(final String query, final String type) {
        try {
            final long sequence = SlowQueryReportJmx.notifySequence.incrementAndGet();
            if (this.isNotifyPool()) {
                if (this.pool != null && this.pool.getJmxPool() != null) {
                    this.pool.getJmxPool().notify(type, query);
                }
            }
            else if (this.notifier != null) {
                final Notification notification = new Notification(type, this, sequence, System.currentTimeMillis(), query);
                this.notifier.sendNotification(notification);
            }
        }
        catch (RuntimeOperationsException e) {
            if (SlowQueryReportJmx.log.isDebugEnabled()) {
                SlowQueryReportJmx.log.debug((Object)"Unable to send failed query notification.", (Throwable)e);
            }
        }
    }
    
    @Override
    protected String reportSlowQuery(String query, final Object[] args, final String name, final long start, final long delta) {
        query = super.reportSlowQuery(query, args, name, start, delta);
        if (this.isLogSlow()) {
            this.notifyJmx(query, "SLOW QUERY");
        }
        return query;
    }
    
    public String[] getPoolNames() {
        final Set<String> keys = SlowQueryReportJmx.perPoolStats.keySet();
        return keys.toArray(new String[0]);
    }
    
    public String getPoolName() {
        return this.poolName;
    }
    
    public boolean isNotifyPool() {
        return this.notifyPool;
    }
    
    public void setNotifyPool(final boolean notifyPool) {
        this.notifyPool = notifyPool;
    }
    
    public void resetStats() {
        final ConcurrentHashMap<String, QueryStats> queries = SlowQueryReportJmx.perPoolStats.get(this.poolName);
        if (queries != null) {
            final Iterator<String> it = queries.keySet().iterator();
            while (it.hasNext()) {
                it.remove();
            }
        }
    }
    
    @Override
    public CompositeData[] getSlowQueriesCD() throws OpenDataException {
        CompositeDataSupport[] result = null;
        final ConcurrentHashMap<String, QueryStats> queries = SlowQueryReportJmx.perPoolStats.get(this.poolName);
        if (queries != null) {
            final Set<Map.Entry<String, QueryStats>> stats = queries.entrySet();
            if (stats != null) {
                result = new CompositeDataSupport[stats.size()];
                final Iterator<Map.Entry<String, QueryStats>> it = stats.iterator();
                int pos = 0;
                while (it.hasNext()) {
                    final Map.Entry<String, QueryStats> entry = it.next();
                    final QueryStats qs = entry.getValue();
                    result[pos++] = qs.getCompositeData(getCompositeType());
                }
            }
        }
        return result;
    }
    
    protected void deregisterJmx() {
        try {
            if (SlowQueryReportJmx.mbeans.remove(this.poolName) != null) {
                final ObjectName oname = this.getObjectName(this.getClass(), this.poolName);
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(oname);
            }
        }
        catch (MBeanRegistrationException e) {
            SlowQueryReportJmx.log.debug((Object)"Jmx deregistration failed.", (Throwable)e);
        }
        catch (InstanceNotFoundException e2) {
            SlowQueryReportJmx.log.debug((Object)"Jmx deregistration failed.", (Throwable)e2);
        }
        catch (MalformedObjectNameException e3) {
            SlowQueryReportJmx.log.warn((Object)"Jmx deregistration failed.", (Throwable)e3);
        }
        catch (RuntimeOperationsException e4) {
            SlowQueryReportJmx.log.warn((Object)"Jmx deregistration failed.", (Throwable)e4);
        }
    }
    
    public ObjectName getObjectName(final Class<?> clazz, final String poolName) throws MalformedObjectNameException {
        final Map<String, PoolProperties.InterceptorProperty> properties = this.getProperties();
        ObjectName oname;
        if (properties != null && properties.containsKey("objectName")) {
            oname = new ObjectName(properties.get("objectName").getValue());
        }
        else {
            oname = new ObjectName("tomcat.jdbc:type=" + clazz.getName() + ",name=" + poolName);
        }
        return oname;
    }
    
    protected void registerJmx() {
        try {
            if (!this.isNotifyPool()) {
                if (getCompositeType() != null) {
                    final ObjectName oname = this.getObjectName(this.getClass(), this.poolName);
                    if (SlowQueryReportJmx.mbeans.putIfAbsent(this.poolName, this) == null) {
                        ManagementFactory.getPlatformMBeanServer().registerMBean(this, oname);
                    }
                }
                else {
                    SlowQueryReportJmx.log.warn((Object)(SlowQueryReport.class.getName() + "- No JMX support, composite type was not found."));
                }
            }
        }
        catch (MalformedObjectNameException e) {
            SlowQueryReportJmx.log.error((Object)"Jmx registration failed, no JMX data will be exposed for the query stats.", (Throwable)e);
        }
        catch (RuntimeOperationsException e2) {
            SlowQueryReportJmx.log.error((Object)"Jmx registration failed, no JMX data will be exposed for the query stats.", (Throwable)e2);
        }
        catch (MBeanException e3) {
            SlowQueryReportJmx.log.error((Object)"Jmx registration failed, no JMX data will be exposed for the query stats.", (Throwable)e3);
        }
        catch (InstanceAlreadyExistsException e4) {
            SlowQueryReportJmx.log.error((Object)"Jmx registration failed, no JMX data will be exposed for the query stats.", (Throwable)e4);
        }
        catch (NotCompliantMBeanException e5) {
            SlowQueryReportJmx.log.error((Object)"Jmx registration failed, no JMX data will be exposed for the query stats.", (Throwable)e5);
        }
    }
    
    @Override
    public void setProperties(final Map<String, PoolProperties.InterceptorProperty> properties) {
        super.setProperties(properties);
        final String threshold = "notifyPool";
        final PoolProperties.InterceptorProperty p1 = properties.get("notifyPool");
        if (p1 != null) {
            this.setNotifyPool(Boolean.parseBoolean(p1.getValue()));
        }
    }
    
    static {
        log = LogFactory.getLog((Class)SlowQueryReportJmx.class);
        mbeans = new ConcurrentHashMap<String, SlowQueryReportJmxMBean>();
        notifySequence = new AtomicLong(0L);
    }
}

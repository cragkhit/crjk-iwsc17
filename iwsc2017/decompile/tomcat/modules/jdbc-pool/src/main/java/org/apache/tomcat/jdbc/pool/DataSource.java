// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import org.apache.juli.logging.LogFactory;
import javax.management.InstanceNotFoundException;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.juli.logging.Log;
import javax.sql.ConnectionPoolDataSource;
import org.apache.tomcat.jdbc.pool.jmx.ConnectionPoolMBean;
import javax.management.MBeanRegistration;
import javax.sql.DataSource;

public class DataSource extends DataSourceProxy implements javax.sql.DataSource, MBeanRegistration, ConnectionPoolMBean, ConnectionPoolDataSource
{
    private static final Log log;
    protected volatile ObjectName oname;
    
    public DataSource() {
        this.oname = null;
    }
    
    public DataSource(final PoolConfiguration poolProperties) {
        super(poolProperties);
        this.oname = null;
    }
    
    @Override
    public void postDeregister() {
        if (this.oname != null) {
            this.unregisterJmx();
        }
    }
    
    @Override
    public void postRegister(final Boolean registrationDone) {
    }
    
    @Override
    public void preDeregister() throws Exception {
    }
    
    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        try {
            if (this.isJmxEnabled()) {
                this.oname = this.createObjectName(name);
                if (this.oname != null) {
                    this.registerJmx();
                }
            }
        }
        catch (MalformedObjectNameException x) {
            DataSource.log.error((Object)"Unable to create object name for JDBC pool.", (Throwable)x);
        }
        return name;
    }
    
    public ObjectName createObjectName(final ObjectName original) throws MalformedObjectNameException {
        final String domain = "tomcat.jdbc";
        final Hashtable<String, String> properties = original.getKeyPropertyList();
        final String origDomain = original.getDomain();
        properties.put("type", "ConnectionPool");
        properties.put("class", this.getClass().getName());
        if (original.getKeyProperty("path") != null || properties.get("context") != null) {
            properties.put("engine", origDomain);
        }
        final ObjectName name = new ObjectName(domain, properties);
        return name;
    }
    
    protected void registerJmx() {
        try {
            if (this.pool.getJmxPool() != null) {
                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.registerMBean(this.pool.getJmxPool(), this.oname);
            }
        }
        catch (Exception e) {
            DataSource.log.error((Object)"Unable to register JDBC pool with JMX", (Throwable)e);
        }
    }
    
    protected void unregisterJmx() {
        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.unregisterMBean(this.oname);
        }
        catch (InstanceNotFoundException ex) {}
        catch (Exception e) {
            DataSource.log.error((Object)"Unable to unregister JDBC pool with JMX", (Throwable)e);
        }
    }
    
    static {
        log = LogFactory.getLog((Class)DataSource.class);
    }
}

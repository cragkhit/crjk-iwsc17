package org.apache.tomcat.jdbc.pool;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class DataSource extends DataSourceProxy implements javax.sql.DataSource, MBeanRegistration, org.apache.tomcat.jdbc.pool.jmx.ConnectionPoolMBean, javax.sql.ConnectionPoolDataSource {
    private static final Log log = LogFactory.getLog ( DataSource.class );
    public DataSource() {
        super();
    }
    public DataSource ( PoolConfiguration poolProperties ) {
        super ( poolProperties );
    }
    protected volatile ObjectName oname = null;
    @Override
    public void postDeregister() {
        if ( oname != null ) {
            unregisterJmx();
        }
    }
    @Override
    public void postRegister ( Boolean registrationDone ) {
    }
    @Override
    public void preDeregister() throws Exception {
    }
    @Override
    public ObjectName preRegister ( MBeanServer server, ObjectName name ) throws Exception {
        try {
            if ( isJmxEnabled() ) {
                this.oname = createObjectName ( name );
                if ( oname != null ) {
                    registerJmx();
                }
            }
        } catch ( MalformedObjectNameException x ) {
            log.error ( "Unable to create object name for JDBC pool.", x );
        }
        return name;
    }
    public ObjectName createObjectName ( ObjectName original ) throws MalformedObjectNameException {
        String domain = ConnectionPool.POOL_JMX_DOMAIN;
        Hashtable<String, String> properties = original.getKeyPropertyList();
        String origDomain = original.getDomain();
        properties.put ( "type", "ConnectionPool" );
        properties.put ( "class", this.getClass().getName() );
        if ( original.getKeyProperty ( "path" ) != null || properties.get ( "context" ) != null ) {
            properties.put ( "engine", origDomain );
        }
        ObjectName name = new ObjectName ( domain, properties );
        return name;
    }
    protected void registerJmx() {
        try {
            if ( pool.getJmxPool() != null ) {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.registerMBean ( pool.getJmxPool(), oname );
            }
        } catch ( Exception e ) {
            log.error ( "Unable to register JDBC pool with JMX", e );
        }
    }
    protected void unregisterJmx() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.unregisterMBean ( oname );
        } catch ( InstanceNotFoundException ignore ) {
        } catch ( Exception e ) {
            log.error ( "Unable to unregister JDBC pool with JMX", e );
        }
    }
}

package org.apache.catalina.util;
import org.apache.juli.logging.LogFactory;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.JmxEnabled;
public abstract class LifecycleMBeanBase extends LifecycleBase implements JmxEnabled {
    private static final Log log;
    private static final StringManager sm;
    private String domain;
    private ObjectName oname;
    protected MBeanServer mserver;
    public LifecycleMBeanBase() {
        this.domain = null;
        this.oname = null;
        this.mserver = null;
    }
    @Override
    protected void initInternal() throws LifecycleException {
        if ( this.oname == null ) {
            this.mserver = Registry.getRegistry ( null, null ).getMBeanServer();
            this.oname = this.register ( this, this.getObjectNameKeyProperties() );
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        this.unregister ( this.oname );
    }
    @Override
    public final void setDomain ( final String domain ) {
        this.domain = domain;
    }
    @Override
    public final String getDomain() {
        if ( this.domain == null ) {
            this.domain = this.getDomainInternal();
        }
        if ( this.domain == null ) {
            this.domain = "Catalina";
        }
        return this.domain;
    }
    protected abstract String getDomainInternal();
    @Override
    public final ObjectName getObjectName() {
        return this.oname;
    }
    protected abstract String getObjectNameKeyProperties();
    protected final ObjectName register ( final Object obj, final String objectNameKeyProperties ) {
        final StringBuilder name = new StringBuilder ( this.getDomain() );
        name.append ( ':' );
        name.append ( objectNameKeyProperties );
        ObjectName on = null;
        try {
            on = new ObjectName ( name.toString() );
            Registry.getRegistry ( null, null ).registerComponent ( obj, on, null );
        } catch ( MalformedObjectNameException e ) {
            LifecycleMBeanBase.log.warn ( LifecycleMBeanBase.sm.getString ( "lifecycleMBeanBase.registerFail", obj, name ), e );
        } catch ( Exception e2 ) {
            LifecycleMBeanBase.log.warn ( LifecycleMBeanBase.sm.getString ( "lifecycleMBeanBase.registerFail", obj, name ), e2 );
        }
        return on;
    }
    protected final void unregister ( final ObjectName on ) {
        if ( on == null ) {
            return;
        }
        if ( this.mserver == null ) {
            LifecycleMBeanBase.log.warn ( LifecycleMBeanBase.sm.getString ( "lifecycleMBeanBase.unregisterNoServer", on ) );
            return;
        }
        try {
            this.mserver.unregisterMBean ( on );
        } catch ( MBeanRegistrationException e ) {
            LifecycleMBeanBase.log.warn ( LifecycleMBeanBase.sm.getString ( "lifecycleMBeanBase.unregisterFail", on ), e );
        } catch ( InstanceNotFoundException e2 ) {
            LifecycleMBeanBase.log.warn ( LifecycleMBeanBase.sm.getString ( "lifecycleMBeanBase.unregisterFail", on ), e2 );
        }
    }
    @Override
    public final void postDeregister() {
    }
    @Override
    public final void postRegister ( final Boolean registrationDone ) {
    }
    @Override
    public final void preDeregister() throws Exception {
    }
    @Override
    public final ObjectName preRegister ( final MBeanServer server, final ObjectName name ) throws Exception {
        this.mserver = server;
        this.oname = name;
        this.domain = name.getDomain();
        return this.oname;
    }
    static {
        log = LogFactory.getLog ( LifecycleMBeanBase.class );
        sm = StringManager.getManager ( "org.apache.catalina.util" );
    }
}

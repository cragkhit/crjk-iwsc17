package org.apache.catalina.util;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.catalina.Globals;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public abstract class LifecycleMBeanBase extends LifecycleBase
    implements JmxEnabled {
    private static final Log log = LogFactory.getLog ( LifecycleMBeanBase.class );
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.catalina.util" );
    private String domain = null;
    private ObjectName oname = null;
    protected MBeanServer mserver = null;
    @Override
    protected void initInternal() throws LifecycleException {
        if ( oname == null ) {
            mserver = Registry.getRegistry ( null, null ).getMBeanServer();
            oname = register ( this, getObjectNameKeyProperties() );
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        unregister ( oname );
    }
    @Override
    public final void setDomain ( String domain ) {
        this.domain = domain;
    }
    @Override
    public final String getDomain() {
        if ( domain == null ) {
            domain = getDomainInternal();
        }
        if ( domain == null ) {
            domain = Globals.DEFAULT_MBEAN_DOMAIN;
        }
        return domain;
    }
    protected abstract String getDomainInternal();
    @Override
    public final ObjectName getObjectName() {
        return oname;
    }
    protected abstract String getObjectNameKeyProperties();
    protected final ObjectName register ( Object obj,
                                          String objectNameKeyProperties ) {
        StringBuilder name = new StringBuilder ( getDomain() );
        name.append ( ':' );
        name.append ( objectNameKeyProperties );
        ObjectName on = null;
        try {
            on = new ObjectName ( name.toString() );
            Registry.getRegistry ( null, null ).registerComponent ( obj, on, null );
        } catch ( MalformedObjectNameException e ) {
            log.warn ( sm.getString ( "lifecycleMBeanBase.registerFail", obj, name ),
                       e );
        } catch ( Exception e ) {
            log.warn ( sm.getString ( "lifecycleMBeanBase.registerFail", obj, name ),
                       e );
        }
        return on;
    }
    protected final void unregister ( ObjectName on ) {
        if ( on == null ) {
            return;
        }
        if ( mserver == null ) {
            log.warn ( sm.getString ( "lifecycleMBeanBase.unregisterNoServer", on ) );
            return;
        }
        try {
            mserver.unregisterMBean ( on );
        } catch ( MBeanRegistrationException e ) {
            log.warn ( sm.getString ( "lifecycleMBeanBase.unregisterFail", on ), e );
        } catch ( InstanceNotFoundException e ) {
            log.warn ( sm.getString ( "lifecycleMBeanBase.unregisterFail", on ), e );
        }
    }
    @Override
    public final void postDeregister() {
    }
    @Override
    public final void postRegister ( Boolean registrationDone ) {
    }
    @Override
    public final void preDeregister() throws Exception {
    }
    @Override
    public final ObjectName preRegister ( MBeanServer server, ObjectName name )
    throws Exception {
        this.mserver = server;
        this.oname = name;
        this.domain = name.getDomain();
        return oname;
    }
}

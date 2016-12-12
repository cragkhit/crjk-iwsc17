package org.apache.catalina.mbeans;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import org.apache.catalina.User;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;
import org.apache.catalina.UserDatabase;
import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.catalina.Lifecycle;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public class GlobalResourcesLifecycleListener implements LifecycleListener {
    private static final Log log;
    protected Lifecycle component;
    protected static final Registry registry;
    public GlobalResourcesLifecycleListener() {
        this.component = null;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "start".equals ( event.getType() ) ) {
            this.component = event.getLifecycle();
            this.createMBeans();
        } else if ( "stop".equals ( event.getType() ) ) {
            this.destroyMBeans();
            this.component = null;
        }
    }
    protected void createMBeans() {
        Context context = null;
        try {
            context = ( Context ) new InitialContext().lookup ( "java:/" );
        } catch ( NamingException e ) {
            GlobalResourcesLifecycleListener.log.error ( "No global naming context defined for server" );
            return;
        }
        try {
            this.createMBeans ( "", context );
        } catch ( NamingException e ) {
            GlobalResourcesLifecycleListener.log.error ( "Exception processing Global JNDI Resources", e );
        }
    }
    protected void createMBeans ( final String prefix, final Context context ) throws NamingException {
        if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
            GlobalResourcesLifecycleListener.log.debug ( "Creating MBeans for Global JNDI Resources in Context '" + prefix + "'" );
        }
        try {
            final NamingEnumeration<Binding> bindings = context.listBindings ( "" );
            while ( bindings.hasMore() ) {
                final Binding binding = bindings.next();
                final String name = prefix + binding.getName();
                final Object value = context.lookup ( binding.getName() );
                if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
                    GlobalResourcesLifecycleListener.log.debug ( "Checking resource " + name );
                }
                if ( value instanceof Context ) {
                    this.createMBeans ( name + "/", ( Context ) value );
                } else {
                    if ( ! ( value instanceof UserDatabase ) ) {
                        continue;
                    }
                    try {
                        this.createMBeans ( name, ( UserDatabase ) value );
                    } catch ( Exception e ) {
                        GlobalResourcesLifecycleListener.log.error ( "Exception creating UserDatabase MBeans for " + name, e );
                    }
                }
            }
        } catch ( RuntimeException ex ) {
            GlobalResourcesLifecycleListener.log.error ( "RuntimeException " + ex );
        } catch ( OperationNotSupportedException ex2 ) {
            GlobalResourcesLifecycleListener.log.error ( "Operation not supported " + ex2 );
        }
    }
    protected void createMBeans ( final String name, final UserDatabase database ) throws Exception {
        if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
            GlobalResourcesLifecycleListener.log.debug ( "Creating UserDatabase MBeans for resource " + name );
            GlobalResourcesLifecycleListener.log.debug ( "Database=" + database );
        }
        try {
            MBeanUtils.createMBean ( database );
        } catch ( Exception e ) {
            throw new IllegalArgumentException ( "Cannot create UserDatabase MBean for resource " + name, e );
        }
        final Iterator<Role> roles = database.getRoles();
        while ( roles.hasNext() ) {
            final Role role = roles.next();
            if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
                GlobalResourcesLifecycleListener.log.debug ( "  Creating Role MBean for role " + role );
            }
            try {
                MBeanUtils.createMBean ( role );
            } catch ( Exception e2 ) {
                throw new IllegalArgumentException ( "Cannot create Role MBean for role " + role, e2 );
            }
        }
        final Iterator<Group> groups = database.getGroups();
        while ( groups.hasNext() ) {
            final Group group = groups.next();
            if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
                GlobalResourcesLifecycleListener.log.debug ( "  Creating Group MBean for group " + group );
            }
            try {
                MBeanUtils.createMBean ( group );
            } catch ( Exception e3 ) {
                throw new IllegalArgumentException ( "Cannot create Group MBean for group " + group, e3 );
            }
        }
        final Iterator<User> users = database.getUsers();
        while ( users.hasNext() ) {
            final User user = users.next();
            if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
                GlobalResourcesLifecycleListener.log.debug ( "  Creating User MBean for user " + user );
            }
            try {
                MBeanUtils.createMBean ( user );
            } catch ( Exception e4 ) {
                throw new IllegalArgumentException ( "Cannot create User MBean for user " + user, e4 );
            }
        }
    }
    protected void destroyMBeans() {
        if ( GlobalResourcesLifecycleListener.log.isDebugEnabled() ) {
            GlobalResourcesLifecycleListener.log.debug ( "Destroying MBeans for Global JNDI Resources" );
        }
    }
    static {
        log = LogFactory.getLog ( GlobalResourcesLifecycleListener.class );
        registry = MBeanUtils.createRegistry();
    }
}

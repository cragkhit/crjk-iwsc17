package org.apache.catalina.mbeans;
import java.util.Iterator;
import java.util.Set;
import javax.management.QueryExp;
import org.apache.catalina.Loader;
import javax.management.MalformedObjectNameException;
import org.apache.catalina.Container;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.User;
import org.apache.catalina.Role;
import org.apache.catalina.Group;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import javax.management.ObjectName;
import org.apache.tomcat.util.modeler.ManagedBean;
import javax.management.MBeanException;
import javax.management.DynamicMBean;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import javax.management.MBeanServer;
import org.apache.tomcat.util.modeler.Registry;
public class MBeanUtils {
    private static final String[][] exceptions;
    private static Registry registry;
    private static MBeanServer mserver;
    static String createManagedName ( final Object component ) {
        String className = component.getClass().getName();
        for ( int i = 0; i < MBeanUtils.exceptions.length; ++i ) {
            if ( className.equals ( MBeanUtils.exceptions[i][0] ) ) {
                return MBeanUtils.exceptions[i][1];
            }
        }
        final int period = className.lastIndexOf ( 46 );
        if ( period >= 0 ) {
            className = className.substring ( period + 1 );
        }
        return className;
    }
    public static DynamicMBean createMBean ( final ContextEnvironment environment ) throws Exception {
        final String mname = createManagedName ( environment );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( environment );
        final ObjectName oname = createObjectName ( domain, environment );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    public static DynamicMBean createMBean ( final ContextResource resource ) throws Exception {
        final String mname = createManagedName ( resource );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( resource );
        final ObjectName oname = createObjectName ( domain, resource );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    public static DynamicMBean createMBean ( final ContextResourceLink resourceLink ) throws Exception {
        final String mname = createManagedName ( resourceLink );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( resourceLink );
        final ObjectName oname = createObjectName ( domain, resourceLink );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    static DynamicMBean createMBean ( final Group group ) throws Exception {
        final String mname = createManagedName ( group );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( group );
        final ObjectName oname = createObjectName ( domain, group );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    static DynamicMBean createMBean ( final Role role ) throws Exception {
        final String mname = createManagedName ( role );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( role );
        final ObjectName oname = createObjectName ( domain, role );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    static DynamicMBean createMBean ( final User user ) throws Exception {
        final String mname = createManagedName ( user );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( user );
        final ObjectName oname = createObjectName ( domain, user );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    static DynamicMBean createMBean ( final UserDatabase userDatabase ) throws Exception {
        final String mname = createManagedName ( userDatabase );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            final Exception e = new Exception ( "ManagedBean is not found with " + mname );
            throw new MBeanException ( e );
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final DynamicMBean mbean = managed.createMBean ( userDatabase );
        final ObjectName oname = createObjectName ( domain, userDatabase );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
        MBeanUtils.mserver.registerMBean ( mbean, oname );
        return mbean;
    }
    public static ObjectName createObjectName ( final String domain, final ContextEnvironment environment ) throws MalformedObjectNameException {
        ObjectName name = null;
        final Object container = environment.getNamingResources().getContainer();
        if ( container instanceof Server ) {
            name = new ObjectName ( domain + ":type=Environment,resourcetype=Global,name=" + environment.getName() );
        } else if ( container instanceof Context ) {
            final Context context = ( Context ) container;
            final ContextName cn = new ContextName ( context.getName(), false );
            final Container host = context.getParent();
            name = new ObjectName ( domain + ":type=Environment,resourcetype=Context,host=" + host.getName() + ",context=" + cn.getDisplayName() + ",name=" + environment.getName() );
        }
        return name;
    }
    public static ObjectName createObjectName ( final String domain, final ContextResource resource ) throws MalformedObjectNameException {
        ObjectName name = null;
        final String quotedResourceName = ObjectName.quote ( resource.getName() );
        final Object container = resource.getNamingResources().getContainer();
        if ( container instanceof Server ) {
            name = new ObjectName ( domain + ":type=Resource,resourcetype=Global,class=" + resource.getType() + ",name=" + quotedResourceName );
        } else if ( container instanceof Context ) {
            final Context context = ( Context ) container;
            final ContextName cn = new ContextName ( context.getName(), false );
            final Container host = context.getParent();
            name = new ObjectName ( domain + ":type=Resource,resourcetype=Context,host=" + host.getName() + ",context=" + cn.getDisplayName() + ",class=" + resource.getType() + ",name=" + quotedResourceName );
        }
        return name;
    }
    public static ObjectName createObjectName ( final String domain, final ContextResourceLink resourceLink ) throws MalformedObjectNameException {
        ObjectName name = null;
        final String quotedResourceLinkName = ObjectName.quote ( resourceLink.getName() );
        final Object container = resourceLink.getNamingResources().getContainer();
        if ( container instanceof Server ) {
            name = new ObjectName ( domain + ":type=ResourceLink,resourcetype=Global,name=" + quotedResourceLinkName );
        } else if ( container instanceof Context ) {
            final Context context = ( Context ) container;
            final ContextName cn = new ContextName ( context.getName(), false );
            final Container host = context.getParent();
            name = new ObjectName ( domain + ":type=ResourceLink,resourcetype=Context,host=" + host.getName() + ",context=" + cn.getDisplayName() + ",name=" + quotedResourceLinkName );
        }
        return name;
    }
    static ObjectName createObjectName ( final String domain, final Group group ) throws MalformedObjectNameException {
        ObjectName name = null;
        name = new ObjectName ( domain + ":type=Group,groupname=" + ObjectName.quote ( group.getGroupname() ) + ",database=" + group.getUserDatabase().getId() );
        return name;
    }
    static ObjectName createObjectName ( final String domain, final Loader loader ) throws MalformedObjectNameException {
        ObjectName name = null;
        final Context context = loader.getContext();
        final ContextName cn = new ContextName ( context.getName(), false );
        final Container host = context.getParent();
        name = new ObjectName ( domain + ":type=Loader,host=" + host.getName() + ",context=" + cn.getDisplayName() );
        return name;
    }
    static ObjectName createObjectName ( final String domain, final Role role ) throws MalformedObjectNameException {
        final ObjectName name = new ObjectName ( domain + ":type=Role,rolename=" + ObjectName.quote ( role.getRolename() ) + ",database=" + role.getUserDatabase().getId() );
        return name;
    }
    static ObjectName createObjectName ( final String domain, final User user ) throws MalformedObjectNameException {
        final ObjectName name = new ObjectName ( domain + ":type=User,username=" + ObjectName.quote ( user.getUsername() ) + ",database=" + user.getUserDatabase().getId() );
        return name;
    }
    static ObjectName createObjectName ( final String domain, final UserDatabase userDatabase ) throws MalformedObjectNameException {
        ObjectName name = null;
        name = new ObjectName ( domain + ":type=UserDatabase,database=" + userDatabase.getId() );
        return name;
    }
    public static synchronized Registry createRegistry() {
        if ( MBeanUtils.registry == null ) {
            MBeanUtils.registry = Registry.getRegistry ( null, null );
            final ClassLoader cl = MBeanUtils.class.getClassLoader();
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.mbeans", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.authenticator", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.core", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.deploy", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.loader", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.realm", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.session", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.startup", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.users", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.ha", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.connector", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.valves", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.catalina.storeconfig", cl );
            MBeanUtils.registry.loadDescriptors ( "org.apache.tomcat.util.descriptor.web", cl );
        }
        return MBeanUtils.registry;
    }
    public static synchronized MBeanServer createServer() {
        if ( MBeanUtils.mserver == null ) {
            MBeanUtils.mserver = Registry.getRegistry ( null, null ).getMBeanServer();
        }
        return MBeanUtils.mserver;
    }
    public static void destroyMBean ( final ContextEnvironment environment ) throws Exception {
        final String mname = createManagedName ( environment );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            return;
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final ObjectName oname = createObjectName ( domain, environment );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
    }
    public static void destroyMBean ( final ContextResource resource ) throws Exception {
        if ( "org.apache.catalina.UserDatabase".equals ( resource.getType() ) ) {
            destroyMBeanUserDatabase ( resource.getName() );
        }
        final String mname = createManagedName ( resource );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            return;
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final ObjectName oname = createObjectName ( domain, resource );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
    }
    public static void destroyMBean ( final ContextResourceLink resourceLink ) throws Exception {
        final String mname = createManagedName ( resourceLink );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            return;
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final ObjectName oname = createObjectName ( domain, resourceLink );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
    }
    static void destroyMBean ( final Group group ) throws Exception {
        final String mname = createManagedName ( group );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            return;
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final ObjectName oname = createObjectName ( domain, group );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
    }
    static void destroyMBean ( final Role role ) throws Exception {
        final String mname = createManagedName ( role );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            return;
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final ObjectName oname = createObjectName ( domain, role );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
    }
    static void destroyMBean ( final User user ) throws Exception {
        final String mname = createManagedName ( user );
        final ManagedBean managed = MBeanUtils.registry.findManagedBean ( mname );
        if ( managed == null ) {
            return;
        }
        String domain = managed.getDomain();
        if ( domain == null ) {
            domain = MBeanUtils.mserver.getDefaultDomain();
        }
        final ObjectName oname = createObjectName ( domain, user );
        if ( MBeanUtils.mserver.isRegistered ( oname ) ) {
            MBeanUtils.mserver.unregisterMBean ( oname );
        }
    }
    static void destroyMBeanUserDatabase ( final String userDatabase ) throws Exception {
        ObjectName query = null;
        Set<ObjectName> results = null;
        query = new ObjectName ( "Users:type=Group,database=" + userDatabase + ",*" );
        results = MBeanUtils.mserver.queryNames ( query, null );
        for ( final ObjectName result : results ) {
            MBeanUtils.mserver.unregisterMBean ( result );
        }
        query = new ObjectName ( "Users:type=Role,database=" + userDatabase + ",*" );
        results = MBeanUtils.mserver.queryNames ( query, null );
        for ( final ObjectName result : results ) {
            MBeanUtils.mserver.unregisterMBean ( result );
        }
        query = new ObjectName ( "Users:type=User,database=" + userDatabase + ",*" );
        results = MBeanUtils.mserver.queryNames ( query, null );
        for ( final ObjectName result : results ) {
            MBeanUtils.mserver.unregisterMBean ( result );
        }
        final ObjectName db = new ObjectName ( "Users:type=UserDatabase,database=" + userDatabase );
        if ( MBeanUtils.mserver.isRegistered ( db ) ) {
            MBeanUtils.mserver.unregisterMBean ( db );
        }
    }
    static {
        exceptions = new String[][] { { "org.apache.catalina.users.MemoryGroup", "Group" }, { "org.apache.catalina.users.MemoryRole", "Role" }, { "org.apache.catalina.users.MemoryUser", "User" } };
        MBeanUtils.registry = createRegistry();
        MBeanUtils.mserver = createServer();
    }
}

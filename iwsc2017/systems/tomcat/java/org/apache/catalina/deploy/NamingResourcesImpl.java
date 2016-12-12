package org.apache.catalina.deploy;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.naming.NamingException;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.catalina.util.Introspection;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.naming.ContextBindings;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.apache.tomcat.util.descriptor.web.ContextTransaction;
import org.apache.tomcat.util.descriptor.web.InjectionTarget;
import org.apache.tomcat.util.descriptor.web.MessageDestinationRef;
import org.apache.tomcat.util.descriptor.web.NamingResources;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
import org.apache.tomcat.util.res.StringManager;
public class NamingResourcesImpl extends LifecycleMBeanBase
    implements Serializable, NamingResources {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog ( NamingResourcesImpl.class );
    private static final StringManager sm = StringManager.getManager ( NamingResourcesImpl.class );
    private volatile boolean resourceRequireExplicitRegistration = false;
    public NamingResourcesImpl() {
    }
    private Object container = null;
    private final Set<String> entries = new HashSet<>();
    private final HashMap<String, ContextEjb> ejbs = new HashMap<>();
    private final HashMap<String, ContextEnvironment> envs = new HashMap<>();
    private final HashMap<String, ContextLocalEjb> localEjbs = new HashMap<>();
    private final HashMap<String, MessageDestinationRef> mdrs = new HashMap<>();
    private final HashMap<String, ContextResourceEnvRef> resourceEnvRefs =
        new HashMap<>();
    private final HashMap<String, ContextResource> resources =
        new HashMap<>();
    private final HashMap<String, ContextResourceLink> resourceLinks =
        new HashMap<>();
    private final HashMap<String, ContextService> services =
        new HashMap<>();
    private ContextTransaction transaction = null;
    protected final PropertyChangeSupport support =
        new PropertyChangeSupport ( this );
    @Override
    public Object getContainer() {
        return container;
    }
    public void setContainer ( Object container ) {
        this.container = container;
    }
    public void setTransaction ( ContextTransaction transaction ) {
        this.transaction = transaction;
    }
    public ContextTransaction getTransaction() {
        return transaction;
    }
    public void addEjb ( ContextEjb ejb ) {
        if ( entries.contains ( ejb.getName() ) ) {
            return;
        } else {
            entries.add ( ejb.getName() );
        }
        synchronized ( ejbs ) {
            ejb.setNamingResources ( this );
            ejbs.put ( ejb.getName(), ejb );
        }
        support.firePropertyChange ( "ejb", null, ejb );
    }
    @Override
    public void addEnvironment ( ContextEnvironment environment ) {
        if ( entries.contains ( environment.getName() ) ) {
            ContextEnvironment ce = findEnvironment ( environment.getName() );
            ContextResourceLink rl = findResourceLink ( environment.getName() );
            if ( ce != null ) {
                if ( ce.getOverride() ) {
                    removeEnvironment ( environment.getName() );
                } else {
                    return;
                }
            } else if ( rl != null ) {
                NamingResourcesImpl global = getServer().getGlobalNamingResources();
                if ( global.findEnvironment ( rl.getGlobal() ) != null ) {
                    if ( global.findEnvironment ( rl.getGlobal() ).getOverride() ) {
                        removeResourceLink ( environment.getName() );
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }
        }
        if ( !checkResourceType ( environment ) ) {
            throw new IllegalArgumentException ( sm.getString (
                    "namingResources.resourceTypeFail", environment.getName(),
                    environment.getType() ) );
        }
        entries.add ( environment.getName() );
        synchronized ( envs ) {
            environment.setNamingResources ( this );
            envs.put ( environment.getName(), environment );
        }
        support.firePropertyChange ( "environment", null, environment );
        if ( resourceRequireExplicitRegistration ) {
            try {
                MBeanUtils.createMBean ( environment );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "namingResources.mbeanCreateFail",
                                          environment.getName() ), e );
            }
        }
    }
    private Server getServer() {
        if ( container instanceof Server ) {
            return ( Server ) container;
        }
        if ( container instanceof Context ) {
            Engine engine =
                ( Engine ) ( ( Context ) container ).getParent().getParent();
            return engine.getService().getServer();
        }
        return null;
    }
    public void addLocalEjb ( ContextLocalEjb ejb ) {
        if ( entries.contains ( ejb.getName() ) ) {
            return;
        } else {
            entries.add ( ejb.getName() );
        }
        synchronized ( localEjbs ) {
            ejb.setNamingResources ( this );
            localEjbs.put ( ejb.getName(), ejb );
        }
        support.firePropertyChange ( "localEjb", null, ejb );
    }
    public void addMessageDestinationRef ( MessageDestinationRef mdr ) {
        if ( entries.contains ( mdr.getName() ) ) {
            return;
        } else {
            if ( !checkResourceType ( mdr ) ) {
                throw new IllegalArgumentException ( sm.getString (
                        "namingResources.resourceTypeFail", mdr.getName(),
                        mdr.getType() ) );
            }
            entries.add ( mdr.getName() );
        }
        synchronized ( mdrs ) {
            mdr.setNamingResources ( this );
            mdrs.put ( mdr.getName(), mdr );
        }
        support.firePropertyChange ( "messageDestinationRef", null, mdr );
    }
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    @Override
    public void addResource ( ContextResource resource ) {
        if ( entries.contains ( resource.getName() ) ) {
            return;
        } else {
            if ( !checkResourceType ( resource ) ) {
                throw new IllegalArgumentException ( sm.getString (
                        "namingResources.resourceTypeFail", resource.getName(),
                        resource.getType() ) );
            }
            entries.add ( resource.getName() );
        }
        synchronized ( resources ) {
            resource.setNamingResources ( this );
            resources.put ( resource.getName(), resource );
        }
        support.firePropertyChange ( "resource", null, resource );
        if ( resourceRequireExplicitRegistration ) {
            try {
                MBeanUtils.createMBean ( resource );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "namingResources.mbeanCreateFail",
                                          resource.getName() ), e );
            }
        }
    }
    public void addResourceEnvRef ( ContextResourceEnvRef resource ) {
        if ( entries.contains ( resource.getName() ) ) {
            return;
        } else {
            if ( !checkResourceType ( resource ) ) {
                throw new IllegalArgumentException ( sm.getString (
                        "namingResources.resourceTypeFail", resource.getName(),
                        resource.getType() ) );
            }
            entries.add ( resource.getName() );
        }
        synchronized ( resourceEnvRefs ) {
            resource.setNamingResources ( this );
            resourceEnvRefs.put ( resource.getName(), resource );
        }
        support.firePropertyChange ( "resourceEnvRef", null, resource );
    }
    @Override
    public void addResourceLink ( ContextResourceLink resourceLink ) {
        if ( entries.contains ( resourceLink.getName() ) ) {
            return;
        } else {
            entries.add ( resourceLink.getName() );
        }
        synchronized ( resourceLinks ) {
            resourceLink.setNamingResources ( this );
            resourceLinks.put ( resourceLink.getName(), resourceLink );
        }
        support.firePropertyChange ( "resourceLink", null, resourceLink );
        if ( resourceRequireExplicitRegistration ) {
            try {
                MBeanUtils.createMBean ( resourceLink );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "namingResources.mbeanCreateFail",
                                          resourceLink.getName() ), e );
            }
        }
    }
    public void addService ( ContextService service ) {
        if ( entries.contains ( service.getName() ) ) {
            return;
        } else {
            entries.add ( service.getName() );
        }
        synchronized ( services ) {
            service.setNamingResources ( this );
            services.put ( service.getName(), service );
        }
        support.firePropertyChange ( "service", null, service );
    }
    public ContextEjb findEjb ( String name ) {
        synchronized ( ejbs ) {
            return ejbs.get ( name );
        }
    }
    public ContextEjb[] findEjbs() {
        synchronized ( ejbs ) {
            ContextEjb results[] = new ContextEjb[ejbs.size()];
            return ejbs.values().toArray ( results );
        }
    }
    public ContextEnvironment findEnvironment ( String name ) {
        synchronized ( envs ) {
            return envs.get ( name );
        }
    }
    public ContextEnvironment[] findEnvironments() {
        synchronized ( envs ) {
            ContextEnvironment results[] = new ContextEnvironment[envs.size()];
            return envs.values().toArray ( results );
        }
    }
    public ContextLocalEjb findLocalEjb ( String name ) {
        synchronized ( localEjbs ) {
            return localEjbs.get ( name );
        }
    }
    public ContextLocalEjb[] findLocalEjbs() {
        synchronized ( localEjbs ) {
            ContextLocalEjb results[] = new ContextLocalEjb[localEjbs.size()];
            return localEjbs.values().toArray ( results );
        }
    }
    public MessageDestinationRef findMessageDestinationRef ( String name ) {
        synchronized ( mdrs ) {
            return mdrs.get ( name );
        }
    }
    public MessageDestinationRef[] findMessageDestinationRefs() {
        synchronized ( mdrs ) {
            MessageDestinationRef results[] =
                new MessageDestinationRef[mdrs.size()];
            return mdrs.values().toArray ( results );
        }
    }
    public ContextResource findResource ( String name ) {
        synchronized ( resources ) {
            return resources.get ( name );
        }
    }
    public ContextResourceLink findResourceLink ( String name ) {
        synchronized ( resourceLinks ) {
            return resourceLinks.get ( name );
        }
    }
    public ContextResourceLink[] findResourceLinks() {
        synchronized ( resourceLinks ) {
            ContextResourceLink results[] =
                new ContextResourceLink[resourceLinks.size()];
            return resourceLinks.values().toArray ( results );
        }
    }
    public ContextResource[] findResources() {
        synchronized ( resources ) {
            ContextResource results[] = new ContextResource[resources.size()];
            return resources.values().toArray ( results );
        }
    }
    public ContextResourceEnvRef findResourceEnvRef ( String name ) {
        synchronized ( resourceEnvRefs ) {
            return resourceEnvRefs.get ( name );
        }
    }
    public ContextResourceEnvRef[] findResourceEnvRefs() {
        synchronized ( resourceEnvRefs ) {
            ContextResourceEnvRef results[] = new ContextResourceEnvRef[resourceEnvRefs.size()];
            return resourceEnvRefs.values().toArray ( results );
        }
    }
    public ContextService findService ( String name ) {
        synchronized ( services ) {
            return services.get ( name );
        }
    }
    public ContextService[] findServices() {
        synchronized ( services ) {
            ContextService results[] = new ContextService[services.size()];
            return services.values().toArray ( results );
        }
    }
    public void removeEjb ( String name ) {
        entries.remove ( name );
        ContextEjb ejb = null;
        synchronized ( ejbs ) {
            ejb = ejbs.remove ( name );
        }
        if ( ejb != null ) {
            support.firePropertyChange ( "ejb", ejb, null );
            ejb.setNamingResources ( null );
        }
    }
    @Override
    public void removeEnvironment ( String name ) {
        entries.remove ( name );
        ContextEnvironment environment = null;
        synchronized ( envs ) {
            environment = envs.remove ( name );
        }
        if ( environment != null ) {
            support.firePropertyChange ( "environment", environment, null );
            if ( resourceRequireExplicitRegistration ) {
                try {
                    MBeanUtils.destroyMBean ( environment );
                } catch ( Exception e ) {
                    log.warn ( sm.getString ( "namingResources.mbeanDestroyFail",
                                              environment.getName() ), e );
                }
            }
            environment.setNamingResources ( null );
        }
    }
    public void removeLocalEjb ( String name ) {
        entries.remove ( name );
        ContextLocalEjb localEjb = null;
        synchronized ( localEjbs ) {
            localEjb = localEjbs.remove ( name );
        }
        if ( localEjb != null ) {
            support.firePropertyChange ( "localEjb", localEjb, null );
            localEjb.setNamingResources ( null );
        }
    }
    public void removeMessageDestinationRef ( String name ) {
        entries.remove ( name );
        MessageDestinationRef mdr = null;
        synchronized ( mdrs ) {
            mdr = mdrs.remove ( name );
        }
        if ( mdr != null ) {
            support.firePropertyChange ( "messageDestinationRef",
                                         mdr, null );
            mdr.setNamingResources ( null );
        }
    }
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    @Override
    public void removeResource ( String name ) {
        entries.remove ( name );
        ContextResource resource = null;
        synchronized ( resources ) {
            resource = resources.remove ( name );
        }
        if ( resource != null ) {
            support.firePropertyChange ( "resource", resource, null );
            if ( resourceRequireExplicitRegistration ) {
                try {
                    MBeanUtils.destroyMBean ( resource );
                } catch ( Exception e ) {
                    log.warn ( sm.getString ( "namingResources.mbeanDestroyFail",
                                              resource.getName() ), e );
                }
            }
            resource.setNamingResources ( null );
        }
    }
    public void removeResourceEnvRef ( String name ) {
        entries.remove ( name );
        ContextResourceEnvRef resourceEnvRef = null;
        synchronized ( resourceEnvRefs ) {
            resourceEnvRef =
                resourceEnvRefs.remove ( name );
        }
        if ( resourceEnvRef != null ) {
            support.firePropertyChange ( "resourceEnvRef", resourceEnvRef, null );
            resourceEnvRef.setNamingResources ( null );
        }
    }
    @Override
    public void removeResourceLink ( String name ) {
        entries.remove ( name );
        ContextResourceLink resourceLink = null;
        synchronized ( resourceLinks ) {
            resourceLink = resourceLinks.remove ( name );
        }
        if ( resourceLink != null ) {
            support.firePropertyChange ( "resourceLink", resourceLink, null );
            if ( resourceRequireExplicitRegistration ) {
                try {
                    MBeanUtils.destroyMBean ( resourceLink );
                } catch ( Exception e ) {
                    log.warn ( sm.getString ( "namingResources.mbeanDestroyFail",
                                              resourceLink.getName() ), e );
                }
            }
            resourceLink.setNamingResources ( null );
        }
    }
    public void removeService ( String name ) {
        entries.remove ( name );
        ContextService service = null;
        synchronized ( services ) {
            service = services.remove ( name );
        }
        if ( service != null ) {
            support.firePropertyChange ( "service", service, null );
            service.setNamingResources ( null );
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        resourceRequireExplicitRegistration = true;
        for ( ContextResource cr : resources.values() ) {
            try {
                MBeanUtils.createMBean ( cr );
            } catch ( Exception e ) {
                log.warn ( sm.getString (
                               "namingResources.mbeanCreateFail", cr.getName() ), e );
            }
        }
        for ( ContextEnvironment ce : envs.values() ) {
            try {
                MBeanUtils.createMBean ( ce );
            } catch ( Exception e ) {
                log.warn ( sm.getString (
                               "namingResources.mbeanCreateFail", ce.getName() ), e );
            }
        }
        for ( ContextResourceLink crl : resourceLinks.values() ) {
            try {
                MBeanUtils.createMBean ( crl );
            } catch ( Exception e ) {
                log.warn ( sm.getString (
                               "namingResources.mbeanCreateFail", crl.getName() ), e );
            }
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        fireLifecycleEvent ( CONFIGURE_START_EVENT, null );
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        cleanUp();
        setState ( LifecycleState.STOPPING );
        fireLifecycleEvent ( CONFIGURE_STOP_EVENT, null );
    }
    private void cleanUp() {
        if ( resources.size() == 0 ) {
            return;
        }
        javax.naming.Context ctxt;
        try {
            if ( container instanceof Server ) {
                ctxt = ( ( Server ) container ).getGlobalNamingContext();
            } else {
                ctxt = ContextBindings.getClassLoader();
                ctxt = ( javax.naming.Context ) ctxt.lookup ( "comp/env" );
            }
        } catch ( NamingException e ) {
            log.warn ( sm.getString ( "namingResources.cleanupNoContext",
                                      container ), e );
            return;
        }
        for ( ContextResource cr : resources.values() ) {
            if ( cr.getSingleton() ) {
                String closeMethod = cr.getCloseMethod();
                if ( closeMethod != null && closeMethod.length() > 0 ) {
                    String name = cr.getName();
                    Object resource;
                    try {
                        resource = ctxt.lookup ( name );
                    } catch ( NamingException e ) {
                        log.warn ( sm.getString (
                                       "namingResources.cleanupNoResource",
                                       cr.getName(), container ), e );
                        continue;
                    }
                    cleanUp ( resource, name, closeMethod );
                }
            }
        }
    }
    private void cleanUp ( Object resource, String name, String closeMethod ) {
        Method m = null;
        try {
            m = resource.getClass().getMethod ( closeMethod, ( Class<?>[] ) null );
        } catch ( SecurityException e ) {
            log.debug ( sm.getString ( "namingResources.cleanupCloseSecurity",
                                       closeMethod, name, container ) );
            return;
        } catch ( NoSuchMethodException e ) {
            log.debug ( sm.getString ( "namingResources.cleanupNoClose",
                                       name, container, closeMethod ) );
            return;
        }
        try {
            m.invoke ( resource, ( Object[] ) null );
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            log.warn ( sm.getString ( "namingResources.cleanupCloseFailed",
                                      closeMethod, name, container ), e );
        } catch ( InvocationTargetException e ) {
            Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            log.warn ( sm.getString ( "namingResources.cleanupCloseFailed",
                                      closeMethod, name, container ), t );
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        resourceRequireExplicitRegistration = false;
        for ( ContextResourceLink crl : resourceLinks.values() ) {
            try {
                MBeanUtils.destroyMBean ( crl );
            } catch ( Exception e ) {
                log.warn ( sm.getString (
                               "namingResources.mbeanDestroyFail", crl.getName() ), e );
            }
        }
        for ( ContextEnvironment ce : envs.values() ) {
            try {
                MBeanUtils.destroyMBean ( ce );
            } catch ( Exception e ) {
                log.warn ( sm.getString (
                               "namingResources.mbeanDestroyFail", ce.getName() ), e );
            }
        }
        for ( ContextResource cr : resources.values() ) {
            try {
                MBeanUtils.destroyMBean ( cr );
            } catch ( Exception e ) {
                log.warn ( sm.getString (
                               "namingResources.mbeanDestroyFail", cr.getName() ), e );
            }
        }
        super.destroyInternal();
    }
    @Override
    protected String getDomainInternal() {
        Object c = getContainer();
        if ( c instanceof JmxEnabled ) {
            return ( ( JmxEnabled ) c ).getDomain();
        }
        return null;
    }
    @Override
    protected String getObjectNameKeyProperties() {
        Object c = getContainer();
        if ( c instanceof Container ) {
            return "type=NamingResources" +
                   ( ( Container ) c ).getMBeanKeyProperties();
        }
        return "type=NamingResources";
    }
    private boolean checkResourceType ( ResourceBase resource ) {
        if ( ! ( container instanceof Context ) ) {
            return true;
        }
        if ( resource.getInjectionTargets() == null ||
                resource.getInjectionTargets().size() == 0 ) {
            return true;
        }
        Context context = ( Context ) container;
        String typeName = resource.getType();
        Class<?> typeClass = null;
        if ( typeName != null ) {
            typeClass = Introspection.loadClass ( context, typeName );
            if ( typeClass == null ) {
                return true;
            }
        }
        Class<?> compatibleClass =
            getCompatibleType ( context, resource, typeClass );
        if ( compatibleClass == null ) {
            return false;
        }
        resource.setType ( compatibleClass.getCanonicalName() );
        return true;
    }
    private Class<?> getCompatibleType ( Context context,
                                         ResourceBase resource, Class<?> typeClass ) {
        Class<?> result = null;
        for ( InjectionTarget injectionTarget : resource.getInjectionTargets() ) {
            Class<?> clazz = Introspection.loadClass (
                                 context, injectionTarget.getTargetClass() );
            if ( clazz == null ) {
                continue;
            }
            String targetName = injectionTarget.getTargetName();
            Class<?> targetType = getSetterType ( clazz, targetName );
            if ( targetType == null ) {
                targetType = getFieldType ( clazz, targetName );
            }
            if ( targetType == null ) {
                continue;
            }
            targetType = Introspection.convertPrimitiveType ( targetType );
            if ( typeClass == null ) {
                if ( result == null ) {
                    result = targetType;
                } else if ( targetType.isAssignableFrom ( result ) ) {
                } else if ( result.isAssignableFrom ( targetType ) ) {
                    result = targetType;
                } else {
                    return null;
                }
            } else {
                if ( targetType.isAssignableFrom ( typeClass ) ) {
                    result = typeClass;
                } else {
                    return null;
                }
            }
        }
        return result;
    }
    private Class<?> getSetterType ( Class<?> clazz, String name ) {
        Method[] methods = Introspection.getDeclaredMethods ( clazz );
        if ( methods != null && methods.length > 0 ) {
            for ( Method method : methods ) {
                if ( Introspection.isValidSetter ( method ) &&
                        Introspection.getPropertyName ( method ).equals ( name ) ) {
                    return method.getParameterTypes() [0];
                }
            }
        }
        return null;
    }
    private Class<?> getFieldType ( Class<?> clazz, String name ) {
        Field[] fields = Introspection.getDeclaredFields ( clazz );
        if ( fields != null && fields.length > 0 ) {
            for ( Field field : fields ) {
                if ( field.getName().equals ( name ) ) {
                    return field.getType();
                }
            }
        }
        return null;
    }
}

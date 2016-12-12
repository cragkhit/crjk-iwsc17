package org.apache.catalina.core;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.naming.EjbRef;
import org.apache.naming.HandlerRef;
import org.apache.naming.NamingContext;
import org.apache.naming.ResourceEnvRef;
import org.apache.naming.ResourceLinkRef;
import org.apache.naming.ResourceRef;
import org.apache.naming.ServiceRef;
import org.apache.naming.TransactionRef;
import org.apache.naming.factory.ResourceLinkFactory;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextHandler;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.apache.tomcat.util.descriptor.web.ContextTransaction;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public class NamingContextListener
    implements LifecycleListener, ContainerListener, PropertyChangeListener {
    private static final Log log = LogFactory.getLog ( NamingContextListener.class );
    protected String name = "/";
    protected Object container = null;
    private Object token = null;
    protected boolean initialized = false;
    protected NamingResourcesImpl namingResources = null;
    protected NamingContext namingContext = null;
    protected javax.naming.Context compCtx = null;
    protected javax.naming.Context envCtx = null;
    protected HashMap<String, ObjectName> objectNames = new HashMap<>();
    private boolean exceptionOnFailedWrite = true;
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    public boolean getExceptionOnFailedWrite() {
        return exceptionOnFailedWrite;
    }
    public void setExceptionOnFailedWrite ( boolean exceptionOnFailedWrite ) {
        this.exceptionOnFailedWrite = exceptionOnFailedWrite;
    }
    public String getName() {
        return ( this.name );
    }
    public void setName ( String name ) {
        this.name = name;
    }
    public javax.naming.Context getEnvContext() {
        return this.envCtx;
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        container = event.getLifecycle();
        if ( container instanceof Context ) {
            namingResources = ( ( Context ) container ).getNamingResources();
            token = ( ( Context ) container ).getNamingToken();
        } else if ( container instanceof Server ) {
            namingResources = ( ( Server ) container ).getGlobalNamingResources();
            token = ( ( Server ) container ).getNamingToken();
        } else {
            return;
        }
        if ( Lifecycle.CONFIGURE_START_EVENT.equals ( event.getType() ) ) {
            if ( initialized ) {
                return;
            }
            try {
                Hashtable<String, Object> contextEnv = new Hashtable<>();
                namingContext = new NamingContext ( contextEnv, getName() );
                ContextAccessController.setSecurityToken ( getName(), token );
                ContextAccessController.setSecurityToken ( container, token );
                ContextBindings.bindContext ( container, namingContext, token );
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Bound " + container );
                }
                namingContext.setExceptionOnFailedWrite (
                    getExceptionOnFailedWrite() );
                ContextAccessController.setWritable ( getName(), token );
                try {
                    createNamingContext();
                } catch ( NamingException e ) {
                    log.error
                    ( sm.getString ( "naming.namingContextCreationFailed", e ) );
                }
                namingResources.addPropertyChangeListener ( this );
                if ( container instanceof Context ) {
                    ContextAccessController.setReadOnly ( getName() );
                    try {
                        ContextBindings.bindClassLoader ( container, token,
                                                          ( ( Context ) container ).getLoader().getClassLoader() );
                    } catch ( NamingException e ) {
                        log.error ( sm.getString ( "naming.bindFailed", e ) );
                    }
                }
                if ( container instanceof Server ) {
                    org.apache.naming.factory.ResourceLinkFactory.setGlobalContext
                    ( namingContext );
                    try {
                        ContextBindings.bindClassLoader ( container, token,
                                                          this.getClass().getClassLoader() );
                    } catch ( NamingException e ) {
                        log.error ( sm.getString ( "naming.bindFailed", e ) );
                    }
                    if ( container instanceof StandardServer ) {
                        ( ( StandardServer ) container ).setGlobalNamingContext
                        ( namingContext );
                    }
                }
            } finally {
                initialized = true;
            }
        } else if ( Lifecycle.CONFIGURE_STOP_EVENT.equals ( event.getType() ) ) {
            if ( !initialized ) {
                return;
            }
            try {
                ContextAccessController.setWritable ( getName(), token );
                ContextBindings.unbindContext ( container, token );
                if ( container instanceof Context ) {
                    ContextBindings.unbindClassLoader ( container, token,
                                                        ( ( Context ) container ).getLoader().getClassLoader() );
                }
                if ( container instanceof Server ) {
                    namingResources.removePropertyChangeListener ( this );
                    ContextBindings.unbindClassLoader ( container, token,
                                                        this.getClass().getClassLoader() );
                }
                ContextAccessController.unsetSecurityToken ( getName(), token );
                ContextAccessController.unsetSecurityToken ( container, token );
                if ( !objectNames.isEmpty() ) {
                    Collection<ObjectName> names = objectNames.values();
                    Registry registry = Registry.getRegistry ( null, null );
                    for ( ObjectName objectName : names ) {
                        registry.unregisterComponent ( objectName );
                    }
                }
                javax.naming.Context global = getGlobalNamingContext();
                if ( global != null ) {
                    ResourceLinkFactory.deregisterGlobalResourceAccess ( global );
                }
            } finally {
                objectNames.clear();
                namingContext = null;
                envCtx = null;
                compCtx = null;
                initialized = false;
            }
        }
    }
    @Override
    public void containerEvent ( ContainerEvent event ) {
        if ( !initialized ) {
            return;
        }
        ContextAccessController.setWritable ( getName(), token );
        String type = event.getType();
        if ( type.equals ( "addEjb" ) ) {
            String ejbName = ( String ) event.getData();
            if ( ejbName != null ) {
                ContextEjb ejb = namingResources.findEjb ( ejbName );
                addEjb ( ejb );
            }
        } else if ( type.equals ( "addEnvironment" ) ) {
            String environmentName = ( String ) event.getData();
            if ( environmentName != null ) {
                ContextEnvironment env =
                    namingResources.findEnvironment ( environmentName );
                addEnvironment ( env );
            }
        } else if ( type.equals ( "addLocalEjb" ) ) {
            String localEjbName = ( String ) event.getData();
            if ( localEjbName != null ) {
                ContextLocalEjb localEjb =
                    namingResources.findLocalEjb ( localEjbName );
                addLocalEjb ( localEjb );
            }
        } else if ( type.equals ( "addResource" ) ) {
            String resourceName = ( String ) event.getData();
            if ( resourceName != null ) {
                ContextResource resource =
                    namingResources.findResource ( resourceName );
                addResource ( resource );
            }
        } else if ( type.equals ( "addResourceLink" ) ) {
            String resourceLinkName = ( String ) event.getData();
            if ( resourceLinkName != null ) {
                ContextResourceLink resourceLink =
                    namingResources.findResourceLink ( resourceLinkName );
                addResourceLink ( resourceLink );
            }
        } else if ( type.equals ( "addResourceEnvRef" ) ) {
            String resourceEnvRefName = ( String ) event.getData();
            if ( resourceEnvRefName != null ) {
                ContextResourceEnvRef resourceEnvRef =
                    namingResources.findResourceEnvRef ( resourceEnvRefName );
                addResourceEnvRef ( resourceEnvRef );
            }
        } else if ( type.equals ( "addService" ) ) {
            String serviceName = ( String ) event.getData();
            if ( serviceName != null ) {
                ContextService service =
                    namingResources.findService ( serviceName );
                addService ( service );
            }
        } else if ( type.equals ( "removeEjb" ) ) {
            String ejbName = ( String ) event.getData();
            if ( ejbName != null ) {
                removeEjb ( ejbName );
            }
        } else if ( type.equals ( "removeEnvironment" ) ) {
            String environmentName = ( String ) event.getData();
            if ( environmentName != null ) {
                removeEnvironment ( environmentName );
            }
        } else if ( type.equals ( "removeLocalEjb" ) ) {
            String localEjbName = ( String ) event.getData();
            if ( localEjbName != null ) {
                removeLocalEjb ( localEjbName );
            }
        } else if ( type.equals ( "removeResource" ) ) {
            String resourceName = ( String ) event.getData();
            if ( resourceName != null ) {
                removeResource ( resourceName );
            }
        } else if ( type.equals ( "removeResourceLink" ) ) {
            String resourceLinkName = ( String ) event.getData();
            if ( resourceLinkName != null ) {
                removeResourceLink ( resourceLinkName );
            }
        } else if ( type.equals ( "removeResourceEnvRef" ) ) {
            String resourceEnvRefName = ( String ) event.getData();
            if ( resourceEnvRefName != null ) {
                removeResourceEnvRef ( resourceEnvRefName );
            }
        } else if ( type.equals ( "removeService" ) ) {
            String serviceName = ( String ) event.getData();
            if ( serviceName != null ) {
                removeService ( serviceName );
            }
        }
        ContextAccessController.setReadOnly ( getName() );
    }
    @Override
    public void propertyChange ( PropertyChangeEvent event ) {
        if ( !initialized ) {
            return;
        }
        Object source = event.getSource();
        if ( source == namingResources ) {
            ContextAccessController.setWritable ( getName(), token );
            processGlobalResourcesChange ( event.getPropertyName(),
                                           event.getOldValue(),
                                           event.getNewValue() );
            ContextAccessController.setReadOnly ( getName() );
        }
    }
    private void processGlobalResourcesChange ( String name,
            Object oldValue,
            Object newValue ) {
        if ( name.equals ( "ejb" ) ) {
            if ( oldValue != null ) {
                ContextEjb ejb = ( ContextEjb ) oldValue;
                if ( ejb.getName() != null ) {
                    removeEjb ( ejb.getName() );
                }
            }
            if ( newValue != null ) {
                ContextEjb ejb = ( ContextEjb ) newValue;
                if ( ejb.getName() != null ) {
                    addEjb ( ejb );
                }
            }
        } else if ( name.equals ( "environment" ) ) {
            if ( oldValue != null ) {
                ContextEnvironment env = ( ContextEnvironment ) oldValue;
                if ( env.getName() != null ) {
                    removeEnvironment ( env.getName() );
                }
            }
            if ( newValue != null ) {
                ContextEnvironment env = ( ContextEnvironment ) newValue;
                if ( env.getName() != null ) {
                    addEnvironment ( env );
                }
            }
        } else if ( name.equals ( "localEjb" ) ) {
            if ( oldValue != null ) {
                ContextLocalEjb ejb = ( ContextLocalEjb ) oldValue;
                if ( ejb.getName() != null ) {
                    removeLocalEjb ( ejb.getName() );
                }
            }
            if ( newValue != null ) {
                ContextLocalEjb ejb = ( ContextLocalEjb ) newValue;
                if ( ejb.getName() != null ) {
                    addLocalEjb ( ejb );
                }
            }
        } else if ( name.equals ( "resource" ) ) {
            if ( oldValue != null ) {
                ContextResource resource = ( ContextResource ) oldValue;
                if ( resource.getName() != null ) {
                    removeResource ( resource.getName() );
                }
            }
            if ( newValue != null ) {
                ContextResource resource = ( ContextResource ) newValue;
                if ( resource.getName() != null ) {
                    addResource ( resource );
                }
            }
        } else if ( name.equals ( "resourceEnvRef" ) ) {
            if ( oldValue != null ) {
                ContextResourceEnvRef resourceEnvRef =
                    ( ContextResourceEnvRef ) oldValue;
                if ( resourceEnvRef.getName() != null ) {
                    removeResourceEnvRef ( resourceEnvRef.getName() );
                }
            }
            if ( newValue != null ) {
                ContextResourceEnvRef resourceEnvRef =
                    ( ContextResourceEnvRef ) newValue;
                if ( resourceEnvRef.getName() != null ) {
                    addResourceEnvRef ( resourceEnvRef );
                }
            }
        } else if ( name.equals ( "resourceLink" ) ) {
            if ( oldValue != null ) {
                ContextResourceLink rl = ( ContextResourceLink ) oldValue;
                if ( rl.getName() != null ) {
                    removeResourceLink ( rl.getName() );
                }
            }
            if ( newValue != null ) {
                ContextResourceLink rl = ( ContextResourceLink ) newValue;
                if ( rl.getName() != null ) {
                    addResourceLink ( rl );
                }
            }
        } else if ( name.equals ( "service" ) ) {
            if ( oldValue != null ) {
                ContextService service = ( ContextService ) oldValue;
                if ( service.getName() != null ) {
                    removeService ( service.getName() );
                }
            }
            if ( newValue != null ) {
                ContextService service = ( ContextService ) newValue;
                if ( service.getName() != null ) {
                    addService ( service );
                }
            }
        }
    }
    private void createNamingContext()
    throws NamingException {
        if ( container instanceof Server ) {
            compCtx = namingContext;
            envCtx = namingContext;
        } else {
            compCtx = namingContext.createSubcontext ( "comp" );
            envCtx = compCtx.createSubcontext ( "env" );
        }
        int i;
        if ( log.isDebugEnabled() ) {
            log.debug ( "Creating JNDI naming context" );
        }
        if ( namingResources == null ) {
            namingResources = new NamingResourcesImpl();
            namingResources.setContainer ( container );
        }
        ContextResourceLink[] resourceLinks =
            namingResources.findResourceLinks();
        for ( i = 0; i < resourceLinks.length; i++ ) {
            addResourceLink ( resourceLinks[i] );
        }
        ContextResource[] resources = namingResources.findResources();
        for ( i = 0; i < resources.length; i++ ) {
            addResource ( resources[i] );
        }
        ContextResourceEnvRef[] resourceEnvRefs = namingResources.findResourceEnvRefs();
        for ( i = 0; i < resourceEnvRefs.length; i++ ) {
            addResourceEnvRef ( resourceEnvRefs[i] );
        }
        ContextEnvironment[] contextEnvironments =
            namingResources.findEnvironments();
        for ( i = 0; i < contextEnvironments.length; i++ ) {
            addEnvironment ( contextEnvironments[i] );
        }
        ContextEjb[] ejbs = namingResources.findEjbs();
        for ( i = 0; i < ejbs.length; i++ ) {
            addEjb ( ejbs[i] );
        }
        ContextService[] services = namingResources.findServices();
        for ( i = 0; i < services.length; i++ ) {
            addService ( services[i] );
        }
        if ( container instanceof Context ) {
            try {
                Reference ref = new TransactionRef();
                compCtx.bind ( "UserTransaction", ref );
                ContextTransaction transaction = namingResources.getTransaction();
                if ( transaction != null ) {
                    Iterator<String> params = transaction.listProperties();
                    while ( params.hasNext() ) {
                        String paramName = params.next();
                        String paramValue = ( String ) transaction.getProperty ( paramName );
                        StringRefAddr refAddr = new StringRefAddr ( paramName, paramValue );
                        ref.add ( refAddr );
                    }
                }
            } catch ( NameAlreadyBoundException e ) {
            } catch ( NamingException e ) {
                log.error ( sm.getString ( "naming.bindFailed", e ) );
            }
        }
        if ( container instanceof Context ) {
            try {
                compCtx.bind ( "Resources",
                               ( ( Context ) container ).getResources() );
            } catch ( NamingException e ) {
                log.error ( sm.getString ( "naming.bindFailed", e ) );
            }
        }
    }
    protected ObjectName createObjectName ( ContextResource resource )
    throws MalformedObjectNameException {
        String domain = null;
        if ( container instanceof StandardServer ) {
            domain = ( ( StandardServer ) container ).getDomain();
        } else if ( container instanceof ContainerBase ) {
            domain = ( ( ContainerBase ) container ).getDomain();
        }
        if ( domain == null ) {
            domain = "Catalina";
        }
        ObjectName name = null;
        String quotedResourceName = ObjectName.quote ( resource.getName() );
        if ( container instanceof Server ) {
            name = new ObjectName ( domain + ":type=DataSource" +
                                    ",class=" + resource.getType() +
                                    ",name=" + quotedResourceName );
        } else if ( container instanceof Context ) {
            String contextName = ( ( Context ) container ).getName();
            if ( !contextName.startsWith ( "/" ) ) {
                contextName = "/" + contextName;
            }
            Host host = ( Host ) ( ( Context ) container ).getParent();
            name = new ObjectName ( domain + ":type=DataSource" +
                                    ",host=" + host.getName() +
                                    ",context=" + contextName +
                                    ",class=" + resource.getType() +
                                    ",name=" + quotedResourceName );
        }
        return ( name );
    }
    public void addEjb ( ContextEjb ejb ) {
        Reference ref = new EjbRef
        ( ejb.getType(), ejb.getHome(), ejb.getRemote(), ejb.getLink() );
        Iterator<String> params = ejb.listProperties();
        while ( params.hasNext() ) {
            String paramName = params.next();
            String paramValue = ( String ) ejb.getProperty ( paramName );
            StringRefAddr refAddr = new StringRefAddr ( paramName, paramValue );
            ref.add ( refAddr );
        }
        try {
            createSubcontexts ( envCtx, ejb.getName() );
            envCtx.bind ( ejb.getName(), ref );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.bindFailed", e ) );
        }
    }
    public void addEnvironment ( ContextEnvironment env ) {
        Object value = null;
        String type = env.getType();
        try {
            if ( type.equals ( "java.lang.String" ) ) {
                value = env.getValue();
            } else if ( type.equals ( "java.lang.Byte" ) ) {
                if ( env.getValue() == null ) {
                    value = Byte.valueOf ( ( byte ) 0 );
                } else {
                    value = Byte.decode ( env.getValue() );
                }
            } else if ( type.equals ( "java.lang.Short" ) ) {
                if ( env.getValue() == null ) {
                    value = Short.valueOf ( ( short ) 0 );
                } else {
                    value = Short.decode ( env.getValue() );
                }
            } else if ( type.equals ( "java.lang.Integer" ) ) {
                if ( env.getValue() == null ) {
                    value = Integer.valueOf ( 0 );
                } else {
                    value = Integer.decode ( env.getValue() );
                }
            } else if ( type.equals ( "java.lang.Long" ) ) {
                if ( env.getValue() == null ) {
                    value = Long.valueOf ( 0 );
                } else {
                    value = Long.decode ( env.getValue() );
                }
            } else if ( type.equals ( "java.lang.Boolean" ) ) {
                value = Boolean.valueOf ( env.getValue() );
            } else if ( type.equals ( "java.lang.Double" ) ) {
                if ( env.getValue() == null ) {
                    value = Double.valueOf ( 0 );
                } else {
                    value = Double.valueOf ( env.getValue() );
                }
            } else if ( type.equals ( "java.lang.Float" ) ) {
                if ( env.getValue() == null ) {
                    value = Float.valueOf ( 0 );
                } else {
                    value = Float.valueOf ( env.getValue() );
                }
            } else if ( type.equals ( "java.lang.Character" ) ) {
                if ( env.getValue() == null ) {
                    value = Character.valueOf ( ( char ) 0 );
                } else {
                    if ( env.getValue().length() == 1 ) {
                        value = Character.valueOf ( env.getValue().charAt ( 0 ) );
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            } else {
                value = constructEnvEntry ( env.getType(), env.getValue() );
                if ( value == null ) {
                    log.error ( sm.getString (
                                    "naming.invalidEnvEntryType", env.getName() ) );
                }
            }
        } catch ( NumberFormatException e ) {
            log.error ( sm.getString ( "naming.invalidEnvEntryValue", env.getName() ) );
        } catch ( IllegalArgumentException e ) {
            log.error ( sm.getString ( "naming.invalidEnvEntryValue", env.getName() ) );
        }
        if ( value != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  Adding environment entry " + env.getName() );
                }
                createSubcontexts ( envCtx, env.getName() );
                envCtx.bind ( env.getName(), value );
            } catch ( NamingException e ) {
                log.error ( sm.getString ( "naming.invalidEnvEntryValue", e ) );
            }
        }
    }
    private Object constructEnvEntry ( String type, String value ) {
        try {
            Class<?> clazz = Class.forName ( type );
            Constructor<?> c = null;
            try {
                c = clazz.getConstructor ( String.class );
                return c.newInstance ( value );
            } catch ( NoSuchMethodException e ) {
            }
            if ( value.length() != 1 ) {
                return null;
            }
            try {
                c = clazz.getConstructor ( char.class );
                return c.newInstance ( Character.valueOf ( value.charAt ( 0 ) ) );
            } catch ( NoSuchMethodException e ) {
            }
        } catch ( Exception e ) {
        }
        return null;
    }
    public void addLocalEjb ( ContextLocalEjb localEjb ) {
    }
    public void addService ( ContextService service ) {
        if ( service.getWsdlfile() != null ) {
            URL wsdlURL = null;
            try {
                wsdlURL = new URL ( service.getWsdlfile() );
            } catch ( MalformedURLException e ) {
            }
            if ( wsdlURL == null ) {
                try {
                    wsdlURL = ( ( Context ) container ).
                              getServletContext().
                              getResource ( service.getWsdlfile() );
                } catch ( MalformedURLException e ) {
                }
            }
            if ( wsdlURL == null ) {
                try {
                    wsdlURL = ( ( Context ) container ).
                              getServletContext().
                              getResource ( "/" + service.getWsdlfile() );
                    log.debug ( "  Changing service ref wsdl file for /"
                                + service.getWsdlfile() );
                } catch ( MalformedURLException e ) {
                    log.error ( sm.getString ( "naming.wsdlFailed", e ) );
                }
            }
            if ( wsdlURL == null ) {
                service.setWsdlfile ( null );
            } else {
                service.setWsdlfile ( wsdlURL.toString() );
            }
        }
        if ( service.getJaxrpcmappingfile() != null ) {
            URL jaxrpcURL = null;
            try {
                jaxrpcURL = new URL ( service.getJaxrpcmappingfile() );
            } catch ( MalformedURLException e ) {
            }
            if ( jaxrpcURL == null ) {
                try {
                    jaxrpcURL = ( ( Context ) container ).
                                getServletContext().
                                getResource ( service.getJaxrpcmappingfile() );
                } catch ( MalformedURLException e ) {
                }
            }
            if ( jaxrpcURL == null ) {
                try {
                    jaxrpcURL = ( ( Context ) container ).
                                getServletContext().
                                getResource ( "/" + service.getJaxrpcmappingfile() );
                    log.debug ( "  Changing service ref jaxrpc file for /"
                                + service.getJaxrpcmappingfile() );
                } catch ( MalformedURLException e ) {
                    log.error ( sm.getString ( "naming.wsdlFailed", e ) );
                }
            }
            if ( jaxrpcURL == null ) {
                service.setJaxrpcmappingfile ( null );
            } else {
                service.setJaxrpcmappingfile ( jaxrpcURL.toString() );
            }
        }
        Reference ref = new ServiceRef
        ( service.getName(), service.getType(), service.getServiceqname(),
          service.getWsdlfile(), service.getJaxrpcmappingfile() );
        Iterator<String> portcomponent = service.getServiceendpoints();
        while ( portcomponent.hasNext() ) {
            String serviceendpoint = portcomponent.next();
            StringRefAddr refAddr = new StringRefAddr ( ServiceRef.SERVICEENDPOINTINTERFACE, serviceendpoint );
            ref.add ( refAddr );
            String portlink = service.getPortlink ( serviceendpoint );
            refAddr = new StringRefAddr ( ServiceRef.PORTCOMPONENTLINK, portlink );
            ref.add ( refAddr );
        }
        Iterator<String> handlers = service.getHandlers();
        while ( handlers.hasNext() ) {
            String handlername = handlers.next();
            ContextHandler handler = service.getHandler ( handlername );
            HandlerRef handlerRef = new HandlerRef ( handlername, handler.getHandlerclass() );
            Iterator<String> localParts = handler.getLocalparts();
            while ( localParts.hasNext() ) {
                String localPart = localParts.next();
                String namespaceURI = handler.getNamespaceuri ( localPart );
                handlerRef.add ( new StringRefAddr ( HandlerRef.HANDLER_LOCALPART, localPart ) );
                handlerRef.add ( new StringRefAddr ( HandlerRef.HANDLER_NAMESPACE, namespaceURI ) );
            }
            Iterator<String> params = handler.listProperties();
            while ( params.hasNext() ) {
                String paramName = params.next();
                String paramValue = ( String ) handler.getProperty ( paramName );
                handlerRef.add ( new StringRefAddr ( HandlerRef.HANDLER_PARAMNAME, paramName ) );
                handlerRef.add ( new StringRefAddr ( HandlerRef.HANDLER_PARAMVALUE, paramValue ) );
            }
            for ( int i = 0; i < handler.getSoapRolesSize(); i++ ) {
                handlerRef.add ( new StringRefAddr ( HandlerRef.HANDLER_SOAPROLE, handler.getSoapRole ( i ) ) );
            }
            for ( int i = 0; i < handler.getPortNamesSize(); i++ ) {
                handlerRef.add ( new StringRefAddr ( HandlerRef.HANDLER_PORTNAME, handler.getPortName ( i ) ) );
            }
            ( ( ServiceRef ) ref ).addHandler ( handlerRef );
        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Adding service ref "
                            + service.getName() + "  " + ref );
            }
            createSubcontexts ( envCtx, service.getName() );
            envCtx.bind ( service.getName(), ref );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.bindFailed", e ) );
        }
    }
    public void addResource ( ContextResource resource ) {
        Reference ref = new ResourceRef
        ( resource.getType(), resource.getDescription(),
          resource.getScope(), resource.getAuth(),
          resource.getSingleton() );
        Iterator<String> params = resource.listProperties();
        while ( params.hasNext() ) {
            String paramName = params.next();
            String paramValue = ( String ) resource.getProperty ( paramName );
            StringRefAddr refAddr = new StringRefAddr ( paramName, paramValue );
            ref.add ( refAddr );
        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Adding resource ref "
                            + resource.getName() + "  " + ref );
            }
            createSubcontexts ( envCtx, resource.getName() );
            envCtx.bind ( resource.getName(), ref );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.bindFailed", e ) );
        }
        if ( "javax.sql.DataSource".equals ( ref.getClassName() ) &&
                resource.getSingleton() ) {
            try {
                ObjectName on = createObjectName ( resource );
                Object actualResource = envCtx.lookup ( resource.getName() );
                Registry.getRegistry ( null, null ).registerComponent ( actualResource, on, null );
                objectNames.put ( resource.getName(), on );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "naming.jmxRegistrationFailed", e ) );
            }
        }
    }
    public void addResourceEnvRef ( ContextResourceEnvRef resourceEnvRef ) {
        Reference ref = new ResourceEnvRef ( resourceEnvRef.getType() );
        Iterator<String> params = resourceEnvRef.listProperties();
        while ( params.hasNext() ) {
            String paramName = params.next();
            String paramValue = ( String ) resourceEnvRef.getProperty ( paramName );
            StringRefAddr refAddr = new StringRefAddr ( paramName, paramValue );
            ref.add ( refAddr );
        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Adding resource env ref " + resourceEnvRef.getName() );
            }
            createSubcontexts ( envCtx, resourceEnvRef.getName() );
            envCtx.bind ( resourceEnvRef.getName(), ref );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.bindFailed", e ) );
        }
    }
    public void addResourceLink ( ContextResourceLink resourceLink ) {
        Reference ref = new ResourceLinkRef
        ( resourceLink.getType(), resourceLink.getGlobal(), resourceLink.getFactory(), null );
        Iterator<String> i = resourceLink.listProperties();
        while ( i.hasNext() ) {
            String key = i.next();
            Object val = resourceLink.getProperty ( key );
            if ( val != null ) {
                StringRefAddr refAddr = new StringRefAddr ( key, val.toString() );
                ref.add ( refAddr );
            }
        }
        javax.naming.Context ctx =
            "UserTransaction".equals ( resourceLink.getName() )
            ? compCtx : envCtx;
        try {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Adding resource link " + resourceLink.getName() );
            }
            createSubcontexts ( envCtx, resourceLink.getName() );
            ctx.bind ( resourceLink.getName(), ref );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.bindFailed", e ) );
        }
        ResourceLinkFactory.registerGlobalResourceAccess (
            getGlobalNamingContext(), resourceLink.getName(), resourceLink.getGlobal() );
    }
    private javax.naming.Context getGlobalNamingContext() {
        if ( container instanceof Context ) {
            Engine e = ( Engine ) ( ( Context ) container ).getParent().getParent();
            return e.getService().getServer().getGlobalNamingContext();
        }
        return null;
    }
    public void removeEjb ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
    }
    public void removeEnvironment ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
    }
    public void removeLocalEjb ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
    }
    public void removeService ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
    }
    public void removeResource ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
        ObjectName on = objectNames.get ( name );
        if ( on != null ) {
            Registry.getRegistry ( null, null ).unregisterComponent ( on );
        }
    }
    public void removeResourceEnvRef ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
    }
    public void removeResourceLink ( String name ) {
        try {
            envCtx.unbind ( name );
        } catch ( NamingException e ) {
            log.error ( sm.getString ( "naming.unbindFailed", e ) );
        }
        ResourceLinkFactory.deregisterGlobalResourceAccess ( getGlobalNamingContext(), name );
    }
    private void createSubcontexts ( javax.naming.Context ctx, String name )
    throws NamingException {
        javax.naming.Context currentContext = ctx;
        StringTokenizer tokenizer = new StringTokenizer ( name, "/" );
        while ( tokenizer.hasMoreTokens() ) {
            String token = tokenizer.nextToken();
            if ( ( !token.equals ( "" ) ) && ( tokenizer.hasMoreTokens() ) ) {
                try {
                    currentContext = currentContext.createSubcontext ( token );
                } catch ( NamingException e ) {
                    currentContext =
                        ( javax.naming.Context ) currentContext.lookup ( token );
                }
            }
        }
    }
}

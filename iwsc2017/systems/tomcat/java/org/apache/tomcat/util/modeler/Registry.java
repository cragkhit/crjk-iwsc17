package org.apache.tomcat.util.modeler;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.modules.ModelerSource;
public class Registry implements RegistryMBean, MBeanRegistration  {
    private static final Log log = LogFactory.getLog ( Registry.class );
    private static final HashMap<Object, Registry> perLoaderRegistries = null;
    private static Registry registry = null;
    private MBeanServer server = null;
    private HashMap<String, ManagedBean> descriptors = new HashMap<>();
    private HashMap<String, ManagedBean> descriptorsByClass = new HashMap<>();
    private HashMap<String, URL> searchedPaths = new HashMap<>();
    private Object guard;
    private final Hashtable<String, Hashtable<String, Integer>> idDomains =
        new Hashtable<>();
    private final Hashtable<String, int[]> ids = new Hashtable<>();
    public Registry() {
        super();
    }
    public static synchronized Registry getRegistry ( Object key, Object guard ) {
        Registry localRegistry;
        if ( perLoaderRegistries != null ) {
            if ( key == null ) {
                key = Thread.currentThread().getContextClassLoader();
            }
            if ( key != null ) {
                localRegistry = perLoaderRegistries.get ( key );
                if ( localRegistry == null ) {
                    localRegistry = new Registry();
                    localRegistry.guard = guard;
                    perLoaderRegistries.put ( key, localRegistry );
                    return localRegistry;
                }
                if ( localRegistry.guard != null &&
                        localRegistry.guard != guard ) {
                    return null;
                }
                return localRegistry;
            }
        }
        if ( registry == null ) {
            registry = new Registry();
        }
        if ( registry.guard != null &&
                registry.guard != guard ) {
            return null;
        }
        return ( registry );
    }
    @Override
    public void stop() {
        descriptorsByClass = new HashMap<>();
        descriptors = new HashMap<>();
        searchedPaths = new HashMap<>();
    }
    @Override
    public void registerComponent ( Object bean, String oname, String type )
    throws Exception {
        registerComponent ( bean, new ObjectName ( oname ), type );
    }
    @Override
    public void unregisterComponent ( String oname ) {
        try {
            unregisterComponent ( new ObjectName ( oname ) );
        } catch ( MalformedObjectNameException e ) {
            log.info ( "Error creating object name " + e );
        }
    }
    @Override
    public void invoke ( List<ObjectName> mbeans, String operation,
                         boolean failFirst ) throws Exception {
        if ( mbeans == null ) {
            return;
        }
        Iterator<ObjectName> itr = mbeans.iterator();
        while ( itr.hasNext() ) {
            ObjectName current = itr.next();
            try {
                if ( current == null ) {
                    continue;
                }
                if ( getMethodInfo ( current, operation ) == null ) {
                    continue;
                }
                getMBeanServer().invoke ( current, operation,
                                          new Object[] {}, new String[] {} );
            } catch ( Exception t ) {
                if ( failFirst ) {
                    throw t;
                }
                log.info ( "Error initializing " + current + " " + t.toString() );
            }
        }
    }
    @Override
    public synchronized int getId ( String domain, String name ) {
        if ( domain == null ) {
            domain = "";
        }
        Hashtable<String, Integer> domainTable = idDomains.get ( domain );
        if ( domainTable == null ) {
            domainTable = new Hashtable<>();
            idDomains.put ( domain, domainTable );
        }
        if ( name == null ) {
            name = "";
        }
        Integer i = domainTable.get ( name );
        if ( i != null ) {
            return i.intValue();
        }
        int id[] = ids.get ( domain );
        if ( id == null ) {
            id = new int[1];
            ids.put ( domain, id );
        }
        int code = id[0]++;
        domainTable.put ( name, Integer.valueOf ( code ) );
        return code;
    }
    public void addManagedBean ( ManagedBean bean ) {
        descriptors.put ( bean.getName(), bean );
        if ( bean.getType() != null ) {
            descriptorsByClass.put ( bean.getType(), bean );
        }
    }
    public ManagedBean findManagedBean ( String name ) {
        ManagedBean mb = descriptors.get ( name );
        if ( mb == null ) {
            mb = descriptorsByClass.get ( name );
        }
        return mb;
    }
    public String getType ( ObjectName oname, String attName ) {
        String type = null;
        MBeanInfo info = null;
        try {
            info = server.getMBeanInfo ( oname );
        } catch ( Exception e ) {
            log.info ( "Can't find metadata for object" + oname );
            return null;
        }
        MBeanAttributeInfo attInfo[] = info.getAttributes();
        for ( int i = 0; i < attInfo.length; i++ ) {
            if ( attName.equals ( attInfo[i].getName() ) ) {
                type = attInfo[i].getType();
                return type;
            }
        }
        return null;
    }
    public MBeanOperationInfo getMethodInfo ( ObjectName oname, String opName ) {
        MBeanInfo info = null;
        try {
            info = server.getMBeanInfo ( oname );
        } catch ( Exception e ) {
            log.info ( "Can't find metadata " + oname );
            return null;
        }
        MBeanOperationInfo attInfo[] = info.getOperations();
        for ( int i = 0; i < attInfo.length; i++ ) {
            if ( opName.equals ( attInfo[i].getName() ) ) {
                return attInfo[i];
            }
        }
        return null;
    }
    public void unregisterComponent ( ObjectName oname ) {
        try {
            if ( oname != null && getMBeanServer().isRegistered ( oname ) ) {
                getMBeanServer().unregisterMBean ( oname );
            }
        } catch ( Throwable t ) {
            log.error ( "Error unregistering mbean", t );
        }
    }
    public synchronized MBeanServer getMBeanServer() {
        if ( server == null ) {
            long t1 = System.currentTimeMillis();
            if ( MBeanServerFactory.findMBeanServer ( null ).size() > 0 ) {
                server = MBeanServerFactory.findMBeanServer ( null ).get ( 0 );
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Using existing MBeanServer " + ( System.currentTimeMillis() - t1 ) );
                }
            } else {
                server = ManagementFactory.getPlatformMBeanServer();
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Creating MBeanServer" + ( System.currentTimeMillis() - t1 ) );
                }
            }
        }
        return server;
    }
    public ManagedBean findManagedBean ( Object bean, Class<?> beanClass,
                                         String type ) throws Exception {
        if ( bean != null && beanClass == null ) {
            beanClass = bean.getClass();
        }
        if ( type == null ) {
            type = beanClass.getName();
        }
        ManagedBean managed = findManagedBean ( type );
        if ( managed == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Looking for descriptor " );
            }
            findDescriptor ( beanClass, type );
            managed = findManagedBean ( type );
        }
        if ( managed == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Introspecting " );
            }
            load ( "MbeansDescriptorsIntrospectionSource", beanClass, type );
            managed = findManagedBean ( type );
            if ( managed == null ) {
                log.warn ( "No metadata found for " + type );
                return null;
            }
            managed.setName ( type );
            addManagedBean ( managed );
        }
        return managed;
    }
    public Object convertValue ( String type, String value ) {
        Object objValue = value;
        if ( type == null || "java.lang.String".equals ( type ) ) {
            objValue = value;
        } else if ( "javax.management.ObjectName".equals ( type ) ||
                    "ObjectName".equals ( type ) ) {
            try {
                objValue = new ObjectName ( value );
            } catch ( MalformedObjectNameException e ) {
                return null;
            }
        } else if ( "java.lang.Integer".equals ( type ) ||
                    "int".equals ( type ) ) {
            objValue = Integer.valueOf ( value );
        } else if ( "java.lang.Long".equals ( type ) ||
                    "long".equals ( type ) ) {
            objValue = Long.valueOf ( value );
        } else if ( "java.lang.Boolean".equals ( type ) ||
                    "boolean".equals ( type ) ) {
            objValue = Boolean.valueOf ( value );
        }
        return objValue;
    }
    public List<ObjectName> load ( String sourceType, Object source,
                                   String param ) throws Exception {
        if ( log.isTraceEnabled() ) {
            log.trace ( "load " + source );
        }
        String location = null;
        String type = null;
        Object inputsource = null;
        if ( source instanceof URL ) {
            URL url = ( URL ) source;
            location = url.toString();
            type = param;
            inputsource = url.openStream();
            if ( sourceType == null && location.endsWith ( ".xml" ) ) {
                sourceType = "MbeansDescriptorsDigesterSource";
            }
        } else if ( source instanceof File ) {
            location = ( ( File ) source ).getAbsolutePath();
            inputsource = new FileInputStream ( ( File ) source );
            type = param;
            if ( sourceType == null && location.endsWith ( ".xml" ) ) {
                sourceType = "MbeansDescriptorsDigesterSource";
            }
        } else if ( source instanceof InputStream ) {
            type = param;
            inputsource = source;
        } else if ( source instanceof Class<?> ) {
            location = ( ( Class<?> ) source ).getName();
            type = param;
            inputsource = source;
            if ( sourceType == null ) {
                sourceType = "MbeansDescriptorsIntrospectionSource";
            }
        }
        if ( sourceType == null ) {
            sourceType = "MbeansDescriptorsDigesterSource";
        }
        ModelerSource ds = getModelerSource ( sourceType );
        List<ObjectName> mbeans =
            ds.loadDescriptors ( this, type, inputsource );
        return mbeans;
    }
    public void registerComponent ( Object bean, ObjectName oname, String type )
    throws Exception {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Managed= " + oname );
        }
        if ( bean == null ) {
            log.error ( "Null component " + oname );
            return;
        }
        try {
            if ( type == null ) {
                type = bean.getClass().getName();
            }
            ManagedBean managed = findManagedBean ( null, bean.getClass(), type );
            DynamicMBean mbean = managed.createMBean ( bean );
            if ( getMBeanServer().isRegistered ( oname ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unregistering existing component " + oname );
                }
                getMBeanServer().unregisterMBean ( oname );
            }
            getMBeanServer().registerMBean ( mbean, oname );
        } catch ( Exception ex ) {
            log.error ( "Error registering " + oname, ex );
            throw ex;
        }
    }
    public void loadDescriptors ( String packageName, ClassLoader classLoader ) {
        String res = packageName.replace ( '.', '/' );
        if ( log.isTraceEnabled() ) {
            log.trace ( "Finding descriptor " + res );
        }
        if ( searchedPaths.get ( packageName ) != null ) {
            return;
        }
        String descriptors = res + "/mbeans-descriptors.xml";
        URL dURL = classLoader.getResource ( descriptors );
        if ( dURL == null ) {
            return;
        }
        log.debug ( "Found " + dURL );
        searchedPaths.put ( packageName,  dURL );
        try {
            load ( "MbeansDescriptorsDigesterSource", dURL, null );
        } catch ( Exception ex ) {
            log.error ( "Error loading " + dURL );
        }
    }
    private void findDescriptor ( Class<?> beanClass, String type ) {
        if ( type == null ) {
            type = beanClass.getName();
        }
        ClassLoader classLoader = null;
        if ( beanClass != null ) {
            classLoader = beanClass.getClassLoader();
        }
        if ( classLoader == null ) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if ( classLoader == null ) {
            classLoader = this.getClass().getClassLoader();
        }
        String className = type;
        String pkg = className;
        while ( pkg.indexOf ( "." ) > 0 ) {
            int lastComp = pkg.lastIndexOf ( "." );
            if ( lastComp <= 0 ) {
                return;
            }
            pkg = pkg.substring ( 0, lastComp );
            if ( searchedPaths.get ( pkg ) != null ) {
                return;
            }
            loadDescriptors ( pkg, classLoader );
        }
        return;
    }
    private ModelerSource getModelerSource ( String type )
    throws Exception {
        if ( type == null ) {
            type = "MbeansDescriptorsDigesterSource";
        }
        if ( type.indexOf ( "." ) < 0 ) {
            type = "org.apache.tomcat.util.modeler.modules." + type;
        }
        Class<?> c = Class.forName ( type );
        ModelerSource ds = ( ModelerSource ) c.newInstance();
        return ds;
    }
    @Override
    public ObjectName preRegister ( MBeanServer server,
                                    ObjectName name ) throws Exception {
        this.server = server;
        return name;
    }
    @Override
    public void postRegister ( Boolean registrationDone ) {
    }
    @Override
    public void preDeregister() throws Exception {
    }
    @Override
    public void postDeregister() {
    }
}

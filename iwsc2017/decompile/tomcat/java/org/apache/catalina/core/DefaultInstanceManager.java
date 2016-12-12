package org.apache.catalina.core;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import javax.annotation.PostConstruct;
import java.security.PrivilegedAction;
import org.apache.catalina.Globals;
import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.ContainerServlet;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import org.apache.catalina.security.SecurityUtil;
import java.lang.reflect.Field;
import java.util.List;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceContext;
import javax.xml.ws.WebServiceRef;
import javax.ejb.EJB;
import javax.annotation.Resource;
import org.apache.catalina.util.Introspection;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import org.apache.juli.logging.Log;
import java.util.Collections;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.Map;
import javax.naming.Context;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.InstanceManager;
public class DefaultInstanceManager implements InstanceManager {
    private static final AnnotationCacheEntry[] ANNOTATIONS_EMPTY;
    protected static final StringManager sm;
    private final Context context;
    private final Map<String, Map<String, String>> injectionMap;
    protected final ClassLoader classLoader;
    protected final ClassLoader containerClassLoader;
    protected final boolean privileged;
    protected final boolean ignoreAnnotations;
    private final Set<String> restrictedClasses;
    private final Map<Class<?>, AnnotationCacheEntry[]> annotationCache;
    private final Map<String, String> postConstructMethods;
    private final Map<String, String> preDestroyMethods;
    public DefaultInstanceManager ( final Context context, final Map<String, Map<String, String>> injectionMap, final org.apache.catalina.Context catalinaContext, final ClassLoader containerClassLoader ) {
        this.annotationCache = new WeakHashMap<Class<?>, AnnotationCacheEntry[]>();
        this.classLoader = catalinaContext.getLoader().getClassLoader();
        this.privileged = catalinaContext.getPrivileged();
        this.containerClassLoader = containerClassLoader;
        this.ignoreAnnotations = catalinaContext.getIgnoreAnnotations();
        final Log log = catalinaContext.getLogger();
        final Set<String> classNames = new HashSet<String>();
        loadProperties ( classNames, "org/apache/catalina/core/RestrictedServlets.properties", "defaultInstanceManager.restrictedServletsResource", log );
        loadProperties ( classNames, "org/apache/catalina/core/RestrictedListeners.properties", "defaultInstanceManager.restrictedListenersResource", log );
        loadProperties ( classNames, "org/apache/catalina/core/RestrictedFilters.properties", "defaultInstanceManager.restrictedFiltersResource", log );
        this.restrictedClasses = Collections.unmodifiableSet ( ( Set<? extends String> ) classNames );
        this.context = context;
        this.injectionMap = injectionMap;
        this.postConstructMethods = catalinaContext.findPostConstructMethods();
        this.preDestroyMethods = catalinaContext.findPreDestroyMethods();
    }
    @Override
    public Object newInstance ( final Class<?> clazz ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
        return this.newInstance ( clazz.newInstance(), clazz );
    }
    @Override
    public Object newInstance ( final String className ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        final Class<?> clazz = this.loadClassMaybePrivileged ( className, this.classLoader );
        return this.newInstance ( clazz.newInstance(), clazz );
    }
    @Override
    public Object newInstance ( final String className, final ClassLoader classLoader ) throws IllegalAccessException, NamingException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        final Class<?> clazz = classLoader.loadClass ( className );
        return this.newInstance ( clazz.newInstance(), clazz );
    }
    @Override
    public void newInstance ( final Object o ) throws IllegalAccessException, InvocationTargetException, NamingException {
        this.newInstance ( o, o.getClass() );
    }
    private Object newInstance ( final Object instance, final Class<?> clazz ) throws IllegalAccessException, InvocationTargetException, NamingException {
        if ( !this.ignoreAnnotations ) {
            final Map<String, String> injections = this.assembleInjectionsFromClassHierarchy ( clazz );
            this.populateAnnotationsCache ( clazz, injections );
            this.processAnnotations ( instance, injections );
            this.postConstruct ( instance, clazz );
        }
        return instance;
    }
    private Map<String, String> assembleInjectionsFromClassHierarchy ( Class<?> clazz ) {
        final Map<String, String> injections = new HashMap<String, String>();
        Map<String, String> currentInjections = null;
        while ( clazz != null ) {
            currentInjections = this.injectionMap.get ( clazz.getName() );
            if ( currentInjections != null ) {
                injections.putAll ( currentInjections );
            }
            clazz = clazz.getSuperclass();
        }
        return injections;
    }
    @Override
    public void destroyInstance ( final Object instance ) throws IllegalAccessException, InvocationTargetException {
        if ( !this.ignoreAnnotations ) {
            this.preDestroy ( instance, instance.getClass() );
        }
    }
    protected void postConstruct ( final Object instance, final Class<?> clazz ) throws IllegalAccessException, InvocationTargetException {
        if ( this.context == null ) {
            return;
        }
        final Class<?> superClass = clazz.getSuperclass();
        if ( superClass != Object.class ) {
            this.postConstruct ( instance, superClass );
        }
        final AnnotationCacheEntry[] annotations;
        synchronized ( this.annotationCache ) {
            annotations = this.annotationCache.get ( clazz );
        }
        for ( final AnnotationCacheEntry entry : annotations ) {
            if ( entry.getType() == AnnotationCacheEntryType.POST_CONSTRUCT ) {
                final Method postConstruct = getMethod ( clazz, entry );
                synchronized ( postConstruct ) {
                    final boolean accessibility = postConstruct.isAccessible();
                    postConstruct.setAccessible ( true );
                    postConstruct.invoke ( instance, new Object[0] );
                    postConstruct.setAccessible ( accessibility );
                }
            }
        }
    }
    protected void preDestroy ( final Object instance, final Class<?> clazz ) throws IllegalAccessException, InvocationTargetException {
        final Class<?> superClass = clazz.getSuperclass();
        if ( superClass != Object.class ) {
            this.preDestroy ( instance, superClass );
        }
        AnnotationCacheEntry[] annotations = null;
        synchronized ( this.annotationCache ) {
            annotations = this.annotationCache.get ( clazz );
        }
        if ( annotations == null ) {
            return;
        }
        for ( final AnnotationCacheEntry entry : annotations ) {
            if ( entry.getType() == AnnotationCacheEntryType.PRE_DESTROY ) {
                final Method preDestroy = getMethod ( clazz, entry );
                synchronized ( preDestroy ) {
                    final boolean accessibility = preDestroy.isAccessible();
                    preDestroy.setAccessible ( true );
                    preDestroy.invoke ( instance, new Object[0] );
                    preDestroy.setAccessible ( accessibility );
                }
            }
        }
    }
    protected void populateAnnotationsCache ( Class<?> clazz, final Map<String, String> injections ) throws IllegalAccessException, InvocationTargetException, NamingException {
        List<AnnotationCacheEntry> annotations = null;
        while ( clazz != null ) {
            AnnotationCacheEntry[] annotationsArray = null;
            synchronized ( this.annotationCache ) {
                annotationsArray = this.annotationCache.get ( clazz );
            }
            if ( annotationsArray == null ) {
                if ( annotations == null ) {
                    annotations = new ArrayList<AnnotationCacheEntry>();
                } else {
                    annotations.clear();
                }
                if ( this.context != null ) {
                    final Field[] declaredFields;
                    final Field[] fields = declaredFields = Introspection.getDeclaredFields ( clazz );
                    for ( final Field field : declaredFields ) {
                        if ( injections != null && injections.containsKey ( field.getName() ) ) {
                            annotations.add ( new AnnotationCacheEntry ( field.getName(), null, injections.get ( field.getName() ), AnnotationCacheEntryType.FIELD ) );
                        } else {
                            final Resource resourceAnnotation;
                            if ( ( resourceAnnotation = field.getAnnotation ( Resource.class ) ) != null ) {
                                annotations.add ( new AnnotationCacheEntry ( field.getName(), null, resourceAnnotation.name(), AnnotationCacheEntryType.FIELD ) );
                            } else {
                                final EJB ejbAnnotation;
                                if ( ( ejbAnnotation = field.getAnnotation ( EJB.class ) ) != null ) {
                                    annotations.add ( new AnnotationCacheEntry ( field.getName(), null, ejbAnnotation.name(), AnnotationCacheEntryType.FIELD ) );
                                } else {
                                    final WebServiceRef webServiceRefAnnotation;
                                    if ( ( webServiceRefAnnotation = field.getAnnotation ( WebServiceRef.class ) ) != null ) {
                                        annotations.add ( new AnnotationCacheEntry ( field.getName(), null, webServiceRefAnnotation.name(), AnnotationCacheEntryType.FIELD ) );
                                    } else {
                                        final PersistenceContext persistenceContextAnnotation;
                                        if ( ( persistenceContextAnnotation = field.getAnnotation ( PersistenceContext.class ) ) != null ) {
                                            annotations.add ( new AnnotationCacheEntry ( field.getName(), null, persistenceContextAnnotation.name(), AnnotationCacheEntryType.FIELD ) );
                                        } else {
                                            final PersistenceUnit persistenceUnitAnnotation;
                                            if ( ( persistenceUnitAnnotation = field.getAnnotation ( PersistenceUnit.class ) ) != null ) {
                                                annotations.add ( new AnnotationCacheEntry ( field.getName(), null, persistenceUnitAnnotation.name(), AnnotationCacheEntryType.FIELD ) );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                final Method[] methods = Introspection.getDeclaredMethods ( clazz );
                Method postConstruct = null;
                final String postConstructFromXml = this.postConstructMethods.get ( clazz.getName() );
                Method preDestroy = null;
                final String preDestroyFromXml = this.preDestroyMethods.get ( clazz.getName() );
                for ( final Method method : methods ) {
                    Label_0853: {
                        if ( this.context != null ) {
                            if ( injections != null && Introspection.isValidSetter ( method ) ) {
                                final String fieldName = Introspection.getPropertyName ( method );
                                if ( injections.containsKey ( fieldName ) ) {
                                    annotations.add ( new AnnotationCacheEntry ( method.getName(), method.getParameterTypes(), injections.get ( fieldName ), AnnotationCacheEntryType.SETTER ) );
                                    break Label_0853;
                                }
                            }
                            final Resource resourceAnnotation2;
                            if ( ( resourceAnnotation2 = method.getAnnotation ( Resource.class ) ) != null ) {
                                annotations.add ( new AnnotationCacheEntry ( method.getName(), method.getParameterTypes(), resourceAnnotation2.name(), AnnotationCacheEntryType.SETTER ) );
                            } else {
                                final EJB ejbAnnotation2;
                                if ( ( ejbAnnotation2 = method.getAnnotation ( EJB.class ) ) != null ) {
                                    annotations.add ( new AnnotationCacheEntry ( method.getName(), method.getParameterTypes(), ejbAnnotation2.name(), AnnotationCacheEntryType.SETTER ) );
                                } else {
                                    final WebServiceRef webServiceRefAnnotation2;
                                    if ( ( webServiceRefAnnotation2 = method.getAnnotation ( WebServiceRef.class ) ) != null ) {
                                        annotations.add ( new AnnotationCacheEntry ( method.getName(), method.getParameterTypes(), webServiceRefAnnotation2.name(), AnnotationCacheEntryType.SETTER ) );
                                    } else {
                                        final PersistenceContext persistenceContextAnnotation2;
                                        if ( ( persistenceContextAnnotation2 = method.getAnnotation ( PersistenceContext.class ) ) != null ) {
                                            annotations.add ( new AnnotationCacheEntry ( method.getName(), method.getParameterTypes(), persistenceContextAnnotation2.name(), AnnotationCacheEntryType.SETTER ) );
                                        } else {
                                            final PersistenceUnit persistenceUnitAnnotation2;
                                            if ( ( persistenceUnitAnnotation2 = method.getAnnotation ( PersistenceUnit.class ) ) != null ) {
                                                annotations.add ( new AnnotationCacheEntry ( method.getName(), method.getParameterTypes(), persistenceUnitAnnotation2.name(), AnnotationCacheEntryType.SETTER ) );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        postConstruct = findPostConstruct ( postConstruct, postConstructFromXml, method );
                        preDestroy = findPreDestroy ( preDestroy, preDestroyFromXml, method );
                    }
                }
                if ( postConstruct != null ) {
                    annotations.add ( new AnnotationCacheEntry ( postConstruct.getName(), postConstruct.getParameterTypes(), null, AnnotationCacheEntryType.POST_CONSTRUCT ) );
                } else if ( postConstructFromXml != null ) {
                    throw new IllegalArgumentException ( "Post construct method " + postConstructFromXml + " for class " + clazz.getName() + " is declared in deployment descriptor but cannot be found." );
                }
                if ( preDestroy != null ) {
                    annotations.add ( new AnnotationCacheEntry ( preDestroy.getName(), preDestroy.getParameterTypes(), null, AnnotationCacheEntryType.PRE_DESTROY ) );
                } else if ( preDestroyFromXml != null ) {
                    throw new IllegalArgumentException ( "Pre destroy method " + preDestroyFromXml + " for class " + clazz.getName() + " is declared in deployment descriptor but cannot be found." );
                }
                if ( annotations.isEmpty() ) {
                    annotationsArray = DefaultInstanceManager.ANNOTATIONS_EMPTY;
                } else {
                    annotationsArray = annotations.toArray ( new AnnotationCacheEntry[annotations.size()] );
                }
                synchronized ( this.annotationCache ) {
                    this.annotationCache.put ( clazz, annotationsArray );
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
    protected void processAnnotations ( final Object instance, final Map<String, String> injections ) throws IllegalAccessException, InvocationTargetException, NamingException {
        if ( this.context == null ) {
            return;
        }
        for ( Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass() ) {
            final AnnotationCacheEntry[] annotations;
            synchronized ( this.annotationCache ) {
                annotations = this.annotationCache.get ( clazz );
            }
            for ( final AnnotationCacheEntry entry : annotations ) {
                if ( entry.getType() == AnnotationCacheEntryType.SETTER ) {
                    lookupMethodResource ( this.context, instance, getMethod ( clazz, entry ), entry.getName(), clazz );
                } else if ( entry.getType() == AnnotationCacheEntryType.FIELD ) {
                    lookupFieldResource ( this.context, instance, getField ( clazz, entry ), entry.getName(), clazz );
                }
            }
        }
    }
    protected int getAnnotationCacheSize() {
        synchronized ( this.annotationCache ) {
            return this.annotationCache.size();
        }
    }
    protected Class<?> loadClassMaybePrivileged ( final String className, final ClassLoader classLoader ) throws ClassNotFoundException {
        Class<?> clazz = null;
        Label_0066: {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    clazz = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Class<?>> ) new PrivilegedExceptionAction<Class<?>>() {
                        @Override
                        public Class<?> run() throws Exception {
                            return DefaultInstanceManager.this.loadClass ( className, classLoader );
                        }
                    } );
                    break Label_0066;
                } catch ( PrivilegedActionException e ) {
                    final Throwable t = e.getCause();
                    if ( t instanceof ClassNotFoundException ) {
                        throw ( ClassNotFoundException ) t;
                    }
                    throw new RuntimeException ( t );
                }
            }
            clazz = this.loadClass ( className, classLoader );
        }
        this.checkAccess ( clazz );
        return clazz;
    }
    protected Class<?> loadClass ( final String className, final ClassLoader classLoader ) throws ClassNotFoundException {
        if ( className.startsWith ( "org.apache.catalina" ) ) {
            return this.containerClassLoader.loadClass ( className );
        }
        try {
            final Class<?> clazz = this.containerClassLoader.loadClass ( className );
            if ( ContainerServlet.class.isAssignableFrom ( clazz ) ) {
                return clazz;
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        return classLoader.loadClass ( className );
    }
    private void checkAccess ( Class<?> clazz ) {
        if ( this.privileged ) {
            return;
        }
        if ( ContainerServlet.class.isAssignableFrom ( clazz ) ) {
            throw new SecurityException ( DefaultInstanceManager.sm.getString ( "defaultInstanceManager.restrictedContainerServlet", clazz ) );
        }
        while ( clazz != null ) {
            if ( this.restrictedClasses.contains ( clazz.getName() ) ) {
                throw new SecurityException ( DefaultInstanceManager.sm.getString ( "defaultInstanceManager.restrictedClass", clazz ) );
            }
            clazz = clazz.getSuperclass();
        }
    }
    protected static void lookupFieldResource ( final Context context, final Object instance, final Field field, final String name, final Class<?> clazz ) throws NamingException, IllegalAccessException {
        final String normalizedName = normalize ( name );
        Object lookedupResource;
        if ( normalizedName != null && normalizedName.length() > 0 ) {
            lookedupResource = context.lookup ( normalizedName );
        } else {
            lookedupResource = context.lookup ( clazz.getName() + "/" + field.getName() );
        }
        synchronized ( field ) {
            final boolean accessibility = field.isAccessible();
            field.setAccessible ( true );
            field.set ( instance, lookedupResource );
            field.setAccessible ( accessibility );
        }
    }
    protected static void lookupMethodResource ( final Context context, final Object instance, final Method method, final String name, final Class<?> clazz ) throws NamingException, IllegalAccessException, InvocationTargetException {
        if ( !Introspection.isValidSetter ( method ) ) {
            throw new IllegalArgumentException ( DefaultInstanceManager.sm.getString ( "defaultInstanceManager.invalidInjection" ) );
        }
        final String normalizedName = normalize ( name );
        Object lookedupResource;
        if ( normalizedName != null && normalizedName.length() > 0 ) {
            lookedupResource = context.lookup ( normalizedName );
        } else {
            lookedupResource = context.lookup ( clazz.getName() + "/" + Introspection.getPropertyName ( method ) );
        }
        synchronized ( method ) {
            final boolean accessibility = method.isAccessible();
            method.setAccessible ( true );
            method.invoke ( instance, lookedupResource );
            method.setAccessible ( accessibility );
        }
    }
    private static void loadProperties ( final Set<String> classNames, final String resourceName, final String messageKey, final Log log ) {
        final Properties properties = new Properties();
        final ClassLoader cl = DefaultInstanceManager.class.getClassLoader();
        try ( final InputStream is = cl.getResourceAsStream ( resourceName ) ) {
            if ( is == null ) {
                log.error ( DefaultInstanceManager.sm.getString ( messageKey, resourceName ) );
            } else {
                properties.load ( is );
            }
        } catch ( IOException ioe ) {
            log.error ( DefaultInstanceManager.sm.getString ( messageKey, resourceName ), ioe );
        }
        if ( properties.isEmpty() ) {
            return;
        }
        for ( final Map.Entry<Object, Object> e : properties.entrySet() ) {
            if ( "restricted".equals ( e.getValue() ) ) {
                classNames.add ( e.getKey().toString() );
            } else {
                log.warn ( DefaultInstanceManager.sm.getString ( "defaultInstanceManager.restrictedWrongValue", resourceName, e.getKey(), e.getValue() ) );
            }
        }
    }
    private static String normalize ( final String jndiName ) {
        if ( jndiName != null && jndiName.startsWith ( "java:comp/env/" ) ) {
            return jndiName.substring ( 14 );
        }
        return jndiName;
    }
    private static Method getMethod ( final Class<?> clazz, final AnnotationCacheEntry entry ) {
        Method result = null;
        if ( Globals.IS_SECURITY_ENABLED ) {
            result = AccessController.doPrivileged ( ( PrivilegedAction<Method> ) new PrivilegedAction<Method>() {
                @Override
                public Method run() {
                    Method result = null;
                    try {
                        result = clazz.getDeclaredMethod ( entry.getAccessibleObjectName(), ( Class[] ) entry.getParamTypes() );
                    } catch ( NoSuchMethodException ex ) {}
                    return result;
                }
            } );
        } else {
            try {
                result = clazz.getDeclaredMethod ( entry.getAccessibleObjectName(), entry.getParamTypes() );
            } catch ( NoSuchMethodException ex ) {}
        }
        return result;
    }
    private static Field getField ( final Class<?> clazz, final AnnotationCacheEntry entry ) {
        Field result = null;
        if ( Globals.IS_SECURITY_ENABLED ) {
            result = AccessController.doPrivileged ( ( PrivilegedAction<Field> ) new PrivilegedAction<Field>() {
                @Override
                public Field run() {
                    Field result = null;
                    try {
                        result = clazz.getDeclaredField ( entry.getAccessibleObjectName() );
                    } catch ( NoSuchFieldException ex ) {}
                    return result;
                }
            } );
        } else {
            try {
                result = clazz.getDeclaredField ( entry.getAccessibleObjectName() );
            } catch ( NoSuchFieldException ex ) {}
        }
        return result;
    }
    private static Method findPostConstruct ( final Method currentPostConstruct, final String postConstructFromXml, final Method method ) {
        return findLifecycleCallback ( currentPostConstruct, postConstructFromXml, method, PostConstruct.class );
    }
    private static Method findPreDestroy ( final Method currentPreDestroy, final String preDestroyFromXml, final Method method ) {
        return findLifecycleCallback ( currentPreDestroy, preDestroyFromXml, method, PreDestroy.class );
    }
    private static Method findLifecycleCallback ( final Method currentMethod, final String methodNameFromXml, final Method method, final Class<? extends Annotation> annotation ) {
        Method result = currentMethod;
        if ( methodNameFromXml != null ) {
            if ( method.getName().equals ( methodNameFromXml ) ) {
                if ( !Introspection.isValidLifecycleCallback ( method ) ) {
                    throw new IllegalArgumentException ( "Invalid " + annotation.getName() + " annotation" );
                }
                result = method;
            }
        } else if ( method.isAnnotationPresent ( annotation ) ) {
            if ( currentMethod != null || !Introspection.isValidLifecycleCallback ( method ) ) {
                throw new IllegalArgumentException ( "Invalid " + annotation.getName() + " annotation" );
            }
            result = method;
        }
        return result;
    }
    static {
        ANNOTATIONS_EMPTY = new AnnotationCacheEntry[0];
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
    private static final class AnnotationCacheEntry {
        private final String accessibleObjectName;
        private final Class<?>[] paramTypes;
        private final String name;
        private final AnnotationCacheEntryType type;
        public AnnotationCacheEntry ( final String accessibleObjectName, final Class<?>[] paramTypes, final String name, final AnnotationCacheEntryType type ) {
            this.accessibleObjectName = accessibleObjectName;
            this.paramTypes = paramTypes;
            this.name = name;
            this.type = type;
        }
        public String getAccessibleObjectName() {
            return this.accessibleObjectName;
        }
        public Class<?>[] getParamTypes() {
            return this.paramTypes;
        }
        public String getName() {
            return this.name;
        }
        public AnnotationCacheEntryType getType() {
            return this.type;
        }
    }
    private enum AnnotationCacheEntryType {
        FIELD,
        SETTER,
        POST_CONSTRUCT,
        PRE_DESTROY;
    }
}

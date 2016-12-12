package org.apache.catalina.security;
import org.apache.juli.logging.LogFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.catalina.Globals;
import javax.servlet.http.HttpSession;
import java.security.PrivilegedActionException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import org.apache.tomcat.util.ExceptionUtils;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import java.security.PrivilegedExceptionAction;
import javax.servlet.Filter;
import java.security.Principal;
import javax.servlet.Servlet;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import java.lang.reflect.Method;
import java.util.Map;
public final class SecurityUtil {
    private static final int INIT = 0;
    private static final int SERVICE = 1;
    private static final int DOFILTER = 1;
    private static final int EVENT = 2;
    private static final int DOFILTEREVENT = 2;
    private static final int DESTROY = 3;
    private static final String INIT_METHOD = "init";
    private static final String DOFILTER_METHOD = "doFilter";
    private static final String SERVICE_METHOD = "service";
    private static final String EVENT_METHOD = "event";
    private static final String DOFILTEREVENT_METHOD = "doFilterEvent";
    private static final String DESTROY_METHOD = "destroy";
    private static final Map<Class<?>, Method[]> classCache;
    private static final Log log;
    private static final boolean packageDefinitionEnabled;
    private static final StringManager sm;
    public static void doAsPrivilege ( final String methodName, final Servlet targetObject ) throws Exception {
        doAsPrivilege ( methodName, targetObject, null, null, null );
    }
    public static void doAsPrivilege ( final String methodName, final Servlet targetObject, final Class<?>[] targetType, final Object[] targetArguments ) throws Exception {
        doAsPrivilege ( methodName, targetObject, targetType, targetArguments, null );
    }
    public static void doAsPrivilege ( final String methodName, final Servlet targetObject, final Class<?>[] targetParameterTypes, final Object[] targetArguments, final Principal principal ) throws Exception {
        Method method = null;
        final Method[] methodsCache = SecurityUtil.classCache.get ( Servlet.class );
        if ( methodsCache == null ) {
            method = createMethodAndCacheIt ( methodsCache, Servlet.class, methodName, targetParameterTypes );
        } else {
            method = findMethod ( methodsCache, methodName );
            if ( method == null ) {
                method = createMethodAndCacheIt ( methodsCache, Servlet.class, methodName, targetParameterTypes );
            }
        }
        execute ( method, targetObject, targetArguments, principal );
    }
    public static void doAsPrivilege ( final String methodName, final Filter targetObject ) throws Exception {
        doAsPrivilege ( methodName, targetObject, null, null );
    }
    public static void doAsPrivilege ( final String methodName, final Filter targetObject, final Class<?>[] targetType, final Object[] targetArguments ) throws Exception {
        doAsPrivilege ( methodName, targetObject, targetType, targetArguments, null );
    }
    public static void doAsPrivilege ( final String methodName, final Filter targetObject, final Class<?>[] targetParameterTypes, final Object[] targetParameterValues, final Principal principal ) throws Exception {
        Method method = null;
        final Method[] methodsCache = SecurityUtil.classCache.get ( Filter.class );
        if ( methodsCache == null ) {
            method = createMethodAndCacheIt ( methodsCache, Filter.class, methodName, targetParameterTypes );
        } else {
            method = findMethod ( methodsCache, methodName );
            if ( method == null ) {
                method = createMethodAndCacheIt ( methodsCache, Filter.class, methodName, targetParameterTypes );
            }
        }
        execute ( method, targetObject, targetParameterValues, principal );
    }
    private static void execute ( final Method method, final Object targetObject, final Object[] targetArguments, final Principal principal ) throws Exception {
        try {
            Subject subject = null;
            final PrivilegedExceptionAction<Void> pea = new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    method.invoke ( targetObject, targetArguments );
                    return null;
                }
            };
            if ( targetArguments != null && targetArguments[0] instanceof HttpServletRequest ) {
                final HttpServletRequest request = ( HttpServletRequest ) targetArguments[0];
                boolean hasSubject = false;
                final HttpSession session = request.getSession ( false );
                if ( session != null ) {
                    subject = ( Subject ) session.getAttribute ( "javax.security.auth.subject" );
                    hasSubject = ( subject != null );
                }
                if ( subject == null ) {
                    subject = new Subject();
                    if ( principal != null ) {
                        subject.getPrincipals().add ( principal );
                    }
                }
                if ( session != null && !hasSubject ) {
                    session.setAttribute ( "javax.security.auth.subject", ( Object ) subject );
                }
            }
            Subject.doAsPrivileged ( subject, pea, null );
        } catch ( PrivilegedActionException pe ) {
            Throwable e;
            if ( pe.getException() instanceof InvocationTargetException ) {
                e = pe.getException().getCause();
                ExceptionUtils.handleThrowable ( e );
            } else {
                e = pe;
            }
            if ( SecurityUtil.log.isDebugEnabled() ) {
                SecurityUtil.log.debug ( SecurityUtil.sm.getString ( "SecurityUtil.doAsPrivilege" ), e );
            }
            if ( e instanceof UnavailableException ) {
                throw ( UnavailableException ) e;
            }
            if ( e instanceof ServletException ) {
                throw ( ServletException ) e;
            }
            if ( e instanceof IOException ) {
                throw ( IOException ) e;
            }
            if ( e instanceof RuntimeException ) {
                throw ( RuntimeException ) e;
            }
            throw new ServletException ( e.getMessage(), e );
        }
    }
    private static Method findMethod ( final Method[] methodsCache, final String methodName ) {
        if ( methodName.equals ( "init" ) ) {
            return methodsCache[0];
        }
        if ( methodName.equals ( "destroy" ) ) {
            return methodsCache[3];
        }
        if ( methodName.equals ( "service" ) ) {
            return methodsCache[1];
        }
        if ( methodName.equals ( "doFilter" ) ) {
            return methodsCache[1];
        }
        if ( methodName.equals ( "event" ) ) {
            return methodsCache[2];
        }
        if ( methodName.equals ( "doFilterEvent" ) ) {
            return methodsCache[2];
        }
        return null;
    }
    private static Method createMethodAndCacheIt ( Method[] methodsCache, final Class<?> targetType, final String methodName, final Class<?>[] parameterTypes ) throws Exception {
        if ( methodsCache == null ) {
            methodsCache = new Method[4];
        }
        final Method method = targetType.getMethod ( methodName, parameterTypes );
        if ( methodName.equals ( "init" ) ) {
            methodsCache[0] = method;
        } else if ( methodName.equals ( "destroy" ) ) {
            methodsCache[3] = method;
        } else if ( methodName.equals ( "service" ) ) {
            methodsCache[1] = method;
        } else if ( methodName.equals ( "doFilter" ) ) {
            methodsCache[1] = method;
        } else if ( methodName.equals ( "event" ) ) {
            methodsCache[2] = method;
        } else if ( methodName.equals ( "doFilterEvent" ) ) {
            methodsCache[2] = method;
        }
        SecurityUtil.classCache.put ( targetType, methodsCache );
        return method;
    }
    public static void remove ( final Object cachedObject ) {
        SecurityUtil.classCache.remove ( cachedObject );
    }
    public static boolean isPackageProtectionEnabled() {
        return SecurityUtil.packageDefinitionEnabled && Globals.IS_SECURITY_ENABLED;
    }
    static {
        classCache = new ConcurrentHashMap<Class<?>, Method[]>();
        log = LogFactory.getLog ( SecurityUtil.class );
        packageDefinitionEnabled = ( System.getProperty ( "package.definition" ) != null || System.getProperty ( "package.access" ) != null );
        sm = StringManager.getManager ( "org.apache.catalina.security" );
    }
}

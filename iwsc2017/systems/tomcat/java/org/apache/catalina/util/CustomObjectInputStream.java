package org.apache.catalina.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
public final class CustomObjectInputStream extends ObjectInputStream {
    private static final StringManager sm = StringManager.getManager ( CustomObjectInputStream.class );
    private static final WeakHashMap<ClassLoader, Set<String>> reportedClassCache =
        new WeakHashMap<>();
    private final ClassLoader classLoader;
    private final Set<String> reportedClasses;
    private final Log log;
    private final Pattern allowedClassNamePattern;
    private final String allowedClassNameFilter;
    private final boolean warnOnFailure;
    public CustomObjectInputStream ( InputStream stream, ClassLoader classLoader ) throws IOException {
        this ( stream, classLoader, null, null, false );
    }
    public CustomObjectInputStream ( InputStream stream, ClassLoader classLoader,
                                     Log log, Pattern allowedClassNamePattern, boolean warnOnFailure )
    throws IOException {
        super ( stream );
        if ( log == null && allowedClassNamePattern != null && warnOnFailure ) {
            throw new IllegalArgumentException (
                sm.getString ( "customObjectInputStream.logRequired" ) );
        }
        this.classLoader = classLoader;
        this.log = log;
        this.allowedClassNamePattern = allowedClassNamePattern;
        if ( allowedClassNamePattern == null ) {
            this.allowedClassNameFilter = null;
        } else {
            this.allowedClassNameFilter = allowedClassNamePattern.toString();
        }
        this.warnOnFailure = warnOnFailure;
        Set<String> reportedClasses;
        synchronized ( reportedClassCache ) {
            reportedClasses = reportedClassCache.get ( classLoader );
        }
        if ( reportedClasses == null ) {
            reportedClasses = Collections.newSetFromMap ( new ConcurrentHashMap<>() );
            Set<String> original;
            synchronized ( reportedClassCache ) {
                original = reportedClassCache.putIfAbsent ( classLoader, reportedClasses );
            }
            if ( original != null ) {
                reportedClasses = original;
            }
        }
        this.reportedClasses = reportedClasses;
    }
    @Override
    public Class<?> resolveClass ( ObjectStreamClass classDesc )
    throws ClassNotFoundException, IOException {
        String name = classDesc.getName();
        if ( allowedClassNamePattern != null ) {
            boolean allowed = allowedClassNamePattern.matcher ( name ).matches();
            if ( !allowed ) {
                boolean doLog = warnOnFailure && reportedClasses.add ( name );
                String msg = sm.getString ( "customObjectInputStream.nomatch", name, allowedClassNameFilter );
                if ( doLog ) {
                    log.warn ( msg );
                } else if ( log.isDebugEnabled() ) {
                    log.debug ( msg );
                }
                throw new InvalidClassException ( msg );
            }
        }
        try {
            return Class.forName ( name, false, classLoader );
        } catch ( ClassNotFoundException e ) {
            try {
                return super.resolveClass ( classDesc );
            } catch ( ClassNotFoundException e2 ) {
                throw e;
            }
        }
    }
    @Override
    protected Class<?> resolveProxyClass ( String[] interfaces )
    throws IOException, ClassNotFoundException {
        Class<?>[] cinterfaces = new Class[interfaces.length];
        for ( int i = 0; i < interfaces.length; i++ ) {
            cinterfaces[i] = classLoader.loadClass ( interfaces[i] );
        }
        try {
            return Proxy.getProxyClass ( classLoader, cinterfaces );
        } catch ( IllegalArgumentException e ) {
            throw new ClassNotFoundException ( null, e );
        }
    }
}

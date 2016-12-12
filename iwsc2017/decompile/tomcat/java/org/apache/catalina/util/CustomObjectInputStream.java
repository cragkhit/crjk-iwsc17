package org.apache.catalina.util;
import java.lang.reflect.Proxy;
import java.io.InvalidClassException;
import java.io.ObjectStreamClass;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.apache.juli.logging.Log;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.tomcat.util.res.StringManager;
import java.io.ObjectInputStream;
public final class CustomObjectInputStream extends ObjectInputStream {
    private static final StringManager sm;
    private static final WeakHashMap<ClassLoader, Set<String>> reportedClassCache;
    private final ClassLoader classLoader;
    private final Set<String> reportedClasses;
    private final Log log;
    private final Pattern allowedClassNamePattern;
    private final String allowedClassNameFilter;
    private final boolean warnOnFailure;
    public CustomObjectInputStream ( final InputStream stream, final ClassLoader classLoader ) throws IOException {
        this ( stream, classLoader, null, null, false );
    }
    public CustomObjectInputStream ( final InputStream stream, final ClassLoader classLoader, final Log log, final Pattern allowedClassNamePattern, final boolean warnOnFailure ) throws IOException {
        super ( stream );
        if ( log == null && allowedClassNamePattern != null && warnOnFailure ) {
            throw new IllegalArgumentException ( CustomObjectInputStream.sm.getString ( "customObjectInputStream.logRequired" ) );
        }
        this.classLoader = classLoader;
        this.log = log;
        if ( ( this.allowedClassNamePattern = allowedClassNamePattern ) == null ) {
            this.allowedClassNameFilter = null;
        } else {
            this.allowedClassNameFilter = allowedClassNamePattern.toString();
        }
        this.warnOnFailure = warnOnFailure;
        Set<String> reportedClasses;
        synchronized ( CustomObjectInputStream.reportedClassCache ) {
            reportedClasses = CustomObjectInputStream.reportedClassCache.get ( classLoader );
        }
        if ( reportedClasses == null ) {
            reportedClasses = Collections.newSetFromMap ( new ConcurrentHashMap<String, Boolean>() );
            final Set<String> original;
            synchronized ( CustomObjectInputStream.reportedClassCache ) {
                original = CustomObjectInputStream.reportedClassCache.putIfAbsent ( classLoader, reportedClasses );
            }
            if ( original != null ) {
                reportedClasses = original;
            }
        }
        this.reportedClasses = reportedClasses;
    }
    public Class<?> resolveClass ( final ObjectStreamClass classDesc ) throws ClassNotFoundException, IOException {
        final String name = classDesc.getName();
        if ( this.allowedClassNamePattern != null ) {
            final boolean allowed = this.allowedClassNamePattern.matcher ( name ).matches();
            if ( !allowed ) {
                final boolean doLog = this.warnOnFailure && this.reportedClasses.add ( name );
                final String msg = CustomObjectInputStream.sm.getString ( "customObjectInputStream.nomatch", name, this.allowedClassNameFilter );
                if ( doLog ) {
                    this.log.warn ( msg );
                } else if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( msg );
                }
                throw new InvalidClassException ( msg );
            }
        }
        try {
            return Class.forName ( name, false, this.classLoader );
        } catch ( ClassNotFoundException e ) {
            try {
                return super.resolveClass ( classDesc );
            } catch ( ClassNotFoundException e2 ) {
                throw e;
            }
        }
    }
    @Override
    protected Class<?> resolveProxyClass ( final String[] interfaces ) throws IOException, ClassNotFoundException {
        final Class<?>[] cinterfaces = ( Class<?>[] ) new Class[interfaces.length];
        for ( int i = 0; i < interfaces.length; ++i ) {
            cinterfaces[i] = this.classLoader.loadClass ( interfaces[i] );
        }
        try {
            return Proxy.getProxyClass ( this.classLoader, cinterfaces );
        } catch ( IllegalArgumentException e ) {
            throw new ClassNotFoundException ( null, e );
        }
    }
    static {
        sm = StringManager.getManager ( CustomObjectInputStream.class );
        reportedClassCache = new WeakHashMap<ClassLoader, Set<String>>();
    }
}

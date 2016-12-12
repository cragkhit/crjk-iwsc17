package org.apache.catalina.tribes.io;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import org.apache.catalina.tribes.util.StringManager;
public final class ReplicationStream extends ObjectInputStream {
    static final StringManager sm = StringManager.getManager ( ReplicationStream.class );
    private ClassLoader[] classLoaders = null;
    public ReplicationStream ( InputStream stream,
                               ClassLoader[] classLoaders )
    throws IOException {
        super ( stream );
        this.classLoaders = classLoaders;
    }
    @Override
    public Class<?> resolveClass ( ObjectStreamClass classDesc )
    throws ClassNotFoundException, IOException {
        String name = classDesc.getName();
        try {
            return resolveClass ( name );
        } catch ( ClassNotFoundException e ) {
            return super.resolveClass ( classDesc );
        }
    }
    public Class<?> resolveClass ( String name ) throws ClassNotFoundException {
        boolean tryRepFirst = name.startsWith ( "org.apache.catalina.tribes" );
        try {
            if ( tryRepFirst ) {
                return findReplicationClass ( name );
            } else {
                return findExternalClass ( name );
            }
        } catch ( Exception x ) {
            if ( tryRepFirst ) {
                return findExternalClass ( name );
            } else {
                return findReplicationClass ( name );
            }
        }
    }
    @Override
    protected Class<?> resolveProxyClass ( String[] interfaces )
    throws IOException, ClassNotFoundException {
        ClassLoader latestLoader;
        if ( classLoaders != null && classLoaders.length > 0 ) {
            latestLoader = classLoaders[0];
        } else {
            latestLoader = null;
        }
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;
        Class<?>[] classObjs = new Class[interfaces.length];
        for ( int i = 0; i < interfaces.length; i++ ) {
            Class<?> cl = this.resolveClass ( interfaces[i] );
            if ( latestLoader == null ) {
                latestLoader = cl.getClassLoader();
            }
            if ( ( cl.getModifiers() & Modifier.PUBLIC ) == 0 ) {
                if ( hasNonPublicInterface ) {
                    if ( nonPublicLoader != cl.getClassLoader() ) {
                        throw new IllegalAccessError (
                            sm.getString ( "replicationStream.conflict" ) );
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
            return Proxy.getProxyClass ( hasNonPublicInterface ? nonPublicLoader
                                         : latestLoader, classObjs );
        } catch ( IllegalArgumentException e ) {
            throw new ClassNotFoundException ( null, e );
        }
    }
    public Class<?> findReplicationClass ( String name )
    throws ClassNotFoundException {
        Class<?> clazz = Class.forName ( name, false, getClass().getClassLoader() );
        return clazz;
    }
    public Class<?> findExternalClass ( String name ) throws ClassNotFoundException  {
        ClassNotFoundException cnfe = null;
        for ( int i = 0; i < classLoaders.length; i++ ) {
            try {
                Class<?> clazz = Class.forName ( name, false, classLoaders[i] );
                return clazz;
            } catch ( ClassNotFoundException x ) {
                cnfe = x;
            }
        }
        if ( cnfe != null ) {
            throw cnfe;
        } else {
            throw new ClassNotFoundException ( name );
        }
    }
    @Override
    public void close() throws IOException  {
        this.classLoaders = null;
        super.close();
    }
}

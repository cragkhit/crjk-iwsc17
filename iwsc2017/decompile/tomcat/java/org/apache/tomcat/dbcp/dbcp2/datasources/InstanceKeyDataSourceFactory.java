package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.io.IOException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import java.util.Iterator;
import java.util.Map;
import javax.naming.spi.ObjectFactory;
abstract class InstanceKeyDataSourceFactory implements ObjectFactory {
    private static final Map<String, InstanceKeyDataSource> instanceMap;
    static synchronized String registerNewInstance ( final InstanceKeyDataSource ds ) {
        int max = 0;
        for ( final String s : InstanceKeyDataSourceFactory.instanceMap.keySet() ) {
            if ( s != null ) {
                try {
                    max = Math.max ( max, Integer.parseInt ( s ) );
                } catch ( NumberFormatException ex ) {}
            }
        }
        final String instanceKey = String.valueOf ( max + 1 );
        InstanceKeyDataSourceFactory.instanceMap.put ( instanceKey, ds );
        return instanceKey;
    }
    static void removeInstance ( final String key ) {
        if ( key != null ) {
            InstanceKeyDataSourceFactory.instanceMap.remove ( key );
        }
    }
    public static void closeAll() throws Exception {
        final Iterator<Map.Entry<String, InstanceKeyDataSource>> instanceIterator = InstanceKeyDataSourceFactory.instanceMap.entrySet().iterator();
        while ( instanceIterator.hasNext() ) {
            instanceIterator.next().getValue().close();
        }
        InstanceKeyDataSourceFactory.instanceMap.clear();
    }
    @Override
    public Object getObjectInstance ( final Object refObj, final Name name, final Context context, final Hashtable<?, ?> env ) throws IOException, ClassNotFoundException {
        Object obj = null;
        if ( refObj instanceof Reference ) {
            final Reference ref = ( Reference ) refObj;
            if ( this.isCorrectClass ( ref.getClassName() ) ) {
                final RefAddr ra = ref.get ( "instanceKey" );
                if ( ra != null && ra.getContent() != null ) {
                    obj = InstanceKeyDataSourceFactory.instanceMap.get ( ra.getContent() );
                } else {
                    String key = null;
                    if ( name != null ) {
                        key = name.toString();
                        obj = InstanceKeyDataSourceFactory.instanceMap.get ( key );
                    }
                    if ( obj == null ) {
                        final InstanceKeyDataSource ds = this.getNewInstance ( ref );
                        this.setCommonProperties ( ref, ds );
                        obj = ds;
                        if ( key != null ) {
                            InstanceKeyDataSourceFactory.instanceMap.put ( key, ds );
                        }
                    }
                }
            }
        }
        return obj;
    }
    private void setCommonProperties ( final Reference ref, final InstanceKeyDataSource ikds ) throws IOException, ClassNotFoundException {
        RefAddr ra = ref.get ( "dataSourceName" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDataSourceName ( ra.getContent().toString() );
        }
        ra = ref.get ( "description" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDescription ( ra.getContent().toString() );
        }
        ra = ref.get ( "jndiEnvironment" );
        if ( ra != null && ra.getContent() != null ) {
            final byte[] serialized = ( byte[] ) ra.getContent();
            ikds.setJndiEnvironment ( ( Properties ) deserialize ( serialized ) );
        }
        ra = ref.get ( "loginTimeout" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setLoginTimeout ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "blockWhenExhausted" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultBlockWhenExhausted ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "evictionPolicyClassName" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultEvictionPolicyClassName ( ra.getContent().toString() );
        }
        ra = ref.get ( "lifo" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultLifo ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "maxIdlePerKey" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultMaxIdle ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "maxTotalPerKey" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultMaxTotal ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "maxWaitMillis" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultMaxWaitMillis ( Long.parseLong ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "minEvictableIdleTimeMillis" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultMinEvictableIdleTimeMillis ( Long.parseLong ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "minIdlePerKey" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultMinIdle ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "numTestsPerEvictionRun" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultNumTestsPerEvictionRun ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "softMinEvictableIdleTimeMillis" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultSoftMinEvictableIdleTimeMillis ( Long.parseLong ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "testOnCreate" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultTestOnCreate ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "testOnBorrow" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultTestOnBorrow ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "testOnReturn" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultTestOnReturn ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "testWhileIdle" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultTestWhileIdle ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "timeBetweenEvictionRunsMillis" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultTimeBetweenEvictionRunsMillis ( Long.parseLong ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "validationQuery" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setValidationQuery ( ra.getContent().toString() );
        }
        ra = ref.get ( "validationQueryTimeout" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setValidationQueryTimeout ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "rollbackAfterValidation" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setRollbackAfterValidation ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "maxConnLifetimeMillis" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setMaxConnLifetimeMillis ( Long.parseLong ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "defaultAutoCommit" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultAutoCommit ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "defaultTransactionIsolation" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultTransactionIsolation ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        ra = ref.get ( "defaultReadOnly" );
        if ( ra != null && ra.getContent() != null ) {
            ikds.setDefaultReadOnly ( Boolean.valueOf ( ra.getContent().toString() ) );
        }
    }
    protected abstract boolean isCorrectClass ( final String p0 );
    protected abstract InstanceKeyDataSource getNewInstance ( final Reference p0 ) throws IOException, ClassNotFoundException;
    protected static final Object deserialize ( final byte[] data ) throws IOException, ClassNotFoundException {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream ( new ByteArrayInputStream ( data ) );
            return in.readObject();
        } finally {
            if ( in != null ) {
                try {
                    in.close();
                } catch ( IOException ex ) {}
            }
        }
    }
    static {
        instanceMap = new ConcurrentHashMap<String, InstanceKeyDataSource>();
    }
}

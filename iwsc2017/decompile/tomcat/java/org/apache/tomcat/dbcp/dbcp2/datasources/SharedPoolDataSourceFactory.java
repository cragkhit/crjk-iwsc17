package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.io.IOException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
public class SharedPoolDataSourceFactory extends InstanceKeyDataSourceFactory {
    private static final String SHARED_POOL_CLASSNAME;
    @Override
    protected boolean isCorrectClass ( final String className ) {
        return SharedPoolDataSourceFactory.SHARED_POOL_CLASSNAME.equals ( className );
    }
    @Override
    protected InstanceKeyDataSource getNewInstance ( final Reference ref ) {
        final SharedPoolDataSource spds = new SharedPoolDataSource();
        final RefAddr ra = ref.get ( "maxTotal" );
        if ( ra != null && ra.getContent() != null ) {
            spds.setMaxTotal ( Integer.parseInt ( ra.getContent().toString() ) );
        }
        return spds;
    }
    static {
        SHARED_POOL_CLASSNAME = SharedPoolDataSource.class.getName();
    }
}

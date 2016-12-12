package org.apache.catalina.ha.backend;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import org.apache.tomcat.util.modeler.Registry;
public class CollectedInfo {
    protected MBeanServer mBeanServer = null;
    protected ObjectName objName = null;
    int ready;
    int busy;
    int port = 0;
    String host = null;
    public CollectedInfo ( String host, int port ) throws Exception {
        init ( host, port );
    }
    public void init ( String host, int port ) throws Exception {
        int iport = 0;
        String shost = null;
        mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
        String onStr = "*:type=ThreadPool,*";
        ObjectName objectName = new ObjectName ( onStr );
        Set<ObjectInstance> set = mBeanServer.queryMBeans ( objectName, null );
        Iterator<ObjectInstance> iterator = set.iterator();
        while ( iterator.hasNext() ) {
            ObjectInstance oi = iterator.next();
            objName = oi.getObjectName();
            String name = objName.getKeyProperty ( "name" );
            String [] elenames = name.split ( "-" );
            String sport = elenames[elenames.length - 1];
            iport = Integer.parseInt ( sport );
            String [] shosts = elenames[1].split ( "%2F" );
            shost = shosts[0];
            if ( port == 0 && host == null ) {
                break;
            }
            if ( host == null && iport == port ) {
                break;
            }
            if ( shost.compareTo ( host ) == 0 ) {
                break;
            }
        }
        if ( objName == null ) {
            throw ( new Exception ( "Can't find connector for " + host + ":" + port ) );
        }
        this.port = iport;
        this.host = shost;
    }
    public void refresh() throws Exception {
        if ( mBeanServer == null || objName == null ) {
            throw ( new Exception ( "Not initialized!!!" ) );
        }
        Integer imax = ( Integer ) mBeanServer.getAttribute ( objName, "maxThreads" );
        Integer ibusy  = ( Integer ) mBeanServer.getAttribute ( objName, "currentThreadsBusy" );
        busy = ibusy.intValue();
        ready = imax.intValue() - ibusy.intValue();
    }
}

package org.apache.catalina.storeconfig;
import javax.management.DynamicMBean;
import javax.management.ObjectName;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public class StoreConfigLifecycleListener implements LifecycleListener {
    private static Log log = LogFactory.getLog ( StoreConfigLifecycleListener.class );
    private static StringManager sm = StringManager.getManager ( StoreConfigLifecycleListener.class );
    protected final Registry registry = MBeanUtils.createRegistry();
    IStoreConfig storeConfig;
    private String storeConfigClass = "org.apache.catalina.storeconfig.StoreConfig";
    private String storeRegistry = null;
    private ObjectName oname = null;
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        if ( Lifecycle.AFTER_START_EVENT.equals ( event.getType() ) ) {
            if ( event.getSource() instanceof Server ) {
                createMBean ( ( Server ) event.getSource() );
            } else {
                log.warn ( sm.getString ( "storeConfigListener.notServer" ) );
            }
        } else if ( Lifecycle.AFTER_STOP_EVENT.equals ( event.getType() ) ) {
            if ( oname != null ) {
                registry.unregisterComponent ( oname );
                oname = null;
            }
        }
    }
    protected void createMBean ( Server server ) {
        StoreLoader loader = new StoreLoader();
        try {
            Class<?> clazz = Class.forName ( getStoreConfigClass(), true, this
                                             .getClass().getClassLoader() );
            storeConfig = ( IStoreConfig ) clazz.newInstance();
            if ( null == getStoreRegistry() ) {
                loader.load();
            } else {
                loader.load ( getStoreRegistry() );
            }
            storeConfig.setRegistry ( loader.getRegistry() );
            storeConfig.setServer ( server );
        } catch ( Exception e ) {
            log.error ( "createMBean load", e );
            return;
        }
        try {
            oname = new ObjectName ( "Catalina:type=StoreConfig" );
            registry.registerComponent ( storeConfig, oname, "StoreConfig" );
        } catch ( Exception ex ) {
            log.error ( "createMBean register MBean", ex );
        }
    }
    protected DynamicMBean getManagedBean ( Object object ) throws Exception {
        ManagedBean managedBean = registry.findManagedBean ( "StoreConfig" );
        return managedBean.createMBean ( object );
    }
    public IStoreConfig getStoreConfig() {
        return storeConfig;
    }
    public void setStoreConfig ( IStoreConfig storeConfig ) {
        this.storeConfig = storeConfig;
    }
    public String getStoreConfigClass() {
        return storeConfigClass;
    }
    public void setStoreConfigClass ( String storeConfigClass ) {
        this.storeConfigClass = storeConfigClass;
    }
    public String getStoreRegistry() {
        return storeRegistry;
    }
    public void setStoreRegistry ( String storeRegistry ) {
        this.storeRegistry = storeRegistry;
    }
}

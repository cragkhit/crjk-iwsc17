package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.ManagedBean;
import javax.management.DynamicMBean;
import org.apache.catalina.Server;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.mbeans.MBeanUtils;
import javax.management.ObjectName;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public class StoreConfigLifecycleListener implements LifecycleListener {
    private static Log log;
    private static StringManager sm;
    protected final Registry registry;
    IStoreConfig storeConfig;
    private String storeConfigClass;
    private String storeRegistry;
    private ObjectName oname;
    public StoreConfigLifecycleListener() {
        this.registry = MBeanUtils.createRegistry();
        this.storeConfigClass = "org.apache.catalina.storeconfig.StoreConfig";
        this.storeRegistry = null;
        this.oname = null;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "after_start".equals ( event.getType() ) ) {
            if ( event.getSource() instanceof Server ) {
                this.createMBean ( ( Server ) event.getSource() );
            } else {
                StoreConfigLifecycleListener.log.warn ( StoreConfigLifecycleListener.sm.getString ( "storeConfigListener.notServer" ) );
            }
        } else if ( "after_stop".equals ( event.getType() ) && this.oname != null ) {
            this.registry.unregisterComponent ( this.oname );
            this.oname = null;
        }
    }
    protected void createMBean ( final Server server ) {
        final StoreLoader loader = new StoreLoader();
        try {
            final Class<?> clazz = Class.forName ( this.getStoreConfigClass(), true, this.getClass().getClassLoader() );
            this.storeConfig = ( IStoreConfig ) clazz.newInstance();
            if ( null == this.getStoreRegistry() ) {
                loader.load();
            } else {
                loader.load ( this.getStoreRegistry() );
            }
            this.storeConfig.setRegistry ( loader.getRegistry() );
            this.storeConfig.setServer ( server );
        } catch ( Exception e ) {
            StoreConfigLifecycleListener.log.error ( "createMBean load", e );
            return;
        }
        try {
            this.oname = new ObjectName ( "Catalina:type=StoreConfig" );
            this.registry.registerComponent ( this.storeConfig, this.oname, "StoreConfig" );
        } catch ( Exception ex ) {
            StoreConfigLifecycleListener.log.error ( "createMBean register MBean", ex );
        }
    }
    protected DynamicMBean getManagedBean ( final Object object ) throws Exception {
        final ManagedBean managedBean = this.registry.findManagedBean ( "StoreConfig" );
        return managedBean.createMBean ( object );
    }
    public IStoreConfig getStoreConfig() {
        return this.storeConfig;
    }
    public void setStoreConfig ( final IStoreConfig storeConfig ) {
        this.storeConfig = storeConfig;
    }
    public String getStoreConfigClass() {
        return this.storeConfigClass;
    }
    public void setStoreConfigClass ( final String storeConfigClass ) {
        this.storeConfigClass = storeConfigClass;
    }
    public String getStoreRegistry() {
        return this.storeRegistry;
    }
    public void setStoreRegistry ( final String storeRegistry ) {
        this.storeRegistry = storeRegistry;
    }
    static {
        StoreConfigLifecycleListener.log = LogFactory.getLog ( StoreConfigLifecycleListener.class );
        StoreConfigLifecycleListener.sm = StringManager.getManager ( StoreConfigLifecycleListener.class );
    }
}

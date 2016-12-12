package org.apache.catalina.storeconfig;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.coyote.UpgradeProtocol;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.transport.DataSender;
import org.apache.catalina.tribes.MessageListener;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.Valve;
import org.apache.catalina.LifecycleListener;
import javax.naming.directory.DirContext;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.ha.ClusterDeployer;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.juli.logging.LogFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.juli.logging.Log;
public class StoreRegistry {
    private static Log log;
    private Map<String, StoreDescription> descriptors;
    private String encoding;
    private String name;
    private String version;
    private static Class<?>[] interfaces;
    public StoreRegistry() {
        this.descriptors = new HashMap<String, StoreDescription>();
        this.encoding = "UTF-8";
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getVersion() {
        return this.version;
    }
    public void setVersion ( final String version ) {
        this.version = version;
    }
    public StoreDescription findDescription ( final String id ) {
        if ( StoreRegistry.log.isDebugEnabled() ) {
            StoreRegistry.log.debug ( "search descriptor " + id );
        }
        StoreDescription desc = this.descriptors.get ( id );
        if ( desc == null ) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName ( id, true, this.getClass().getClassLoader() );
            } catch ( ClassNotFoundException e ) {
                StoreRegistry.log.error ( "ClassName:" + id, e );
            }
            if ( aClass != null ) {
                desc = this.descriptors.get ( aClass.getName() );
                for ( int i = 0; desc == null && i < StoreRegistry.interfaces.length; ++i ) {
                    if ( StoreRegistry.interfaces[i].isAssignableFrom ( aClass ) ) {
                        desc = this.descriptors.get ( StoreRegistry.interfaces[i].getName() );
                    }
                }
            }
        }
        if ( StoreRegistry.log.isDebugEnabled() ) {
            if ( desc != null ) {
                StoreRegistry.log.debug ( "find descriptor " + id + "#" + desc.getTag() + "#" + desc.getStoreFactoryClass() );
            } else {
                StoreRegistry.log.debug ( "Can't find descriptor for key " + id );
            }
        }
        return desc;
    }
    public StoreDescription findDescription ( final Class<?> aClass ) {
        return this.findDescription ( aClass.getName() );
    }
    public IStoreFactory findStoreFactory ( final String aClassName ) {
        final StoreDescription desc = this.findDescription ( aClassName );
        if ( desc != null ) {
            return desc.getStoreFactory();
        }
        return null;
    }
    public IStoreFactory findStoreFactory ( final Class<?> aClass ) {
        return this.findStoreFactory ( aClass.getName() );
    }
    public void registerDescription ( final StoreDescription desc ) {
        String key = desc.getId();
        if ( key == null || "".equals ( key ) ) {
            key = desc.getTagClass();
        }
        this.descriptors.put ( key, desc );
        if ( StoreRegistry.log.isDebugEnabled() ) {
            StoreRegistry.log.debug ( "register store descriptor " + key + "#" + desc.getTag() + "#" + desc.getTagClass() );
        }
    }
    public StoreDescription unregisterDescription ( final StoreDescription desc ) {
        String key = desc.getId();
        if ( key == null || "".equals ( key ) ) {
            key = desc.getTagClass();
        }
        return this.descriptors.remove ( key );
    }
    public String getEncoding() {
        return this.encoding;
    }
    public void setEncoding ( final String string ) {
        this.encoding = string;
    }
    static {
        StoreRegistry.log = LogFactory.getLog ( StoreRegistry.class );
        StoreRegistry.interfaces = ( Class<?>[] ) new Class[] { CatalinaCluster.class, ChannelSender.class, ChannelReceiver.class, Channel.class, MembershipService.class, ClusterDeployer.class, Realm.class, Manager.class, DirContext.class, LifecycleListener.class, Valve.class, ClusterListener.class, MessageListener.class, DataSender.class, ChannelInterceptor.class, Member.class, WebResourceRoot.class, WebResourceSet.class, CredentialHandler.class, UpgradeProtocol.class, CookieProcessor.class };
    }
}

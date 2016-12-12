package org.apache.catalina.storeconfig;
import java.util.HashMap;
import java.util.Map;
import javax.naming.directory.DirContext;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterDeployer;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.MessageListener;
import org.apache.catalina.tribes.transport.DataSender;
import org.apache.coyote.UpgradeProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.CookieProcessor;
public class StoreRegistry {
    private static Log log = LogFactory.getLog ( StoreRegistry.class );
    private Map<String, StoreDescription> descriptors = new HashMap<>();
    private String encoding = "UTF-8";
    private String name;
    private String version;
    private static Class<?> interfaces[] = { CatalinaCluster.class,
                                             ChannelSender.class, ChannelReceiver.class, Channel.class,
                                             MembershipService.class, ClusterDeployer.class, Realm.class,
                                             Manager.class, DirContext.class, LifecycleListener.class,
                                             Valve.class, ClusterListener.class, MessageListener.class,
                                             DataSender.class, ChannelInterceptor.class, Member.class,
                                             WebResourceRoot.class, WebResourceSet.class,
                                             CredentialHandler.class, UpgradeProtocol.class,
                                             CookieProcessor.class
                                           };
    public String getName() {
        return name;
    }
    public void setName ( String name ) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion ( String version ) {
        this.version = version;
    }
    public StoreDescription findDescription ( String id ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "search descriptor " + id );
        }
        StoreDescription desc = descriptors.get ( id );
        if ( desc == null ) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName ( id, true, this.getClass()
                                         .getClassLoader() );
            } catch ( ClassNotFoundException e ) {
                log.error ( "ClassName:" + id, e );
            }
            if ( aClass != null ) {
                desc = descriptors.get ( aClass.getName() );
                for ( int i = 0; desc == null && i < interfaces.length; i++ ) {
                    if ( interfaces[i].isAssignableFrom ( aClass ) ) {
                        desc = descriptors.get ( interfaces[i].getName() );
                    }
                }
            }
        }
        if ( log.isDebugEnabled() )
            if ( desc != null )
                log.debug ( "find descriptor " + id + "#" + desc.getTag() + "#"
                            + desc.getStoreFactoryClass() );
            else {
                log.debug ( ( "Can't find descriptor for key " + id ) );
            }
        return desc;
    }
    public StoreDescription findDescription ( Class<?> aClass ) {
        return findDescription ( aClass.getName() );
    }
    public IStoreFactory findStoreFactory ( String aClassName ) {
        StoreDescription desc = findDescription ( aClassName );
        if ( desc != null ) {
            return desc.getStoreFactory();
        } else {
            return null;
        }
    }
    public IStoreFactory findStoreFactory ( Class<?> aClass ) {
        return findStoreFactory ( aClass.getName() );
    }
    public void registerDescription ( StoreDescription desc ) {
        String key = desc.getId();
        if ( key == null || "".equals ( key ) ) {
            key = desc.getTagClass();
        }
        descriptors.put ( key, desc );
        if ( log.isDebugEnabled() )
            log.debug ( "register store descriptor " + key + "#" + desc.getTag()
                        + "#" + desc.getTagClass() );
    }
    public StoreDescription unregisterDescription ( StoreDescription desc ) {
        String key = desc.getId();
        if ( key == null || "".equals ( key ) ) {
            key = desc.getTagClass();
        }
        return descriptors.remove ( key );
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding ( String string ) {
        encoding = string;
    }
}

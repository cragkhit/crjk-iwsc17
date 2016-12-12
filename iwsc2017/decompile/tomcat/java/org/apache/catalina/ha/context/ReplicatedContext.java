package org.apache.catalina.ha.context;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.catalina.core.ApplicationContext;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletContext;
import org.apache.catalina.Loader;
import org.apache.catalina.LifecycleException;
import java.util.Map;
import org.apache.catalina.tribes.tipis.ReplicatedMap;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.tipis.AbstractReplicatedMap;
import org.apache.catalina.core.StandardContext;
public class ReplicatedContext extends StandardContext implements AbstractReplicatedMap.MapOwner {
    private int mapSendOptions;
    private static final Log log;
    protected static final long DEFAULT_REPL_TIMEOUT = 15000L;
    private static final StringManager sm;
    public ReplicatedContext() {
        this.mapSendOptions = 2;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        try {
            final CatalinaCluster catclust = ( CatalinaCluster ) this.getCluster();
            if ( this.context == null ) {
                this.context = new ReplApplContext ( this );
            }
            if ( catclust != null ) {
                final ReplicatedMap<String, Object> map = new ReplicatedMap<String, Object> ( this, catclust.getChannel(), 15000L, this.getName(), this.getClassLoaders() );
                map.setChannelSendOptions ( this.mapSendOptions );
                ( ( ReplApplContext ) this.context ).setAttributeMap ( map );
                if ( this.getAltDDName() != null ) {
                    this.context.setAttribute ( "org.apache.catalina.deploy.alt_dd", this.getAltDDName() );
                }
            }
            super.startInternal();
        } catch ( Exception x ) {
            ReplicatedContext.log.error ( ReplicatedContext.sm.getString ( "replicatedContext.startUnable", this.getName() ), x );
            throw new LifecycleException ( ReplicatedContext.sm.getString ( "replicatedContext.startFailed", this.getName() ), x );
        }
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        final Map<String, Object> map = ( ( ReplApplContext ) this.context ).getAttributeMap();
        super.stopInternal();
        if ( map instanceof ReplicatedMap ) {
            ( ( ReplicatedMap ) map ).breakdown();
        }
    }
    public void setMapSendOptions ( final int mapSendOptions ) {
        this.mapSendOptions = mapSendOptions;
    }
    public int getMapSendOptions() {
        return this.mapSendOptions;
    }
    public ClassLoader[] getClassLoaders() {
        Loader loader = null;
        ClassLoader classLoader = null;
        loader = this.getLoader();
        if ( loader != null ) {
            classLoader = loader.getClassLoader();
        }
        if ( classLoader == null ) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if ( classLoader == Thread.currentThread().getContextClassLoader() ) {
            return new ClassLoader[] { classLoader };
        }
        return new ClassLoader[] { classLoader, Thread.currentThread().getContextClassLoader() };
    }
    @Override
    public ServletContext getServletContext() {
        if ( this.context == null ) {
            this.context = new ReplApplContext ( this );
            if ( this.getAltDDName() != null ) {
                this.context.setAttribute ( "org.apache.catalina.deploy.alt_dd", this.getAltDDName() );
            }
        }
        return ( ( ReplApplContext ) this.context ).getFacade();
    }
    @Override
    public void objectMadePrimary ( final Object key, final Object value ) {
    }
    static {
        log = LogFactory.getLog ( ReplicatedContext.class );
        sm = StringManager.getManager ( ReplicatedContext.class );
    }
    protected static class ReplApplContext extends ApplicationContext {
        protected final Map<String, Object> tomcatAttributes;
        public ReplApplContext ( final ReplicatedContext context ) {
            super ( context );
            this.tomcatAttributes = new ConcurrentHashMap<String, Object>();
        }
        protected ReplicatedContext getParent() {
            return ( ReplicatedContext ) this.getContext();
        }
        @Override
        protected ServletContext getFacade() {
            return super.getFacade();
        }
        public Map<String, Object> getAttributeMap() {
            return this.attributes;
        }
        public void setAttributeMap ( final Map<String, Object> map ) {
            this.attributes = map;
        }
        @Override
        public void removeAttribute ( final String name ) {
            this.tomcatAttributes.remove ( name );
            super.removeAttribute ( name );
        }
        @Override
        public void setAttribute ( final String name, final Object value ) {
            if ( name == null ) {
                throw new IllegalArgumentException ( ReplicatedContext.sm.getString ( "applicationContext.setAttribute.namenull" ) );
            }
            if ( value == null ) {
                this.removeAttribute ( name );
                return;
            }
            if ( !this.getParent().getState().isAvailable() || "org.apache.jasper.runtime.JspApplicationContextImpl".equals ( name ) ) {
                this.tomcatAttributes.put ( name, value );
            } else {
                super.setAttribute ( name, value );
            }
        }
        @Override
        public Object getAttribute ( final String name ) {
            final Object obj = this.tomcatAttributes.get ( name );
            if ( obj == null ) {
                return super.getAttribute ( name );
            }
            return obj;
        }
        @Override
        public Enumeration<String> getAttributeNames() {
            final Set<String> names = new HashSet<String>();
            names.addAll ( this.attributes.keySet() );
            return new MultiEnumeration<String> ( new Enumeration[] { super.getAttributeNames(), Collections.enumeration ( names ) } );
        }
    }
    protected static class MultiEnumeration<T> implements Enumeration<T> {
        private final Enumeration<T>[] e;
        public MultiEnumeration ( final Enumeration<T>[] lists ) {
            this.e = lists;
        }
        @Override
        public boolean hasMoreElements() {
            for ( int i = 0; i < this.e.length; ++i ) {
                if ( this.e[i].hasMoreElements() ) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public T nextElement() {
            for ( int i = 0; i < this.e.length; ++i ) {
                if ( this.e[i].hasMoreElements() ) {
                    return this.e[i].nextElement();
                }
            }
            return null;
        }
    }
}

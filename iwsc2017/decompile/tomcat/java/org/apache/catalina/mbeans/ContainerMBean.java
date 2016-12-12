package org.apache.catalina.mbeans;
import org.apache.catalina.ContainerListener;
import java.util.List;
import java.util.ArrayList;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.Valve;
import org.apache.catalina.LifecycleException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.Container;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ContainerMBean extends BaseModelMBean {
    public void addChild ( final String type, final String name ) throws MBeanException {
        Container contained = null;
        try {
            contained = ( Container ) Class.forName ( type ).newInstance();
            contained.setName ( name );
            if ( contained instanceof StandardHost ) {
                final HostConfig config = new HostConfig();
                contained.addLifecycleListener ( config );
            } else if ( contained instanceof StandardContext ) {
                final ContextConfig config2 = new ContextConfig();
                contained.addLifecycleListener ( config2 );
            }
        } catch ( InstantiationException e ) {
            throw new MBeanException ( e );
        } catch ( IllegalAccessException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( ClassNotFoundException e3 ) {
            throw new MBeanException ( e3 );
        }
        boolean oldValue = true;
        ContainerBase container = null;
        try {
            container = ( ContainerBase ) this.getManagedResource();
            oldValue = container.getStartChildren();
            container.setStartChildren ( false );
            container.addChild ( contained );
            contained.init();
        } catch ( InstanceNotFoundException e4 ) {
            throw new MBeanException ( e4 );
        } catch ( RuntimeOperationsException e5 ) {
            throw new MBeanException ( e5 );
        } catch ( InvalidTargetObjectTypeException e6 ) {
            throw new MBeanException ( e6 );
        } catch ( LifecycleException e7 ) {
            throw new MBeanException ( e7 );
        } finally {
            if ( container != null ) {
                container.setStartChildren ( oldValue );
            }
        }
    }
    public void removeChild ( final String name ) throws MBeanException {
        if ( name != null ) {
            try {
                final Container container = ( Container ) this.getManagedResource();
                final Container contained = container.findChild ( name );
                container.removeChild ( contained );
            } catch ( InstanceNotFoundException e ) {
                throw new MBeanException ( e );
            } catch ( RuntimeOperationsException e2 ) {
                throw new MBeanException ( e2 );
            } catch ( InvalidTargetObjectTypeException e3 ) {
                throw new MBeanException ( e3 );
            }
        }
    }
    public String addValve ( final String valveType ) throws MBeanException {
        Valve valve = null;
        try {
            valve = ( Valve ) Class.forName ( valveType ).newInstance();
        } catch ( InstantiationException e ) {
            throw new MBeanException ( e );
        } catch ( IllegalAccessException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( ClassNotFoundException e3 ) {
            throw new MBeanException ( e3 );
        }
        if ( valve == null ) {
            return null;
        }
        try {
            final Container container = ( Container ) this.getManagedResource();
            container.getPipeline().addValve ( valve );
        } catch ( InstanceNotFoundException e4 ) {
            throw new MBeanException ( e4 );
        } catch ( RuntimeOperationsException e5 ) {
            throw new MBeanException ( e5 );
        } catch ( InvalidTargetObjectTypeException e6 ) {
            throw new MBeanException ( e6 );
        }
        if ( valve instanceof JmxEnabled ) {
            return ( ( JmxEnabled ) valve ).getObjectName().toString();
        }
        return null;
    }
    public void removeValve ( final String valveName ) throws MBeanException {
        Container container = null;
        try {
            container = ( Container ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        ObjectName oname;
        try {
            oname = new ObjectName ( valveName );
        } catch ( MalformedObjectNameException e4 ) {
            throw new MBeanException ( e4 );
        } catch ( NullPointerException e5 ) {
            throw new MBeanException ( e5 );
        }
        if ( container != null ) {
            final Valve[] valves = container.getPipeline().getValves();
            for ( int i = 0; i < valves.length; ++i ) {
                if ( valves[i] instanceof JmxEnabled ) {
                    final ObjectName voname = ( ( JmxEnabled ) valves[i] ).getObjectName();
                    if ( voname.equals ( oname ) ) {
                        container.getPipeline().removeValve ( valves[i] );
                    }
                }
            }
        }
    }
    public void addLifecycleListener ( final String type ) throws MBeanException {
        LifecycleListener listener = null;
        try {
            listener = ( LifecycleListener ) Class.forName ( type ).newInstance();
        } catch ( InstantiationException e ) {
            throw new MBeanException ( e );
        } catch ( IllegalAccessException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( ClassNotFoundException e3 ) {
            throw new MBeanException ( e3 );
        }
        if ( listener != null ) {
            try {
                final Container container = ( Container ) this.getManagedResource();
                container.addLifecycleListener ( listener );
            } catch ( InstanceNotFoundException e4 ) {
                throw new MBeanException ( e4 );
            } catch ( RuntimeOperationsException e5 ) {
                throw new MBeanException ( e5 );
            } catch ( InvalidTargetObjectTypeException e6 ) {
                throw new MBeanException ( e6 );
            }
        }
    }
    public void removeLifecycleListeners ( final String type ) throws MBeanException {
        Container container = null;
        try {
            container = ( Container ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final LifecycleListener[] lifecycleListeners;
        final LifecycleListener[] listeners = lifecycleListeners = container.findLifecycleListeners();
        for ( final LifecycleListener listener : lifecycleListeners ) {
            if ( listener.getClass().getName().equals ( type ) ) {
                container.removeLifecycleListener ( listener );
            }
        }
    }
    public String[] findLifecycleListenerNames() throws MBeanException {
        Container container = null;
        final List<String> result = new ArrayList<String>();
        try {
            container = ( Container ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final LifecycleListener[] lifecycleListeners;
        final LifecycleListener[] listeners = lifecycleListeners = container.findLifecycleListeners();
        for ( final LifecycleListener listener : lifecycleListeners ) {
            result.add ( listener.getClass().getName() );
        }
        return result.toArray ( new String[result.size()] );
    }
    public String[] findContainerListenerNames() throws MBeanException {
        Container container = null;
        final List<String> result = new ArrayList<String>();
        try {
            container = ( Container ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final ContainerListener[] containerListeners;
        final ContainerListener[] listeners = containerListeners = container.findContainerListeners();
        for ( final ContainerListener listener : containerListeners ) {
            result.add ( listener.getClass().getName() );
        }
        return result.toArray ( new String[result.size()] );
    }
}

package org.apache.catalina.mbeans;
import java.util.ArrayList;
import java.util.List;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ContainerMBean extends BaseModelMBean {
    public ContainerMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    public void addChild ( String type, String name ) throws MBeanException {
        Container contained = null;
        try {
            contained = ( Container ) Class.forName ( type ).newInstance();
            contained.setName ( name );
            if ( contained instanceof StandardHost ) {
                HostConfig config = new HostConfig();
                contained.addLifecycleListener ( config );
            } else if ( contained instanceof StandardContext ) {
                ContextConfig config = new ContextConfig();
                contained.addLifecycleListener ( config );
            }
        } catch ( InstantiationException e ) {
            throw new MBeanException ( e );
        } catch ( IllegalAccessException e ) {
            throw new MBeanException ( e );
        } catch ( ClassNotFoundException e ) {
            throw new MBeanException ( e );
        }
        boolean oldValue = true;
        ContainerBase container = null;
        try {
            container = ( ContainerBase ) getManagedResource();
            oldValue = container.getStartChildren();
            container.setStartChildren ( false );
            container.addChild ( contained );
            contained.init();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        } catch ( LifecycleException e ) {
            throw new MBeanException ( e );
        } finally {
            if ( container != null ) {
                container.setStartChildren ( oldValue );
            }
        }
    }
    public void removeChild ( String name ) throws MBeanException {
        if ( name != null ) {
            try {
                Container container = ( Container ) getManagedResource();
                Container contained = container.findChild ( name );
                container.removeChild ( contained );
            } catch ( InstanceNotFoundException e ) {
                throw new MBeanException ( e );
            } catch ( RuntimeOperationsException e ) {
                throw new MBeanException ( e );
            } catch ( InvalidTargetObjectTypeException e ) {
                throw new MBeanException ( e );
            }
        }
    }
    public String addValve ( String valveType ) throws MBeanException {
        Valve valve = null;
        try {
            valve = ( Valve ) Class.forName ( valveType ).newInstance();
        } catch ( InstantiationException e ) {
            throw new MBeanException ( e );
        } catch ( IllegalAccessException e ) {
            throw new MBeanException ( e );
        } catch ( ClassNotFoundException e ) {
            throw new MBeanException ( e );
        }
        if ( valve == null ) {
            return null;
        }
        try {
            Container container = ( Container ) getManagedResource();
            container.getPipeline().addValve ( valve );
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        if ( valve instanceof JmxEnabled ) {
            return ( ( JmxEnabled ) valve ).getObjectName().toString();
        } else {
            return null;
        }
    }
    public void removeValve ( String valveName ) throws MBeanException {
        Container container = null;
        try {
            container = ( Container ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        ObjectName oname;
        try {
            oname = new ObjectName ( valveName );
        } catch ( MalformedObjectNameException e ) {
            throw new MBeanException ( e );
        } catch ( NullPointerException e ) {
            throw new MBeanException ( e );
        }
        if ( container != null ) {
            Valve[] valves = container.getPipeline().getValves();
            for ( int i = 0; i < valves.length; i++ ) {
                if ( valves[i] instanceof JmxEnabled ) {
                    ObjectName voname =
                        ( ( JmxEnabled ) valves[i] ).getObjectName();
                    if ( voname.equals ( oname ) ) {
                        container.getPipeline().removeValve ( valves[i] );
                    }
                }
            }
        }
    }
    public void addLifecycleListener ( String type ) throws MBeanException {
        LifecycleListener listener = null;
        try {
            listener = ( LifecycleListener ) Class.forName ( type ).newInstance();
        } catch ( InstantiationException e ) {
            throw new MBeanException ( e );
        } catch ( IllegalAccessException e ) {
            throw new MBeanException ( e );
        } catch ( ClassNotFoundException e ) {
            throw new MBeanException ( e );
        }
        if ( listener != null ) {
            try {
                Container container = ( Container ) getManagedResource();
                container.addLifecycleListener ( listener );
            } catch ( InstanceNotFoundException e ) {
                throw new MBeanException ( e );
            } catch ( RuntimeOperationsException e ) {
                throw new MBeanException ( e );
            } catch ( InvalidTargetObjectTypeException e ) {
                throw new MBeanException ( e );
            }
        }
    }
    public void removeLifecycleListeners ( String type ) throws MBeanException {
        Container container = null;
        try {
            container = ( Container ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        LifecycleListener[] listeners = container.findLifecycleListeners();
        for ( LifecycleListener listener : listeners ) {
            if ( listener.getClass().getName().equals ( type ) ) {
                container.removeLifecycleListener ( listener );
            }
        }
    }
    public String[] findLifecycleListenerNames() throws MBeanException {
        Container container = null;
        List<String> result = new ArrayList<>();
        try {
            container = ( Container ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        LifecycleListener[] listeners = container.findLifecycleListeners();
        for ( LifecycleListener listener : listeners ) {
            result.add ( listener.getClass().getName() );
        }
        return result.toArray ( new String[result.size()] );
    }
    public String[] findContainerListenerNames() throws MBeanException {
        Container container = null;
        List<String> result = new ArrayList<>();
        try {
            container = ( Container ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        ContainerListener[] listeners = container.findContainerListeners();
        for ( ContainerListener listener : listeners ) {
            result.add ( listener.getClass().getName() );
        }
        return result.toArray ( new String[result.size()] );
    }
}

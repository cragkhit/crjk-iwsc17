package org.apache.tomcat.util.modeler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class BaseModelMBean implements DynamicMBean, MBeanRegistration, ModelMBeanNotificationBroadcaster {
    private static final Log log = LogFactory.getLog ( BaseModelMBean.class );
    protected BaseModelMBean() throws MBeanException, RuntimeOperationsException {
        super();
    }
    protected ObjectName oname = null;
    protected BaseNotificationBroadcaster attributeBroadcaster = null;
    protected BaseNotificationBroadcaster generalBroadcaster = null;
    protected ManagedBean managedBean = null;
    protected Object resource = null;
    static final Object[] NO_ARGS_PARAM = new Object[0];
    protected String resourceType = null;
    @Override
    public Object getAttribute ( String name )
    throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if ( name == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute name is null" ),
              "Attribute name is null" );
        if ( ( resource instanceof DynamicMBean ) &&
                ! ( resource instanceof BaseModelMBean ) ) {
            return ( ( DynamicMBean ) resource ).getAttribute ( name );
        }
        Method m = managedBean.getGetter ( name, this, resource );
        Object result = null;
        try {
            Class<?> declaring = m.getDeclaringClass();
            if ( declaring.isAssignableFrom ( this.getClass() ) ) {
                result = m.invoke ( this, NO_ARGS_PARAM );
            } else {
                result = m.invoke ( resource, NO_ARGS_PARAM );
            }
        } catch ( InvocationTargetException e ) {
            Throwable t = e.getTargetException();
            if ( t == null ) {
                t = e;
            }
            if ( t instanceof RuntimeException )
                throw new RuntimeOperationsException
                ( ( RuntimeException ) t, "Exception invoking method " + name );
            else if ( t instanceof Error )
                throw new RuntimeErrorException
                ( ( Error ) t, "Error invoking method " + name );
            else
                throw new MBeanException
                ( e, "Exception invoking method " + name );
        } catch ( Exception e ) {
            throw new MBeanException
            ( e, "Exception invoking method " + name );
        }
        return ( result );
    }
    @Override
    public AttributeList getAttributes ( String names[] ) {
        if ( names == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute names list is null" ),
              "Attribute names list is null" );
        AttributeList response = new AttributeList();
        for ( int i = 0; i < names.length; i++ ) {
            try {
                response.add ( new Attribute ( names[i], getAttribute ( names[i] ) ) );
            } catch ( Exception e ) {
            }
        }
        return ( response );
    }
    public void setManagedBean ( ManagedBean managedBean ) {
        this.managedBean = managedBean;
    }
    @Override
    public MBeanInfo getMBeanInfo() {
        return managedBean.getMBeanInfo();
    }
    @Override
    public Object invoke ( String name, Object params[], String signature[] )
    throws MBeanException, ReflectionException {
        if ( ( resource instanceof DynamicMBean ) &&
                ! ( resource instanceof BaseModelMBean ) ) {
            return ( ( DynamicMBean ) resource ).invoke ( name, params, signature );
        }
        if ( name == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Method name is null" ),
              "Method name is null" );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Invoke " + name );
        }
        Method method = managedBean.getInvoke ( name, params, signature, this, resource );
        Object result = null;
        try {
            if ( method.getDeclaringClass().isAssignableFrom ( this.getClass() ) ) {
                result = method.invoke ( this, params );
            } else {
                result = method.invoke ( resource, params );
            }
        } catch ( InvocationTargetException e ) {
            Throwable t = e.getTargetException();
            log.error ( "Exception invoking method " + name , t );
            if ( t == null ) {
                t = e;
            }
            if ( t instanceof RuntimeException )
                throw new RuntimeOperationsException
                ( ( RuntimeException ) t, "Exception invoking method " + name );
            else if ( t instanceof Error )
                throw new RuntimeErrorException
                ( ( Error ) t, "Error invoking method " + name );
            else
                throw new MBeanException
                ( ( Exception ) t, "Exception invoking method " + name );
        } catch ( Exception e ) {
            log.error ( "Exception invoking method " + name , e );
            throw new MBeanException
            ( e, "Exception invoking method " + name );
        }
        return ( result );
    }
    static Class<?> getAttributeClass ( String signature )
    throws ReflectionException {
        if ( signature.equals ( Boolean.TYPE.getName() ) ) {
            return Boolean.TYPE;
        } else if ( signature.equals ( Byte.TYPE.getName() ) ) {
            return Byte.TYPE;
        } else if ( signature.equals ( Character.TYPE.getName() ) ) {
            return Character.TYPE;
        } else if ( signature.equals ( Double.TYPE.getName() ) ) {
            return Double.TYPE;
        } else if ( signature.equals ( Float.TYPE.getName() ) ) {
            return Float.TYPE;
        } else if ( signature.equals ( Integer.TYPE.getName() ) ) {
            return Integer.TYPE;
        } else if ( signature.equals ( Long.TYPE.getName() ) ) {
            return Long.TYPE;
        } else if ( signature.equals ( Short.TYPE.getName() ) ) {
            return Short.TYPE;
        } else {
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if ( cl != null ) {
                    return cl.loadClass ( signature );
                }
            } catch ( ClassNotFoundException e ) {
            }
            try {
                return Class.forName ( signature );
            } catch ( ClassNotFoundException e ) {
                throw new ReflectionException
                ( e, "Cannot find Class for " + signature );
            }
        }
    }
    @Override
    public void setAttribute ( Attribute attribute )
    throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Setting attribute " + this + " " + attribute );
        }
        if ( ( resource instanceof DynamicMBean ) &&
                ! ( resource instanceof BaseModelMBean ) ) {
            try {
                ( ( DynamicMBean ) resource ).setAttribute ( attribute );
            } catch ( InvalidAttributeValueException e ) {
                throw new MBeanException ( e );
            }
            return;
        }
        if ( attribute == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute is null" ),
              "Attribute is null" );
        String name = attribute.getName();
        Object value = attribute.getValue();
        if ( name == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute name is null" ),
              "Attribute name is null" );
        Object oldValue = null;
        Method m = managedBean.getSetter ( name, this, resource );
        try {
            if ( m.getDeclaringClass().isAssignableFrom ( this.getClass() ) ) {
                m.invoke ( this, new Object[] { value } );
            } else {
                m.invoke ( resource, new Object[] { value } );
            }
        } catch ( InvocationTargetException e ) {
            Throwable t = e.getTargetException();
            if ( t == null ) {
                t = e;
            }
            if ( t instanceof RuntimeException )
                throw new RuntimeOperationsException
                ( ( RuntimeException ) t, "Exception invoking method " + name );
            else if ( t instanceof Error )
                throw new RuntimeErrorException
                ( ( Error ) t, "Error invoking method " + name );
            else
                throw new MBeanException
                ( e, "Exception invoking method " + name );
        } catch ( Exception e ) {
            log.error ( "Exception invoking method " + name , e );
            throw new MBeanException
            ( e, "Exception invoking method " + name );
        }
        try {
            sendAttributeChangeNotification ( new Attribute ( name, oldValue ),
                                              attribute );
        } catch ( Exception ex ) {
            log.error ( "Error sending notification " + name, ex );
        }
    }
    @Override
    public String toString() {
        if ( resource == null ) {
            return "BaseModelMbean[" + resourceType + "]";
        }
        return resource.toString();
    }
    @Override
    public AttributeList setAttributes ( AttributeList attributes ) {
        AttributeList response = new AttributeList();
        if ( attributes == null ) {
            return response;
        }
        String names[] = new String[attributes.size()];
        int n = 0;
        Iterator<?> items = attributes.iterator();
        while ( items.hasNext() ) {
            Attribute item = ( Attribute ) items.next();
            names[n++] = item.getName();
            try {
                setAttribute ( item );
            } catch ( Exception e ) {
            }
        }
        return ( getAttributes ( names ) );
    }
    public Object getManagedResource()
    throws InstanceNotFoundException, InvalidTargetObjectTypeException,
        MBeanException, RuntimeOperationsException {
        if ( resource == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Managed resource is null" ),
              "Managed resource is null" );
        return resource;
    }
    public void setManagedResource ( Object resource, String type )
    throws InstanceNotFoundException,
        MBeanException, RuntimeOperationsException {
        if ( resource == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Managed resource is null" ),
              "Managed resource is null" );
        this.resource = resource;
        this.resourceType = resource.getClass().getName();
    }
    @Override
    public void addAttributeChangeNotificationListener
    ( NotificationListener listener, String name, Object handback )
    throws IllegalArgumentException {
        if ( listener == null ) {
            throw new IllegalArgumentException ( "Listener is null" );
        }
        if ( attributeBroadcaster == null ) {
            attributeBroadcaster = new BaseNotificationBroadcaster();
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "addAttributeNotificationListener " + listener );
        }
        BaseAttributeFilter filter = new BaseAttributeFilter ( name );
        attributeBroadcaster.addNotificationListener
        ( listener, filter, handback );
    }
    @Override
    public void removeAttributeChangeNotificationListener
    ( NotificationListener listener, String name )
    throws ListenerNotFoundException {
        if ( listener == null ) {
            throw new IllegalArgumentException ( "Listener is null" );
        }
        if ( attributeBroadcaster != null ) {
            attributeBroadcaster.removeNotificationListener ( listener );
        }
    }
    @Override
    public void sendAttributeChangeNotification
    ( AttributeChangeNotification notification )
    throws MBeanException, RuntimeOperationsException {
        if ( notification == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Notification is null" ),
              "Notification is null" );
        if ( attributeBroadcaster == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "AttributeChangeNotification " + notification );
        }
        attributeBroadcaster.sendNotification ( notification );
    }
    @Override
    public void sendAttributeChangeNotification
    ( Attribute oldValue, Attribute newValue )
    throws MBeanException, RuntimeOperationsException {
        String type = null;
        if ( newValue.getValue() != null ) {
            type = newValue.getValue().getClass().getName();
        } else if ( oldValue.getValue() != null ) {
            type = oldValue.getValue().getClass().getName();
        } else {
            return;
        }
        AttributeChangeNotification notification =
            new AttributeChangeNotification
        ( this, 1, System.currentTimeMillis(),
          "Attribute value has changed",
          oldValue.getName(), type,
          oldValue.getValue(), newValue.getValue() );
        sendAttributeChangeNotification ( notification );
    }
    @Override
    public void sendNotification ( Notification notification )
    throws MBeanException, RuntimeOperationsException {
        if ( notification == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Notification is null" ),
              "Notification is null" );
        if ( generalBroadcaster == null ) {
            return;
        }
        generalBroadcaster.sendNotification ( notification );
    }
    @Override
    public void sendNotification ( String message )
    throws MBeanException, RuntimeOperationsException {
        if ( message == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Message is null" ),
              "Message is null" );
        Notification notification = new Notification
        ( "jmx.modelmbean.generic", this, 1, message );
        sendNotification ( notification );
    }
    @Override
    public void addNotificationListener ( NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback )
    throws IllegalArgumentException {
        if ( listener == null ) {
            throw new IllegalArgumentException ( "Listener is null" );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "addNotificationListener " + listener );
        }
        if ( generalBroadcaster == null ) {
            generalBroadcaster = new BaseNotificationBroadcaster();
        }
        generalBroadcaster.addNotificationListener
        ( listener, filter, handback );
        if ( attributeBroadcaster == null ) {
            attributeBroadcaster = new BaseNotificationBroadcaster();
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "addAttributeNotificationListener " + listener );
        }
        attributeBroadcaster.addNotificationListener
        ( listener, filter, handback );
    }
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        MBeanNotificationInfo current[] = getMBeanInfo().getNotifications();
        MBeanNotificationInfo response[] =
            new MBeanNotificationInfo[current.length + 2];
        response[0] = new MBeanNotificationInfo
        ( new String[] { "jmx.modelmbean.generic" },
          "GENERIC",
          "Text message notification from the managed resource" );
        response[1] = new MBeanNotificationInfo
        ( new String[] { "jmx.attribute.change" },
          "ATTRIBUTE_CHANGE",
          "Observed MBean attribute value has changed" );
        System.arraycopy ( current, 0, response, 2, current.length );
        return ( response );
    }
    @Override
    public void removeNotificationListener ( NotificationListener listener )
    throws ListenerNotFoundException {
        if ( listener == null ) {
            throw new IllegalArgumentException ( "Listener is null" );
        }
        if ( generalBroadcaster != null ) {
            generalBroadcaster.removeNotificationListener ( listener );
        }
        if ( attributeBroadcaster != null ) {
            attributeBroadcaster.removeNotificationListener ( listener );
        }
    }
    public String getModelerType() {
        return resourceType;
    }
    public String getClassName() {
        return getModelerType();
    }
    public ObjectName getJmxName() {
        return oname;
    }
    public String getObjectName() {
        if ( oname != null ) {
            return oname.toString();
        } else {
            return null;
        }
    }
    @Override
    public ObjectName preRegister ( MBeanServer server,
                                    ObjectName name )
    throws Exception {
        if ( log.isDebugEnabled() ) {
            log.debug ( "preRegister " + resource + " " + name );
        }
        oname = name;
        if ( resource instanceof MBeanRegistration ) {
            oname = ( ( MBeanRegistration ) resource ).preRegister ( server, name );
        }
        return oname;
    }
    @Override
    public void postRegister ( Boolean registrationDone ) {
        if ( resource instanceof MBeanRegistration ) {
            ( ( MBeanRegistration ) resource ).postRegister ( registrationDone );
        }
    }
    @Override
    public void preDeregister() throws Exception {
        if ( resource instanceof MBeanRegistration ) {
            ( ( MBeanRegistration ) resource ).preDeregister();
        }
    }
    @Override
    public void postDeregister() {
        if ( resource instanceof MBeanRegistration ) {
            ( ( MBeanRegistration ) resource ).postDeregister();
        }
    }
}

package org.apache.tomcat.util.modeler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.ServiceNotFoundException;
public class ManagedBean implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final String BASE_MBEAN = "org.apache.tomcat.util.modeler.BaseModelMBean";
    static final Class<?>[] NO_ARGS_PARAM_SIG = new Class[0];
    private final ReadWriteLock mBeanInfoLock = new ReentrantReadWriteLock();
    private transient volatile MBeanInfo info = null;
    private Map<String, AttributeInfo> attributes = new HashMap<>();
    private Map<String, OperationInfo> operations = new HashMap<>();
    protected String className = BASE_MBEAN;
    protected String description = null;
    protected String domain = null;
    protected String group = null;
    protected String name = null;
    private NotificationInfo notifications[] = new NotificationInfo[0];
    protected String type = null;
    public ManagedBean() {
        AttributeInfo ai = new AttributeInfo();
        ai.setName ( "modelerType" );
        ai.setDescription ( "Type of the modeled resource. Can be set only once" );
        ai.setType ( "java.lang.String" );
        ai.setWriteable ( false );
        addAttribute ( ai );
    }
    public AttributeInfo[] getAttributes() {
        AttributeInfo result[] = new AttributeInfo[attributes.size()];
        attributes.values().toArray ( result );
        return result;
    }
    public String getClassName() {
        return this.className;
    }
    public void setClassName ( String className ) {
        mBeanInfoLock.writeLock().lock();
        try {
            this.className = className;
            this.info = null;
        } finally {
            mBeanInfoLock.writeLock().unlock();
        }
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( String description ) {
        mBeanInfoLock.writeLock().lock();
        try {
            this.description = description;
            this.info = null;
        } finally {
            mBeanInfoLock.writeLock().unlock();
        }
    }
    public String getDomain() {
        return this.domain;
    }
    public void setDomain ( String domain ) {
        this.domain = domain;
    }
    public String getGroup() {
        return this.group;
    }
    public void setGroup ( String group ) {
        this.group = group;
    }
    public String getName() {
        return this.name;
    }
    public void setName ( String name ) {
        mBeanInfoLock.writeLock().lock();
        try {
            this.name = name;
            this.info = null;
        } finally {
            mBeanInfoLock.writeLock().unlock();
        }
    }
    public NotificationInfo[] getNotifications() {
        return this.notifications;
    }
    public OperationInfo[] getOperations() {
        OperationInfo[] result = new OperationInfo[operations.size()];
        operations.values().toArray ( result );
        return result;
    }
    public String getType() {
        return ( this.type );
    }
    public void setType ( String type ) {
        mBeanInfoLock.writeLock().lock();
        try {
            this.type = type;
            this.info = null;
        } finally {
            mBeanInfoLock.writeLock().unlock();
        }
    }
    public void addAttribute ( AttributeInfo attribute ) {
        attributes.put ( attribute.getName(), attribute );
    }
    public void addNotification ( NotificationInfo notification ) {
        mBeanInfoLock.writeLock().lock();
        try {
            NotificationInfo results[] =
                new NotificationInfo[notifications.length + 1];
            System.arraycopy ( notifications, 0, results, 0,
                               notifications.length );
            results[notifications.length] = notification;
            notifications = results;
            this.info = null;
        } finally {
            mBeanInfoLock.writeLock().unlock();
        }
    }
    public void addOperation ( OperationInfo operation ) {
        operations.put ( createOperationKey ( operation ), operation );
    }
    public DynamicMBean createMBean ( Object instance )
    throws InstanceNotFoundException,
        MBeanException, RuntimeOperationsException {
        BaseModelMBean mbean = null;
        if ( getClassName().equals ( BASE_MBEAN ) ) {
            mbean = new BaseModelMBean();
        } else {
            Class<?> clazz = null;
            Exception ex = null;
            try {
                clazz = Class.forName ( getClassName() );
            } catch ( Exception e ) {
            }
            if ( clazz == null ) {
                try {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if ( cl != null ) {
                        clazz = cl.loadClass ( getClassName() );
                    }
                } catch ( Exception e ) {
                    ex = e;
                }
            }
            if ( clazz == null ) {
                throw new MBeanException
                ( ex, "Cannot load ModelMBean class " + getClassName() );
            }
            try {
                mbean = ( BaseModelMBean ) clazz.newInstance();
            } catch ( RuntimeOperationsException e ) {
                throw e;
            } catch ( Exception e ) {
                throw new MBeanException
                ( e, "Cannot instantiate ModelMBean of class " +
                  getClassName() );
            }
        }
        mbean.setManagedBean ( this );
        try {
            if ( instance != null ) {
                mbean.setManagedResource ( instance, "ObjectReference" );
            }
        } catch ( InstanceNotFoundException e ) {
            throw e;
        }
        return mbean;
    }
    MBeanInfo getMBeanInfo() {
        mBeanInfoLock.readLock().lock();
        try {
            if ( info != null ) {
                return info;
            }
        } finally {
            mBeanInfoLock.readLock().unlock();
        }
        mBeanInfoLock.writeLock().lock();
        try {
            if ( info == null ) {
                AttributeInfo attrs[] = getAttributes();
                MBeanAttributeInfo attributes[] =
                    new MBeanAttributeInfo[attrs.length];
                for ( int i = 0; i < attrs.length; i++ ) {
                    attributes[i] = attrs[i].createAttributeInfo();
                }
                OperationInfo opers[] = getOperations();
                MBeanOperationInfo operations[] =
                    new MBeanOperationInfo[opers.length];
                for ( int i = 0; i < opers.length; i++ ) {
                    operations[i] = opers[i].createOperationInfo();
                }
                NotificationInfo notifs[] = getNotifications();
                MBeanNotificationInfo notifications[] =
                    new MBeanNotificationInfo[notifs.length];
                for ( int i = 0; i < notifs.length; i++ ) {
                    notifications[i] = notifs[i].createNotificationInfo();
                }
                info = new MBeanInfo ( getClassName(),
                                       getDescription(),
                                       attributes,
                                       new MBeanConstructorInfo[] {},
                                       operations,
                                       notifications );
            }
            return info;
        } finally {
            mBeanInfoLock.writeLock().unlock();
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ManagedBean[" );
        sb.append ( "name=" );
        sb.append ( name );
        sb.append ( ", className=" );
        sb.append ( className );
        sb.append ( ", description=" );
        sb.append ( description );
        if ( group != null ) {
            sb.append ( ", group=" );
            sb.append ( group );
        }
        sb.append ( ", type=" );
        sb.append ( type );
        sb.append ( "]" );
        return sb.toString();
    }
    Method getGetter ( String aname, BaseModelMBean mbean, Object resource )
    throws AttributeNotFoundException, ReflectionException {
        Method m = null;
        AttributeInfo attrInfo = attributes.get ( aname );
        if ( attrInfo == null ) {
            throw new AttributeNotFoundException ( " Cannot find attribute " + aname + " for " + resource );
        }
        String getMethod = attrInfo.getGetMethod();
        if ( getMethod == null ) {
            throw new AttributeNotFoundException ( "Cannot find attribute " + aname + " get method name" );
        }
        Object object = null;
        NoSuchMethodException exception = null;
        try {
            object = mbean;
            m = object.getClass().getMethod ( getMethod, NO_ARGS_PARAM_SIG );
        } catch ( NoSuchMethodException e ) {
            exception = e;
        }
        if ( m == null && resource != null ) {
            try {
                object = resource;
                m = object.getClass().getMethod ( getMethod, NO_ARGS_PARAM_SIG );
                exception = null;
            } catch ( NoSuchMethodException e ) {
                exception = e;
            }
        }
        if ( exception != null )
            throw new ReflectionException ( exception,
                                            "Cannot find getter method " + getMethod );
        return m;
    }
    public Method getSetter ( String aname, BaseModelMBean bean, Object resource )
    throws AttributeNotFoundException, ReflectionException {
        Method m = null;
        AttributeInfo attrInfo = attributes.get ( aname );
        if ( attrInfo == null ) {
            throw new AttributeNotFoundException ( " Cannot find attribute " + aname );
        }
        String setMethod = attrInfo.getSetMethod();
        if ( setMethod == null ) {
            throw new AttributeNotFoundException ( "Cannot find attribute " + aname + " set method name" );
        }
        String argType = attrInfo.getType();
        Class<?> signature[] =
            new Class[] { BaseModelMBean.getAttributeClass ( argType ) };
        Object object = null;
        NoSuchMethodException exception = null;
        try {
            object = bean;
            m = object.getClass().getMethod ( setMethod, signature );
        } catch ( NoSuchMethodException e ) {
            exception = e;
        }
        if ( m == null && resource != null ) {
            try {
                object = resource;
                m = object.getClass().getMethod ( setMethod, signature );
                exception = null;
            } catch ( NoSuchMethodException e ) {
                exception = e;
            }
        }
        if ( exception != null )
            throw new ReflectionException ( exception,
                                            "Cannot find setter method " + setMethod +
                                            " " + resource );
        return m;
    }
    public Method getInvoke ( String aname, Object[] params, String[] signature, BaseModelMBean bean, Object resource )
    throws MBeanException, ReflectionException {
        Method method = null;
        if ( params == null ) {
            params = new Object[0];
        }
        if ( signature == null ) {
            signature = new String[0];
        }
        if ( params.length != signature.length )
            throw new RuntimeOperationsException (
                new IllegalArgumentException (
                    "Inconsistent arguments and signature" ),
                "Inconsistent arguments and signature" );
        OperationInfo opInfo =
            operations.get ( createOperationKey ( aname, signature ) );
        if ( opInfo == null )
            throw new MBeanException ( new ServiceNotFoundException (
                                           "Cannot find operation " + aname ),
                                       "Cannot find operation " + aname );
        Class<?> types[] = new Class[signature.length];
        for ( int i = 0; i < signature.length; i++ ) {
            types[i] = BaseModelMBean.getAttributeClass ( signature[i] );
        }
        Object object = null;
        Exception exception = null;
        try {
            object = bean;
            method = object.getClass().getMethod ( aname, types );
        } catch ( NoSuchMethodException e ) {
            exception = e;
        }
        try {
            if ( ( method == null ) && ( resource != null ) ) {
                object = resource;
                method = object.getClass().getMethod ( aname, types );
            }
        } catch ( NoSuchMethodException e ) {
            exception = e;
        }
        if ( method == null ) {
            throw new ReflectionException ( exception, "Cannot find method "
                                            + aname + " with this signature" );
        }
        return method;
    }
    private String createOperationKey ( OperationInfo operation ) {
        StringBuilder key = new StringBuilder ( operation.getName() );
        key.append ( '(' );
        for ( ParameterInfo parameterInfo : operation.getSignature() ) {
            key.append ( parameterInfo.getType() );
            key.append ( ',' );
        }
        key.append ( ')' );
        return key.toString();
    }
    private String createOperationKey ( String methodName,
                                        String[] parameterTypes ) {
        StringBuilder key = new StringBuilder ( methodName );
        key.append ( '(' );
        for ( String parameter : parameterTypes ) {
            key.append ( parameter );
            key.append ( ',' );
        }
        key.append ( ')' );
        return key.toString();
    }
}

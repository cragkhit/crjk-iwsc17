package org.apache.naming.factory.webservices;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.Hashtable;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
public class ServiceProxy implements InvocationHandler {
    private final Service service;
    private static Method portQNameClass = null;
    private static Method portClass = null;
    private Hashtable<String, QName> portComponentRef = null;
    public ServiceProxy ( Service service ) throws ServiceException {
        this.service = service;
        try {
            portQNameClass = Service.class.getDeclaredMethod ( "getPort", new Class[] {QName.class, Class.class} );
            portClass = Service.class.getDeclaredMethod ( "getPort", new Class[] {Class.class} );
        } catch ( Exception e ) {
            throw new ServiceException ( e );
        }
    }
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args )
    throws Throwable {
        if ( portQNameClass.equals ( method ) ) {
            return getProxyPortQNameClass ( args );
        }
        if ( portClass.equals ( method ) ) {
            return getProxyPortClass ( args );
        }
        try {
            return method.invoke ( service, args );
        } catch ( InvocationTargetException ite ) {
            throw ite.getTargetException();
        }
    }
    private Object getProxyPortQNameClass ( Object[] args )
    throws ServiceException {
        QName name = ( QName ) args[0];
        String nameString = name.getLocalPart();
        Class<?> serviceendpointClass = ( Class<?> ) args[1];
        for ( @SuppressWarnings ( "unchecked" )
                Iterator<QName> ports = service.getPorts(); ports.hasNext(); ) {
            QName portName = ports.next();
            String portnameString = portName.getLocalPart();
            if ( portnameString.equals ( nameString ) ) {
                return service.getPort ( name, serviceendpointClass );
            }
        }
        throw new ServiceException ( "Port-component-ref : " + name + " not found" );
    }
    public void setPortComponentRef ( Hashtable<String, QName> portComponentRef ) {
        this.portComponentRef = portComponentRef;
    }
    private Remote getProxyPortClass ( Object[] args )
    throws ServiceException {
        Class<?> serviceendpointClass = ( Class<?> ) args[0];
        if ( this.portComponentRef == null ) {
            return service.getPort ( serviceendpointClass );
        }
        QName portname = this.portComponentRef.get ( serviceendpointClass.getName() );
        if ( portname != null ) {
            return service.getPort ( portname, serviceendpointClass );
        } else {
            return service.getPort ( serviceendpointClass );
        }
    }
}

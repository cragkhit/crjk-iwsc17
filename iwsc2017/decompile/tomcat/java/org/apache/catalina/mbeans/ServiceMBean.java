package org.apache.catalina.mbeans;
import org.apache.catalina.Executor;
import org.apache.catalina.connector.Connector;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import org.apache.catalina.Service;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ServiceMBean extends BaseModelMBean {
    public void addConnector ( final String address, final int port, final boolean isAjp, final boolean isSSL ) throws MBeanException {
        Service service;
        try {
            service = ( Service ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final String protocol = isAjp ? "AJP/1.3" : "HTTP/1.1";
        final Connector connector = new Connector ( protocol );
        if ( address != null && address.length() > 0 ) {
            connector.setProperty ( "address", address );
        }
        connector.setPort ( port );
        connector.setSecure ( isSSL );
        connector.setScheme ( isSSL ? "https" : "http" );
        service.addConnector ( connector );
    }
    public void addExecutor ( final String type ) throws MBeanException {
        Service service;
        try {
            service = ( Service ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        Executor executor;
        try {
            executor = ( Executor ) Class.forName ( type ).newInstance();
        } catch ( InstantiationException e4 ) {
            throw new MBeanException ( e4 );
        } catch ( IllegalAccessException e5 ) {
            throw new MBeanException ( e5 );
        } catch ( ClassNotFoundException e6 ) {
            throw new MBeanException ( e6 );
        }
        service.addExecutor ( executor );
    }
    public String[] findConnectors() throws MBeanException {
        Service service;
        try {
            service = ( Service ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final Connector[] connectors = service.findConnectors();
        final String[] str = new String[connectors.length];
        for ( int i = 0; i < connectors.length; ++i ) {
            str[i] = connectors[i].toString();
        }
        return str;
    }
    public String[] findExecutors() throws MBeanException {
        Service service;
        try {
            service = ( Service ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final Executor[] executors = service.findExecutors();
        final String[] str = new String[executors.length];
        for ( int i = 0; i < executors.length; ++i ) {
            str[i] = executors[i].toString();
        }
        return str;
    }
    public String getExecutor ( final String name ) throws MBeanException {
        Service service;
        try {
            service = ( Service ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final Executor executor = service.getExecutor ( name );
        return executor.toString();
    }
}

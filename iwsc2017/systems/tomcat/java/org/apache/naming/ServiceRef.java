package org.apache.naming;
import java.util.Enumeration;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
public class ServiceRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY =
        org.apache.naming.factory.Constants.DEFAULT_SERVICE_FACTORY;
    public static final String SERVICE_INTERFACE  = "serviceInterface";
    public static final String SERVICE_NAMESPACE  = "service namespace";
    public static final String SERVICE_LOCAL_PART = "service local part";
    public static final String WSDL      = "wsdl";
    public static final String JAXRPCMAPPING = "jaxrpcmapping";
    public static final String PORTCOMPONENTLINK = "portcomponentlink";
    public static final String SERVICEENDPOINTINTERFACE = "serviceendpointinterface";
    private final Vector<HandlerRef> handlers = new Vector<>();
    public ServiceRef ( String refname, String serviceInterface, String[] serviceQname,
                        String wsdl, String jaxrpcmapping ) {
        this ( refname, serviceInterface, serviceQname, wsdl, jaxrpcmapping,
               null, null );
    }
    public ServiceRef ( @SuppressWarnings ( "unused" ) String refname,
                        String serviceInterface, String[] serviceQname,
                        String wsdl, String jaxrpcmapping,
                        String factory, String factoryLocation ) {
        super ( serviceInterface, factory, factoryLocation );
        StringRefAddr refAddr = null;
        if ( serviceInterface != null ) {
            refAddr = new StringRefAddr ( SERVICE_INTERFACE, serviceInterface );
            add ( refAddr );
        }
        if ( serviceQname[0] != null ) {
            refAddr = new StringRefAddr ( SERVICE_NAMESPACE, serviceQname[0] );
            add ( refAddr );
        }
        if ( serviceQname[1] != null ) {
            refAddr = new StringRefAddr ( SERVICE_LOCAL_PART, serviceQname[1] );
            add ( refAddr );
        }
        if ( wsdl != null ) {
            refAddr = new StringRefAddr ( WSDL, wsdl );
            add ( refAddr );
        }
        if ( jaxrpcmapping != null ) {
            refAddr = new StringRefAddr ( JAXRPCMAPPING, jaxrpcmapping );
            add ( refAddr );
        }
    }
    public HandlerRef getHandler() {
        return handlers.remove ( 0 );
    }
    public int getHandlersSize() {
        return handlers.size();
    }
    public void addHandler ( HandlerRef handler ) {
        handlers.add ( handler );
    }
    @Override
    public String getFactoryClassName() {
        String factory = super.getFactoryClassName();
        if ( factory != null ) {
            return factory;
        } else {
            factory = System.getProperty ( Context.OBJECT_FACTORIES );
            if ( factory != null ) {
                return null;
            } else {
                return DEFAULT_FACTORY;
            }
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ServiceRef[" );
        sb.append ( "className=" );
        sb.append ( getClassName() );
        sb.append ( ",factoryClassLocation=" );
        sb.append ( getFactoryClassLocation() );
        sb.append ( ",factoryClassName=" );
        sb.append ( getFactoryClassName() );
        Enumeration<RefAddr> refAddrs = getAll();
        while ( refAddrs.hasMoreElements() ) {
            RefAddr refAddr = refAddrs.nextElement();
            sb.append ( ",{type=" );
            sb.append ( refAddr.getType() );
            sb.append ( ",content=" );
            sb.append ( refAddr.getContent() );
            sb.append ( "}" );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
}

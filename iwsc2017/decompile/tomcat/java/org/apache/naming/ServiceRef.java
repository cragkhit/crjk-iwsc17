package org.apache.naming;
import java.util.Enumeration;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import java.util.Vector;
import javax.naming.Reference;
public class ServiceRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY = "org.apache.naming.factory.webservices.ServiceRefFactory";
    public static final String SERVICE_INTERFACE = "serviceInterface";
    public static final String SERVICE_NAMESPACE = "service namespace";
    public static final String SERVICE_LOCAL_PART = "service local part";
    public static final String WSDL = "wsdl";
    public static final String JAXRPCMAPPING = "jaxrpcmapping";
    public static final String PORTCOMPONENTLINK = "portcomponentlink";
    public static final String SERVICEENDPOINTINTERFACE = "serviceendpointinterface";
    private final Vector<HandlerRef> handlers;
    public ServiceRef ( final String refname, final String serviceInterface, final String[] serviceQname, final String wsdl, final String jaxrpcmapping ) {
        this ( refname, serviceInterface, serviceQname, wsdl, jaxrpcmapping, null, null );
    }
    public ServiceRef ( final String refname, final String serviceInterface, final String[] serviceQname, final String wsdl, final String jaxrpcmapping, final String factory, final String factoryLocation ) {
        super ( serviceInterface, factory, factoryLocation );
        this.handlers = new Vector<HandlerRef>();
        StringRefAddr refAddr = null;
        if ( serviceInterface != null ) {
            refAddr = new StringRefAddr ( "serviceInterface", serviceInterface );
            this.add ( refAddr );
        }
        if ( serviceQname[0] != null ) {
            refAddr = new StringRefAddr ( "service namespace", serviceQname[0] );
            this.add ( refAddr );
        }
        if ( serviceQname[1] != null ) {
            refAddr = new StringRefAddr ( "service local part", serviceQname[1] );
            this.add ( refAddr );
        }
        if ( wsdl != null ) {
            refAddr = new StringRefAddr ( "wsdl", wsdl );
            this.add ( refAddr );
        }
        if ( jaxrpcmapping != null ) {
            refAddr = new StringRefAddr ( "jaxrpcmapping", jaxrpcmapping );
            this.add ( refAddr );
        }
    }
    public HandlerRef getHandler() {
        return this.handlers.remove ( 0 );
    }
    public int getHandlersSize() {
        return this.handlers.size();
    }
    public void addHandler ( final HandlerRef handler ) {
        this.handlers.add ( handler );
    }
    @Override
    public String getFactoryClassName() {
        String factory = super.getFactoryClassName();
        if ( factory != null ) {
            return factory;
        }
        factory = System.getProperty ( "java.naming.factory.object" );
        if ( factory != null ) {
            return null;
        }
        return "org.apache.naming.factory.webservices.ServiceRefFactory";
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ServiceRef[" );
        sb.append ( "className=" );
        sb.append ( this.getClassName() );
        sb.append ( ",factoryClassLocation=" );
        sb.append ( this.getFactoryClassLocation() );
        sb.append ( ",factoryClassName=" );
        sb.append ( this.getFactoryClassName() );
        final Enumeration<RefAddr> refAddrs = this.getAll();
        while ( refAddrs.hasMoreElements() ) {
            final RefAddr refAddr = refAddrs.nextElement();
            sb.append ( ",{type=" );
            sb.append ( refAddr.getType() );
            sb.append ( ",content=" );
            sb.append ( refAddr.getContent() );
            sb.append ( "}" );
        }
        sb.append ( "]" );
        return sb.toString();
    }
}

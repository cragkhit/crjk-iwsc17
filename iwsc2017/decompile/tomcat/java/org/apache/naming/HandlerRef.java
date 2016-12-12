package org.apache.naming;
import java.util.Enumeration;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
public class HandlerRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY = "org.apache.naming.factory.HandlerFactory";
    public static final String HANDLER_NAME = "handlername";
    public static final String HANDLER_CLASS = "handlerclass";
    public static final String HANDLER_LOCALPART = "handlerlocalpart";
    public static final String HANDLER_NAMESPACE = "handlernamespace";
    public static final String HANDLER_PARAMNAME = "handlerparamname";
    public static final String HANDLER_PARAMVALUE = "handlerparamvalue";
    public static final String HANDLER_SOAPROLE = "handlersoaprole";
    public static final String HANDLER_PORTNAME = "handlerportname";
    public HandlerRef ( final String refname, final String handlerClass ) {
        this ( refname, handlerClass, null, null );
    }
    public HandlerRef ( final String refname, final String handlerClass, final String factory, final String factoryLocation ) {
        super ( refname, factory, factoryLocation );
        StringRefAddr refAddr = null;
        if ( refname != null ) {
            refAddr = new StringRefAddr ( "handlername", refname );
            this.add ( refAddr );
        }
        if ( handlerClass != null ) {
            refAddr = new StringRefAddr ( "handlerclass", handlerClass );
            this.add ( refAddr );
        }
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
        return "org.apache.naming.factory.HandlerFactory";
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "HandlerRef[" );
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

package org.apache.naming;
import java.util.Enumeration;
import javax.naming.Context;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
public class ResourceRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY =
        org.apache.naming.factory.Constants.DEFAULT_RESOURCE_FACTORY;
    public static final String DESCRIPTION = "description";
    public static final String SCOPE = "scope";
    public static final String AUTH = "auth";
    public static final String SINGLETON = "singleton";
    public ResourceRef ( String resourceClass, String description,
                         String scope, String auth, boolean singleton ) {
        this ( resourceClass, description, scope, auth, singleton, null, null );
    }
    public ResourceRef ( String resourceClass, String description,
                         String scope, String auth, boolean singleton,
                         String factory, String factoryLocation ) {
        super ( resourceClass, factory, factoryLocation );
        StringRefAddr refAddr = null;
        if ( description != null ) {
            refAddr = new StringRefAddr ( DESCRIPTION, description );
            add ( refAddr );
        }
        if ( scope != null ) {
            refAddr = new StringRefAddr ( SCOPE, scope );
            add ( refAddr );
        }
        if ( auth != null ) {
            refAddr = new StringRefAddr ( AUTH, auth );
            add ( refAddr );
        }
        refAddr = new StringRefAddr ( SINGLETON, Boolean.toString ( singleton ) );
        add ( refAddr );
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
        StringBuilder sb = new StringBuilder ( "ResourceRef[" );
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

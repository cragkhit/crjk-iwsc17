package org.apache.naming;
import java.util.Enumeration;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
public class ResourceRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY = "org.apache.naming.factory.ResourceFactory";
    public static final String DESCRIPTION = "description";
    public static final String SCOPE = "scope";
    public static final String AUTH = "auth";
    public static final String SINGLETON = "singleton";
    public ResourceRef ( final String resourceClass, final String description, final String scope, final String auth, final boolean singleton ) {
        this ( resourceClass, description, scope, auth, singleton, null, null );
    }
    public ResourceRef ( final String resourceClass, final String description, final String scope, final String auth, final boolean singleton, final String factory, final String factoryLocation ) {
        super ( resourceClass, factory, factoryLocation );
        StringRefAddr refAddr = null;
        if ( description != null ) {
            refAddr = new StringRefAddr ( "description", description );
            this.add ( refAddr );
        }
        if ( scope != null ) {
            refAddr = new StringRefAddr ( "scope", scope );
            this.add ( refAddr );
        }
        if ( auth != null ) {
            refAddr = new StringRefAddr ( "auth", auth );
            this.add ( refAddr );
        }
        refAddr = new StringRefAddr ( "singleton", Boolean.toString ( singleton ) );
        this.add ( refAddr );
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
        return "org.apache.naming.factory.ResourceFactory";
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ResourceRef[" );
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

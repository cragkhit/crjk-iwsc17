package org.apache.naming;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
public class ResourceLinkRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY =
        org.apache.naming.factory.Constants.DEFAULT_RESOURCE_LINK_FACTORY;
    public static final String GLOBALNAME = "globalName";
    public ResourceLinkRef ( String resourceClass, String globalName,
                             String factory, String factoryLocation ) {
        super ( resourceClass, factory, factoryLocation );
        StringRefAddr refAddr = null;
        if ( globalName != null ) {
            refAddr = new StringRefAddr ( GLOBALNAME, globalName );
            add ( refAddr );
        }
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
}

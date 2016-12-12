package org.apache.naming;
import javax.naming.Context;
import javax.naming.Reference;
public class ResourceEnvRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY =
        org.apache.naming.factory.Constants.DEFAULT_RESOURCE_ENV_FACTORY;
    public ResourceEnvRef ( String resourceType ) {
        super ( resourceType );
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

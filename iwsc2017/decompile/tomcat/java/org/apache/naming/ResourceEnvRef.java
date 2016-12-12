package org.apache.naming;
import javax.naming.Reference;
public class ResourceEnvRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY = "org.apache.naming.factory.ResourceEnvFactory";
    public ResourceEnvRef ( final String resourceType ) {
        super ( resourceType );
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
        return "org.apache.naming.factory.ResourceEnvFactory";
    }
}

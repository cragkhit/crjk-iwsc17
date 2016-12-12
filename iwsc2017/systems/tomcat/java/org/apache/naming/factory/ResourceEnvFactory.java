package org.apache.naming.factory;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import org.apache.naming.ResourceEnvRef;
public class ResourceEnvFactory extends FactoryBase {
    @Override
    protected boolean isReferenceTypeSupported ( Object obj ) {
        return obj instanceof ResourceEnvRef;
    }
    @Override
    protected ObjectFactory getDefaultFactory ( Reference ref ) {
        return null;
    }
    @Override
    protected Object getLinked ( Reference ref ) {
        return null;
    }
}

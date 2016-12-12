package org.apache.naming.factory;
import javax.naming.spi.ObjectFactory;
import javax.naming.Reference;
import org.apache.naming.TransactionRef;
public class TransactionFactory extends FactoryBase {
    @Override
    protected boolean isReferenceTypeSupported ( final Object obj ) {
        return obj instanceof TransactionRef;
    }
    @Override
    protected ObjectFactory getDefaultFactory ( final Reference ref ) {
        return null;
    }
    @Override
    protected Object getLinked ( final Reference ref ) {
        return null;
    }
}

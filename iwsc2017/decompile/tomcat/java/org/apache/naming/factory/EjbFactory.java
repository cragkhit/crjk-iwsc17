package org.apache.naming.factory;
import javax.naming.RefAddr;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import javax.naming.Reference;
import org.apache.naming.EjbRef;
public class EjbFactory extends FactoryBase {
    @Override
    protected boolean isReferenceTypeSupported ( final Object obj ) {
        return obj instanceof EjbRef;
    }
    @Override
    protected ObjectFactory getDefaultFactory ( final Reference ref ) throws NamingException {
        final String javaxEjbFactoryClassName = System.getProperty ( "javax.ejb.Factory", "org.apache.naming.factory.OpenEjbFactory" );
        ObjectFactory factory;
        try {
            factory = ( ObjectFactory ) Class.forName ( javaxEjbFactoryClassName ).newInstance();
        } catch ( Throwable t ) {
            if ( t instanceof NamingException ) {
                throw ( NamingException ) t;
            }
            if ( t instanceof ThreadDeath ) {
                throw ( ThreadDeath ) t;
            }
            if ( t instanceof VirtualMachineError ) {
                throw ( VirtualMachineError ) t;
            }
            final NamingException ex = new NamingException ( "Could not create resource factory instance" );
            ex.initCause ( t );
            throw ex;
        }
        return factory;
    }
    @Override
    protected Object getLinked ( final Reference ref ) throws NamingException {
        final RefAddr linkRefAddr = ref.get ( "link" );
        if ( linkRefAddr != null ) {
            final String ejbLink = linkRefAddr.getContent().toString();
            final Object beanObj = new InitialContext().lookup ( ejbLink );
            return beanObj;
        }
        return null;
    }
}

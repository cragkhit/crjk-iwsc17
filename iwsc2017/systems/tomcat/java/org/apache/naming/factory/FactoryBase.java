package org.apache.naming.factory;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
public abstract class FactoryBase implements ObjectFactory {
    @Override
    public final Object getObjectInstance ( Object obj, Name name, Context nameCtx,
                                            Hashtable<?, ?> environment ) throws Exception {
        if ( isReferenceTypeSupported ( obj ) ) {
            Reference ref = ( Reference ) obj;
            Object linked = getLinked ( ref );
            if ( linked != null ) {
                return linked;
            }
            ObjectFactory factory = null;
            RefAddr factoryRefAddr = ref.get ( Constants.FACTORY );
            if ( factoryRefAddr != null ) {
                String factoryClassName = factoryRefAddr.getContent().toString();
                ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                Class<?> factoryClass = null;
                try {
                    if ( tcl != null ) {
                        factoryClass = tcl.loadClass ( factoryClassName );
                    } else {
                        factoryClass = Class.forName ( factoryClassName );
                    }
                } catch ( ClassNotFoundException e ) {
                    NamingException ex = new NamingException (
                        "Could not load resource factory class" );
                    ex.initCause ( e );
                    throw ex;
                }
                try {
                    factory = ( ObjectFactory ) factoryClass.newInstance();
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
                    NamingException ex = new NamingException (
                        "Could not create resource factory instance" );
                    ex.initCause ( t );
                    throw ex;
                }
            } else {
                factory = getDefaultFactory ( ref );
            }
            if ( factory != null ) {
                return factory.getObjectInstance ( obj, name, nameCtx, environment );
            } else {
                throw new NamingException ( "Cannot create resource instance" );
            }
        }
        return null;
    }
    protected abstract boolean isReferenceTypeSupported ( Object obj );
    protected abstract ObjectFactory getDefaultFactory ( Reference ref )
    throws NamingException;
    protected abstract Object getLinked ( Reference ref ) throws NamingException;
}

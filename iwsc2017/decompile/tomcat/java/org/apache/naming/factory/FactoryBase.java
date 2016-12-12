package org.apache.naming.factory;
import javax.naming.RefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
public abstract class FactoryBase implements ObjectFactory {
    @Override
    public final Object getObjectInstance ( final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment ) throws Exception {
        if ( !this.isReferenceTypeSupported ( obj ) ) {
            return null;
        }
        final Reference ref = ( Reference ) obj;
        final Object linked = this.getLinked ( ref );
        if ( linked != null ) {
            return linked;
        }
        ObjectFactory factory = null;
        final RefAddr factoryRefAddr = ref.get ( "factory" );
        if ( factoryRefAddr != null ) {
            final String factoryClassName = factoryRefAddr.getContent().toString();
            final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            Class<?> factoryClass = null;
            try {
                if ( tcl != null ) {
                    factoryClass = tcl.loadClass ( factoryClassName );
                } else {
                    factoryClass = Class.forName ( factoryClassName );
                }
            } catch ( ClassNotFoundException e ) {
                final NamingException ex = new NamingException ( "Could not load resource factory class" );
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
                final NamingException ex = new NamingException ( "Could not create resource factory instance" );
                ex.initCause ( t );
                throw ex;
            }
        } else {
            factory = this.getDefaultFactory ( ref );
        }
        if ( factory != null ) {
            return factory.getObjectInstance ( obj, name, nameCtx, environment );
        }
        throw new NamingException ( "Cannot create resource instance" );
    }
    protected abstract boolean isReferenceTypeSupported ( final Object p0 );
    protected abstract ObjectFactory getDefaultFactory ( final Reference p0 ) throws NamingException;
    protected abstract Object getLinked ( final Reference p0 ) throws NamingException;
}

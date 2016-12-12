package org.apache.naming.java;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;
import org.apache.naming.ContextBindings;
import org.apache.naming.NamingContext;
import org.apache.naming.SelectorContext;
public class javaURLContextFactory
    implements ObjectFactory, InitialContextFactory {
    public static final String MAIN = "initialContext";
    protected static volatile Context initialContext = null;
    @SuppressWarnings ( "unchecked" )
    @Override
    public Object getObjectInstance ( Object obj, Name name, Context nameCtx,
                                      Hashtable<?, ?> environment )
    throws NamingException {
        if ( ( ContextBindings.isThreadBound() ) ||
                ( ContextBindings.isClassLoaderBound() ) ) {
            return new SelectorContext ( ( Hashtable<String, Object> ) environment );
        }
        return null;
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public Context getInitialContext ( Hashtable<?, ?> environment )
    throws NamingException {
        if ( ContextBindings.isThreadBound() ||
                ( ContextBindings.isClassLoaderBound() ) ) {
            return new SelectorContext (
                       ( Hashtable<String, Object> ) environment, true );
        }
        if ( initialContext == null ) {
            synchronized ( javaURLContextFactory.class ) {
                if ( initialContext == null ) {
                    initialContext = new NamingContext (
                        ( Hashtable<String, Object> ) environment, MAIN );
                }
            }
        }
        return initialContext;
    }
}

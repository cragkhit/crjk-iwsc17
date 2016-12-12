package org.apache.naming.java;
import org.apache.naming.NamingContext;
import javax.naming.NamingException;
import org.apache.naming.SelectorContext;
import org.apache.naming.ContextBindings;
import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;
public class javaURLContextFactory implements ObjectFactory, InitialContextFactory {
    public static final String MAIN = "initialContext";
    protected static volatile Context initialContext;
    @Override
    public Object getObjectInstance ( final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment ) throws NamingException {
        if ( ContextBindings.isThreadBound() || ContextBindings.isClassLoaderBound() ) {
            return new SelectorContext ( ( Hashtable<String, Object> ) environment );
        }
        return null;
    }
    @Override
    public Context getInitialContext ( final Hashtable<?, ?> environment ) throws NamingException {
        if ( ContextBindings.isThreadBound() || ContextBindings.isClassLoaderBound() ) {
            return new SelectorContext ( ( Hashtable<String, Object> ) environment, true );
        }
        if ( javaURLContextFactory.initialContext == null ) {
            synchronized ( javaURLContextFactory.class ) {
                if ( javaURLContextFactory.initialContext == null ) {
                    javaURLContextFactory.initialContext = new NamingContext ( ( Hashtable<String, Object> ) environment, "initialContext" );
                }
            }
        }
        return javaURLContextFactory.initialContext;
    }
    static {
        javaURLContextFactory.initialContext = null;
    }
}

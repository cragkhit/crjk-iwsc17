package org.apache.naming;
import org.apache.juli.logging.LogFactory;
import javax.naming.NameParser;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Name;
import java.util.Hashtable;
import org.apache.juli.logging.Log;
import javax.naming.Context;
public class SelectorContext implements Context {
    public static final String prefix = "java:";
    public static final int prefixLength;
    public static final String IC_PREFIX = "IC_";
    private static final Log log;
    protected final Hashtable<String, Object> env;
    protected static final StringManager sm;
    protected final boolean initialContext;
    public SelectorContext ( final Hashtable<String, Object> env ) {
        this.env = env;
        this.initialContext = false;
    }
    public SelectorContext ( final Hashtable<String, Object> env, final boolean initialContext ) {
        this.env = env;
        this.initialContext = initialContext;
    }
    @Override
    public Object lookup ( final Name name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingName", "lookup", name ) );
        }
        return this.getBoundContext().lookup ( this.parseName ( name ) );
    }
    @Override
    public Object lookup ( final String name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingString", "lookup", name ) );
        }
        return this.getBoundContext().lookup ( this.parseName ( name ) );
    }
    @Override
    public void bind ( final Name name, final Object obj ) throws NamingException {
        this.getBoundContext().bind ( this.parseName ( name ), obj );
    }
    @Override
    public void bind ( final String name, final Object obj ) throws NamingException {
        this.getBoundContext().bind ( this.parseName ( name ), obj );
    }
    @Override
    public void rebind ( final Name name, final Object obj ) throws NamingException {
        this.getBoundContext().rebind ( this.parseName ( name ), obj );
    }
    @Override
    public void rebind ( final String name, final Object obj ) throws NamingException {
        this.getBoundContext().rebind ( this.parseName ( name ), obj );
    }
    @Override
    public void unbind ( final Name name ) throws NamingException {
        this.getBoundContext().unbind ( this.parseName ( name ) );
    }
    @Override
    public void unbind ( final String name ) throws NamingException {
        this.getBoundContext().unbind ( this.parseName ( name ) );
    }
    @Override
    public void rename ( final Name oldName, final Name newName ) throws NamingException {
        this.getBoundContext().rename ( this.parseName ( oldName ), this.parseName ( newName ) );
    }
    @Override
    public void rename ( final String oldName, final String newName ) throws NamingException {
        this.getBoundContext().rename ( this.parseName ( oldName ), this.parseName ( newName ) );
    }
    @Override
    public NamingEnumeration<NameClassPair> list ( final Name name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingName", "list", name ) );
        }
        return this.getBoundContext().list ( this.parseName ( name ) );
    }
    @Override
    public NamingEnumeration<NameClassPair> list ( final String name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingString", "list", name ) );
        }
        return this.getBoundContext().list ( this.parseName ( name ) );
    }
    @Override
    public NamingEnumeration<Binding> listBindings ( final Name name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingName", "listBindings", name ) );
        }
        return this.getBoundContext().listBindings ( this.parseName ( name ) );
    }
    @Override
    public NamingEnumeration<Binding> listBindings ( final String name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingString", "listBindings", name ) );
        }
        return this.getBoundContext().listBindings ( this.parseName ( name ) );
    }
    @Override
    public void destroySubcontext ( final Name name ) throws NamingException {
        this.getBoundContext().destroySubcontext ( this.parseName ( name ) );
    }
    @Override
    public void destroySubcontext ( final String name ) throws NamingException {
        this.getBoundContext().destroySubcontext ( this.parseName ( name ) );
    }
    @Override
    public Context createSubcontext ( final Name name ) throws NamingException {
        return this.getBoundContext().createSubcontext ( this.parseName ( name ) );
    }
    @Override
    public Context createSubcontext ( final String name ) throws NamingException {
        return this.getBoundContext().createSubcontext ( this.parseName ( name ) );
    }
    @Override
    public Object lookupLink ( final Name name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingName", "lookupLink", name ) );
        }
        return this.getBoundContext().lookupLink ( this.parseName ( name ) );
    }
    @Override
    public Object lookupLink ( final String name ) throws NamingException {
        if ( SelectorContext.log.isDebugEnabled() ) {
            SelectorContext.log.debug ( SelectorContext.sm.getString ( "selectorContext.methodUsingString", "lookupLink", name ) );
        }
        return this.getBoundContext().lookupLink ( this.parseName ( name ) );
    }
    @Override
    public NameParser getNameParser ( final Name name ) throws NamingException {
        return this.getBoundContext().getNameParser ( this.parseName ( name ) );
    }
    @Override
    public NameParser getNameParser ( final String name ) throws NamingException {
        return this.getBoundContext().getNameParser ( this.parseName ( name ) );
    }
    @Override
    public Name composeName ( final Name name, final Name prefix ) throws NamingException {
        final Name prefixClone = ( Name ) prefix.clone();
        return prefixClone.addAll ( name );
    }
    @Override
    public String composeName ( final String name, final String prefix ) throws NamingException {
        return prefix + "/" + name;
    }
    @Override
    public Object addToEnvironment ( final String propName, final Object propVal ) throws NamingException {
        return this.getBoundContext().addToEnvironment ( propName, propVal );
    }
    @Override
    public Object removeFromEnvironment ( final String propName ) throws NamingException {
        return this.getBoundContext().removeFromEnvironment ( propName );
    }
    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return this.getBoundContext().getEnvironment();
    }
    @Override
    public void close() throws NamingException {
        this.getBoundContext().close();
    }
    @Override
    public String getNameInNamespace() throws NamingException {
        return "java:";
    }
    protected Context getBoundContext() throws NamingException {
        if ( this.initialContext ) {
            String ICName = "IC_";
            if ( ContextBindings.isThreadBound() ) {
                ICName += ContextBindings.getThreadName();
            } else if ( ContextBindings.isClassLoaderBound() ) {
                ICName += ContextBindings.getClassLoaderName();
            }
            Context initialContext = ContextBindings.getContext ( ICName );
            if ( initialContext == null ) {
                initialContext = new NamingContext ( this.env, ICName );
                ContextBindings.bindContext ( ICName, initialContext );
            }
            return initialContext;
        }
        if ( ContextBindings.isThreadBound() ) {
            return ContextBindings.getThread();
        }
        return ContextBindings.getClassLoader();
    }
    protected String parseName ( final String name ) throws NamingException {
        if ( !this.initialContext && name.startsWith ( "java:" ) ) {
            return name.substring ( SelectorContext.prefixLength );
        }
        if ( this.initialContext ) {
            return name;
        }
        throw new NamingException ( SelectorContext.sm.getString ( "selectorContext.noJavaUrl" ) );
    }
    protected Name parseName ( final Name name ) throws NamingException {
        if ( !this.initialContext && !name.isEmpty() && name.get ( 0 ).startsWith ( "java:" ) ) {
            if ( name.get ( 0 ).equals ( "java:" ) ) {
                return name.getSuffix ( 1 );
            }
            final Name result = name.getSuffix ( 1 );
            result.add ( 0, name.get ( 0 ).substring ( SelectorContext.prefixLength ) );
            return result;
        } else {
            if ( this.initialContext ) {
                return name;
            }
            throw new NamingException ( SelectorContext.sm.getString ( "selectorContext.noJavaUrl" ) );
        }
    }
    static {
        prefixLength = "java:".length();
        log = LogFactory.getLog ( SelectorContext.class );
        sm = StringManager.getManager ( SelectorContext.class );
    }
}

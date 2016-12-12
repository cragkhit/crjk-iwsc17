package org.apache.naming;
import org.apache.juli.logging.LogFactory;
import javax.naming.Referenceable;
import javax.naming.Reference;
import javax.naming.NameAlreadyBoundException;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.spi.NamingManager;
import javax.naming.OperationNotSupportedException;
import javax.naming.NotContextException;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NameNotFoundException;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Name;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import org.apache.juli.logging.Log;
import javax.naming.NameParser;
import javax.naming.Context;
public class NamingContext implements Context {
    protected static final NameParser nameParser;
    private static final Log log;
    protected final Hashtable<String, Object> env;
    protected static final StringManager sm;
    protected final HashMap<String, NamingEntry> bindings;
    protected final String name;
    private boolean exceptionOnFailedWrite;
    public NamingContext ( final Hashtable<String, Object> env, final String name ) {
        this ( env, name, new HashMap<String, NamingEntry>() );
    }
    public NamingContext ( final Hashtable<String, Object> env, final String name, final HashMap<String, NamingEntry> bindings ) {
        this.exceptionOnFailedWrite = true;
        this.env = new Hashtable<String, Object>();
        this.name = name;
        if ( env != null ) {
            final Enumeration<String> envEntries = env.keys();
            while ( envEntries.hasMoreElements() ) {
                final String entryName = envEntries.nextElement();
                this.addToEnvironment ( entryName, env.get ( entryName ) );
            }
        }
        this.bindings = bindings;
    }
    public boolean getExceptionOnFailedWrite() {
        return this.exceptionOnFailedWrite;
    }
    public void setExceptionOnFailedWrite ( final boolean exceptionOnFailedWrite ) {
        this.exceptionOnFailedWrite = exceptionOnFailedWrite;
    }
    @Override
    public Object lookup ( final Name name ) throws NamingException {
        return this.lookup ( name, true );
    }
    @Override
    public Object lookup ( final String name ) throws NamingException {
        return this.lookup ( new CompositeName ( name ), true );
    }
    @Override
    public void bind ( final Name name, final Object obj ) throws NamingException {
        this.bind ( name, obj, false );
    }
    @Override
    public void bind ( final String name, final Object obj ) throws NamingException {
        this.bind ( new CompositeName ( name ), obj );
    }
    @Override
    public void rebind ( final Name name, final Object obj ) throws NamingException {
        this.bind ( name, obj, true );
    }
    @Override
    public void rebind ( final String name, final Object obj ) throws NamingException {
        this.rebind ( new CompositeName ( name ), obj );
    }
    @Override
    public void unbind ( Name name ) throws NamingException {
        if ( !this.checkWritable() ) {
            return;
        }
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            throw new NamingException ( NamingContext.sm.getString ( "namingContext.invalidName" ) );
        }
        final NamingEntry entry = this.bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException ( NamingContext.sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( name.size() > 1 ) {
            if ( entry.type != 10 ) {
                throw new NamingException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
            }
            ( ( Context ) entry.value ).unbind ( name.getSuffix ( 1 ) );
        } else {
            this.bindings.remove ( name.get ( 0 ) );
        }
    }
    @Override
    public void unbind ( final String name ) throws NamingException {
        this.unbind ( new CompositeName ( name ) );
    }
    @Override
    public void rename ( final Name oldName, final Name newName ) throws NamingException {
        final Object value = this.lookup ( oldName );
        this.bind ( newName, value );
        this.unbind ( oldName );
    }
    @Override
    public void rename ( final String oldName, final String newName ) throws NamingException {
        this.rename ( new CompositeName ( oldName ), new CompositeName ( newName ) );
    }
    @Override
    public NamingEnumeration<NameClassPair> list ( Name name ) throws NamingException {
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return new NamingContextEnumeration ( this.bindings.values().iterator() );
        }
        final NamingEntry entry = this.bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException ( NamingContext.sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( entry.type != 10 ) {
            throw new NamingException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
        }
        return ( ( Context ) entry.value ).list ( name.getSuffix ( 1 ) );
    }
    @Override
    public NamingEnumeration<NameClassPair> list ( final String name ) throws NamingException {
        return this.list ( new CompositeName ( name ) );
    }
    @Override
    public NamingEnumeration<Binding> listBindings ( Name name ) throws NamingException {
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return new NamingContextBindingsEnumeration ( this.bindings.values().iterator(), this );
        }
        final NamingEntry entry = this.bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException ( NamingContext.sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( entry.type != 10 ) {
            throw new NamingException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
        }
        return ( ( Context ) entry.value ).listBindings ( name.getSuffix ( 1 ) );
    }
    @Override
    public NamingEnumeration<Binding> listBindings ( final String name ) throws NamingException {
        return this.listBindings ( new CompositeName ( name ) );
    }
    @Override
    public void destroySubcontext ( Name name ) throws NamingException {
        if ( !this.checkWritable() ) {
            return;
        }
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            throw new NamingException ( NamingContext.sm.getString ( "namingContext.invalidName" ) );
        }
        final NamingEntry entry = this.bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException ( NamingContext.sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( name.size() > 1 ) {
            if ( entry.type != 10 ) {
                throw new NamingException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
            }
            ( ( Context ) entry.value ).destroySubcontext ( name.getSuffix ( 1 ) );
        } else {
            if ( entry.type != 10 ) {
                throw new NotContextException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
            }
            ( ( Context ) entry.value ).close();
            this.bindings.remove ( name.get ( 0 ) );
        }
    }
    @Override
    public void destroySubcontext ( final String name ) throws NamingException {
        this.destroySubcontext ( new CompositeName ( name ) );
    }
    @Override
    public Context createSubcontext ( final Name name ) throws NamingException {
        if ( !this.checkWritable() ) {
            return null;
        }
        final NamingContext newContext = new NamingContext ( this.env, this.name );
        this.bind ( name, newContext );
        newContext.setExceptionOnFailedWrite ( this.getExceptionOnFailedWrite() );
        return newContext;
    }
    @Override
    public Context createSubcontext ( final String name ) throws NamingException {
        return this.createSubcontext ( new CompositeName ( name ) );
    }
    @Override
    public Object lookupLink ( final Name name ) throws NamingException {
        return this.lookup ( name, false );
    }
    @Override
    public Object lookupLink ( final String name ) throws NamingException {
        return this.lookup ( new CompositeName ( name ), false );
    }
    @Override
    public NameParser getNameParser ( Name name ) throws NamingException {
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return NamingContext.nameParser;
        }
        if ( name.size() <= 1 ) {
            return NamingContext.nameParser;
        }
        final Object obj = this.bindings.get ( name.get ( 0 ) );
        if ( obj instanceof Context ) {
            return ( ( Context ) obj ).getNameParser ( name.getSuffix ( 1 ) );
        }
        throw new NotContextException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
    }
    @Override
    public NameParser getNameParser ( final String name ) throws NamingException {
        return this.getNameParser ( new CompositeName ( name ) );
    }
    @Override
    public Name composeName ( final Name name, Name prefix ) throws NamingException {
        prefix = ( Name ) prefix.clone();
        return prefix.addAll ( name );
    }
    @Override
    public String composeName ( final String name, final String prefix ) {
        return prefix + "/" + name;
    }
    @Override
    public Object addToEnvironment ( final String propName, final Object propVal ) {
        return this.env.put ( propName, propVal );
    }
    @Override
    public Object removeFromEnvironment ( final String propName ) {
        return this.env.remove ( propName );
    }
    @Override
    public Hashtable<?, ?> getEnvironment() {
        return this.env;
    }
    @Override
    public void close() throws NamingException {
        if ( !this.checkWritable() ) {
            return;
        }
        this.env.clear();
    }
    @Override
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException ( NamingContext.sm.getString ( "namingContext.noAbsoluteName" ) );
    }
    protected Object lookup ( Name name, final boolean resolveLinks ) throws NamingException {
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return new NamingContext ( this.env, this.name, this.bindings );
        }
        final NamingEntry entry = this.bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException ( NamingContext.sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( name.size() > 1 ) {
            if ( entry.type != 10 ) {
                throw new NamingException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
            }
            return ( ( Context ) entry.value ).lookup ( name.getSuffix ( 1 ) );
        } else {
            if ( !resolveLinks || entry.type != 1 ) {
                if ( entry.type == 2 ) {
                    try {
                        final Object obj = NamingManager.getObjectInstance ( entry.value, name, this, this.env );
                        if ( entry.value instanceof ResourceRef ) {
                            final boolean singleton = Boolean.parseBoolean ( ( String ) ( ( ResourceRef ) entry.value ).get ( "singleton" ).getContent() );
                            if ( singleton ) {
                                entry.type = 0;
                                entry.value = obj;
                            }
                        }
                        return obj;
                    } catch ( NamingException e ) {
                        throw e;
                    } catch ( Exception e2 ) {
                        NamingContext.log.warn ( NamingContext.sm.getString ( "namingContext.failResolvingReference" ), e2 );
                        throw new NamingException ( e2.getMessage() );
                    }
                }
                return entry.value;
            }
            final String link = ( ( LinkRef ) entry.value ).getLinkName();
            if ( link.startsWith ( "." ) ) {
                return this.lookup ( link.substring ( 1 ) );
            }
            return new InitialContext ( this.env ).lookup ( link );
        }
    }
    protected void bind ( Name name, final Object obj, final boolean rebind ) throws NamingException {
        if ( !this.checkWritable() ) {
            return;
        }
        while ( !name.isEmpty() && name.get ( 0 ).length() == 0 ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            throw new NamingException ( NamingContext.sm.getString ( "namingContext.invalidName" ) );
        }
        NamingEntry entry = this.bindings.get ( name.get ( 0 ) );
        if ( name.size() > 1 ) {
            if ( entry == null ) {
                throw new NameNotFoundException ( NamingContext.sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
            }
            if ( entry.type != 10 ) {
                throw new NamingException ( NamingContext.sm.getString ( "namingContext.contextExpected" ) );
            }
            if ( rebind ) {
                ( ( Context ) entry.value ).rebind ( name.getSuffix ( 1 ), obj );
            } else {
                ( ( Context ) entry.value ).bind ( name.getSuffix ( 1 ), obj );
            }
        } else {
            if ( !rebind && entry != null ) {
                throw new NameAlreadyBoundException ( NamingContext.sm.getString ( "namingContext.alreadyBound", name.get ( 0 ) ) );
            }
            Object toBind = NamingManager.getStateToBind ( obj, name, this, this.env );
            if ( toBind instanceof Context ) {
                entry = new NamingEntry ( name.get ( 0 ), toBind, 10 );
            } else if ( toBind instanceof LinkRef ) {
                entry = new NamingEntry ( name.get ( 0 ), toBind, 1 );
            } else if ( toBind instanceof Reference ) {
                entry = new NamingEntry ( name.get ( 0 ), toBind, 2 );
            } else if ( toBind instanceof Referenceable ) {
                toBind = ( ( Referenceable ) toBind ).getReference();
                entry = new NamingEntry ( name.get ( 0 ), toBind, 2 );
            } else {
                entry = new NamingEntry ( name.get ( 0 ), toBind, 0 );
            }
            this.bindings.put ( name.get ( 0 ), entry );
        }
    }
    protected boolean isWritable() {
        return ContextAccessController.isWritable ( this.name );
    }
    protected boolean checkWritable() throws NamingException {
        if ( this.isWritable() ) {
            return true;
        }
        if ( this.exceptionOnFailedWrite ) {
            throw new OperationNotSupportedException ( NamingContext.sm.getString ( "namingContext.readOnly" ) );
        }
        return false;
    }
    static {
        nameParser = new NameParserImpl();
        log = LogFactory.getLog ( NamingContext.class );
        sm = StringManager.getManager ( NamingContext.class );
    }
}

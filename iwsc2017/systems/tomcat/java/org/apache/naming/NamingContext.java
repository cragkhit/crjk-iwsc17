package org.apache.naming;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class NamingContext implements Context {
    protected static final NameParser nameParser = new NameParserImpl();
    private static final Log log = LogFactory.getLog ( NamingContext.class );
    public NamingContext ( Hashtable<String, Object> env, String name ) {
        this ( env, name, new HashMap<String, NamingEntry>() );
    }
    public NamingContext ( Hashtable<String, Object> env, String name,
                           HashMap<String, NamingEntry> bindings ) {
        this.env = new Hashtable<>();
        this.name = name;
        if ( env != null ) {
            Enumeration<String> envEntries = env.keys();
            while ( envEntries.hasMoreElements() ) {
                String entryName = envEntries.nextElement();
                addToEnvironment ( entryName, env.get ( entryName ) );
            }
        }
        this.bindings = bindings;
    }
    protected final Hashtable<String, Object> env;
    protected static final StringManager sm = StringManager.getManager ( NamingContext.class );
    protected final HashMap<String, NamingEntry> bindings;
    protected final String name;
    private boolean exceptionOnFailedWrite = true;
    public boolean getExceptionOnFailedWrite() {
        return exceptionOnFailedWrite;
    }
    public void setExceptionOnFailedWrite ( boolean exceptionOnFailedWrite ) {
        this.exceptionOnFailedWrite = exceptionOnFailedWrite;
    }
    @Override
    public Object lookup ( Name name )
    throws NamingException {
        return lookup ( name, true );
    }
    @Override
    public Object lookup ( String name )
    throws NamingException {
        return lookup ( new CompositeName ( name ), true );
    }
    @Override
    public void bind ( Name name, Object obj )
    throws NamingException {
        bind ( name, obj, false );
    }
    @Override
    public void bind ( String name, Object obj )
    throws NamingException {
        bind ( new CompositeName ( name ), obj );
    }
    @Override
    public void rebind ( Name name, Object obj )
    throws NamingException {
        bind ( name, obj, true );
    }
    @Override
    public void rebind ( String name, Object obj )
    throws NamingException {
        rebind ( new CompositeName ( name ), obj );
    }
    @Override
    public void unbind ( Name name ) throws NamingException {
        if ( !checkWritable() ) {
            return;
        }
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() )
            throw new NamingException
            ( sm.getString ( "namingContext.invalidName" ) );
        NamingEntry entry = bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException
            ( sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( name.size() > 1 ) {
            if ( entry.type == NamingEntry.CONTEXT ) {
                ( ( Context ) entry.value ).unbind ( name.getSuffix ( 1 ) );
            } else {
                throw new NamingException
                ( sm.getString ( "namingContext.contextExpected" ) );
            }
        } else {
            bindings.remove ( name.get ( 0 ) );
        }
    }
    @Override
    public void unbind ( String name )
    throws NamingException {
        unbind ( new CompositeName ( name ) );
    }
    @Override
    public void rename ( Name oldName, Name newName )
    throws NamingException {
        Object value = lookup ( oldName );
        bind ( newName, value );
        unbind ( oldName );
    }
    @Override
    public void rename ( String oldName, String newName )
    throws NamingException {
        rename ( new CompositeName ( oldName ), new CompositeName ( newName ) );
    }
    @Override
    public NamingEnumeration<NameClassPair> list ( Name name )
    throws NamingException {
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return new NamingContextEnumeration ( bindings.values().iterator() );
        }
        NamingEntry entry = bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException
            ( sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( entry.type != NamingEntry.CONTEXT ) {
            throw new NamingException
            ( sm.getString ( "namingContext.contextExpected" ) );
        }
        return ( ( Context ) entry.value ).list ( name.getSuffix ( 1 ) );
    }
    @Override
    public NamingEnumeration<NameClassPair> list ( String name )
    throws NamingException {
        return list ( new CompositeName ( name ) );
    }
    @Override
    public NamingEnumeration<Binding> listBindings ( Name name )
    throws NamingException {
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return new NamingContextBindingsEnumeration ( bindings.values().iterator(), this );
        }
        NamingEntry entry = bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException
            ( sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( entry.type != NamingEntry.CONTEXT ) {
            throw new NamingException
            ( sm.getString ( "namingContext.contextExpected" ) );
        }
        return ( ( Context ) entry.value ).listBindings ( name.getSuffix ( 1 ) );
    }
    @Override
    public NamingEnumeration<Binding> listBindings ( String name )
    throws NamingException {
        return listBindings ( new CompositeName ( name ) );
    }
    @Override
    public void destroySubcontext ( Name name ) throws NamingException {
        if ( !checkWritable() ) {
            return;
        }
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() )
            throw new NamingException
            ( sm.getString ( "namingContext.invalidName" ) );
        NamingEntry entry = bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException
            ( sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( name.size() > 1 ) {
            if ( entry.type == NamingEntry.CONTEXT ) {
                ( ( Context ) entry.value ).destroySubcontext ( name.getSuffix ( 1 ) );
            } else {
                throw new NamingException
                ( sm.getString ( "namingContext.contextExpected" ) );
            }
        } else {
            if ( entry.type == NamingEntry.CONTEXT ) {
                ( ( Context ) entry.value ).close();
                bindings.remove ( name.get ( 0 ) );
            } else {
                throw new NotContextException
                ( sm.getString ( "namingContext.contextExpected" ) );
            }
        }
    }
    @Override
    public void destroySubcontext ( String name )
    throws NamingException {
        destroySubcontext ( new CompositeName ( name ) );
    }
    @Override
    public Context createSubcontext ( Name name ) throws NamingException {
        if ( !checkWritable() ) {
            return null;
        }
        NamingContext newContext = new NamingContext ( env, this.name );
        bind ( name, newContext );
        newContext.setExceptionOnFailedWrite ( getExceptionOnFailedWrite() );
        return newContext;
    }
    @Override
    public Context createSubcontext ( String name )
    throws NamingException {
        return createSubcontext ( new CompositeName ( name ) );
    }
    @Override
    public Object lookupLink ( Name name )
    throws NamingException {
        return lookup ( name, false );
    }
    @Override
    public Object lookupLink ( String name )
    throws NamingException {
        return lookup ( new CompositeName ( name ), false );
    }
    @Override
    public NameParser getNameParser ( Name name )
    throws NamingException {
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return nameParser;
        }
        if ( name.size() > 1 ) {
            Object obj = bindings.get ( name.get ( 0 ) );
            if ( obj instanceof Context ) {
                return ( ( Context ) obj ).getNameParser ( name.getSuffix ( 1 ) );
            } else {
                throw new NotContextException
                ( sm.getString ( "namingContext.contextExpected" ) );
            }
        }
        return nameParser;
    }
    @Override
    public NameParser getNameParser ( String name )
    throws NamingException {
        return getNameParser ( new CompositeName ( name ) );
    }
    @Override
    public Name composeName ( Name name, Name prefix ) throws NamingException {
        prefix = ( Name ) prefix.clone();
        return prefix.addAll ( name );
    }
    @Override
    public String composeName ( String name, String prefix ) {
        return prefix + "/" + name;
    }
    @Override
    public Object addToEnvironment ( String propName, Object propVal ) {
        return env.put ( propName, propVal );
    }
    @Override
    public Object removeFromEnvironment ( String propName ) {
        return env.remove ( propName );
    }
    @Override
    public Hashtable<?, ?> getEnvironment() {
        return env;
    }
    @Override
    public void close() throws NamingException {
        if ( !checkWritable() ) {
            return;
        }
        env.clear();
    }
    @Override
    public String getNameInNamespace()
    throws NamingException {
        throw  new OperationNotSupportedException
        ( sm.getString ( "namingContext.noAbsoluteName" ) );
    }
    protected Object lookup ( Name name, boolean resolveLinks )
    throws NamingException {
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() ) {
            return new NamingContext ( env, this.name, bindings );
        }
        NamingEntry entry = bindings.get ( name.get ( 0 ) );
        if ( entry == null ) {
            throw new NameNotFoundException
            ( sm.getString ( "namingContext.nameNotBound", name, name.get ( 0 ) ) );
        }
        if ( name.size() > 1 ) {
            if ( entry.type != NamingEntry.CONTEXT ) {
                throw new NamingException
                ( sm.getString ( "namingContext.contextExpected" ) );
            }
            return ( ( Context ) entry.value ).lookup ( name.getSuffix ( 1 ) );
        } else {
            if ( ( resolveLinks ) && ( entry.type == NamingEntry.LINK_REF ) ) {
                String link = ( ( LinkRef ) entry.value ).getLinkName();
                if ( link.startsWith ( "." ) ) {
                    return lookup ( link.substring ( 1 ) );
                } else {
                    return ( new InitialContext ( env ) ).lookup ( link );
                }
            } else if ( entry.type == NamingEntry.REFERENCE ) {
                try {
                    Object obj = NamingManager.getObjectInstance
                                 ( entry.value, name, this, env );
                    if ( entry.value instanceof ResourceRef ) {
                        boolean singleton = Boolean.parseBoolean (
                                                ( String ) ( ( ResourceRef ) entry.value ).get (
                                                    "singleton" ).getContent() );
                        if ( singleton ) {
                            entry.type = NamingEntry.ENTRY;
                            entry.value = obj;
                        }
                    }
                    return obj;
                } catch ( NamingException e ) {
                    throw e;
                } catch ( Exception e ) {
                    log.warn ( sm.getString
                               ( "namingContext.failResolvingReference" ), e );
                    throw new NamingException ( e.getMessage() );
                }
            } else {
                return entry.value;
            }
        }
    }
    protected void bind ( Name name, Object obj, boolean rebind )
    throws NamingException {
        if ( !checkWritable() ) {
            return;
        }
        while ( ( !name.isEmpty() ) && ( name.get ( 0 ).length() == 0 ) ) {
            name = name.getSuffix ( 1 );
        }
        if ( name.isEmpty() )
            throw new NamingException
            ( sm.getString ( "namingContext.invalidName" ) );
        NamingEntry entry = bindings.get ( name.get ( 0 ) );
        if ( name.size() > 1 ) {
            if ( entry == null ) {
                throw new NameNotFoundException ( sm.getString (
                                                      "namingContext.nameNotBound", name, name.get ( 0 ) ) );
            }
            if ( entry.type == NamingEntry.CONTEXT ) {
                if ( rebind ) {
                    ( ( Context ) entry.value ).rebind ( name.getSuffix ( 1 ), obj );
                } else {
                    ( ( Context ) entry.value ).bind ( name.getSuffix ( 1 ), obj );
                }
            } else {
                throw new NamingException
                ( sm.getString ( "namingContext.contextExpected" ) );
            }
        } else {
            if ( ( !rebind ) && ( entry != null ) ) {
                throw new NameAlreadyBoundException
                ( sm.getString ( "namingContext.alreadyBound", name.get ( 0 ) ) );
            } else {
                Object toBind =
                    NamingManager.getStateToBind ( obj, name, this, env );
                if ( toBind instanceof Context ) {
                    entry = new NamingEntry ( name.get ( 0 ), toBind,
                                              NamingEntry.CONTEXT );
                } else if ( toBind instanceof LinkRef ) {
                    entry = new NamingEntry ( name.get ( 0 ), toBind,
                                              NamingEntry.LINK_REF );
                } else if ( toBind instanceof Reference ) {
                    entry = new NamingEntry ( name.get ( 0 ), toBind,
                                              NamingEntry.REFERENCE );
                } else if ( toBind instanceof Referenceable ) {
                    toBind = ( ( Referenceable ) toBind ).getReference();
                    entry = new NamingEntry ( name.get ( 0 ), toBind,
                                              NamingEntry.REFERENCE );
                } else {
                    entry = new NamingEntry ( name.get ( 0 ), toBind,
                                              NamingEntry.ENTRY );
                }
                bindings.put ( name.get ( 0 ), entry );
            }
        }
    }
    protected boolean isWritable() {
        return ContextAccessController.isWritable ( name );
    }
    protected boolean checkWritable() throws NamingException {
        if ( isWritable() ) {
            return true;
        } else {
            if ( exceptionOnFailedWrite ) {
                throw new javax.naming.OperationNotSupportedException (
                    sm.getString ( "namingContext.readOnly" ) );
            }
        }
        return false;
    }
}

package org.apache.naming;
import java.util.Iterator;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
public class NamingContextBindingsEnumeration
    implements NamingEnumeration<Binding> {
    public NamingContextBindingsEnumeration ( Iterator<NamingEntry> entries,
            Context ctx ) {
        iterator = entries;
        this.ctx = ctx;
    }
    protected final Iterator<NamingEntry> iterator;
    private final Context ctx;
    @Override
    public Binding next()
    throws NamingException {
        return nextElementInternal();
    }
    @Override
    public boolean hasMore()
    throws NamingException {
        return iterator.hasNext();
    }
    @Override
    public void close()
    throws NamingException {
    }
    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }
    @Override
    public Binding nextElement() {
        try {
            return nextElementInternal();
        } catch ( NamingException e ) {
            throw new RuntimeException ( e.getMessage(), e );
        }
    }
    private Binding nextElementInternal() throws NamingException {
        NamingEntry entry = iterator.next();
        Object value;
        if ( entry.type == NamingEntry.REFERENCE
                || entry.type == NamingEntry.LINK_REF ) {
            try {
                value = ctx.lookup ( new CompositeName ( entry.name ) );
            } catch ( NamingException e ) {
                throw e;
            } catch ( Exception e ) {
                NamingException ne = new NamingException ( e.getMessage() );
                ne.initCause ( e );
                throw ne;
            }
        } else {
            value = entry.value;
        }
        return new Binding ( entry.name, value.getClass().getName(), value, true );
    }
}

package org.apache.naming;
import javax.naming.Name;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Context;
import java.util.Iterator;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
public class NamingContextBindingsEnumeration implements NamingEnumeration<Binding> {
    protected final Iterator<NamingEntry> iterator;
    private final Context ctx;
    public NamingContextBindingsEnumeration ( final Iterator<NamingEntry> entries, final Context ctx ) {
        this.iterator = entries;
        this.ctx = ctx;
    }
    @Override
    public Binding next() throws NamingException {
        return this.nextElementInternal();
    }
    @Override
    public boolean hasMore() throws NamingException {
        return this.iterator.hasNext();
    }
    @Override
    public void close() throws NamingException {
    }
    @Override
    public boolean hasMoreElements() {
        return this.iterator.hasNext();
    }
    @Override
    public Binding nextElement() {
        try {
            return this.nextElementInternal();
        } catch ( NamingException e ) {
            throw new RuntimeException ( e.getMessage(), e );
        }
    }
    private Binding nextElementInternal() throws NamingException {
        final NamingEntry entry = this.iterator.next();
        Label_0080: {
            if ( entry.type != 2 ) {
                if ( entry.type != 1 ) {
                    break Label_0080;
                }
            }
            try {
                final Object value = this.ctx.lookup ( new CompositeName ( entry.name ) );
                return new Binding ( entry.name, value.getClass().getName(), value, true );
            } catch ( NamingException e ) {
                throw e;
            } catch ( Exception e2 ) {
                final NamingException ne = new NamingException ( e2.getMessage() );
                ne.initCause ( e2 );
                throw ne;
            }
        }
        final Object value = entry.value;
        return new Binding ( entry.name, value.getClass().getName(), value, true );
    }
}

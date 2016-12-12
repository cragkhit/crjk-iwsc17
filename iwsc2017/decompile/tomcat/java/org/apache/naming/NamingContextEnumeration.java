package org.apache.naming;
import javax.naming.NamingException;
import java.util.Iterator;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
public class NamingContextEnumeration implements NamingEnumeration<NameClassPair> {
    protected final Iterator<NamingEntry> iterator;
    public NamingContextEnumeration ( final Iterator<NamingEntry> entries ) {
        this.iterator = entries;
    }
    @Override
    public NameClassPair next() throws NamingException {
        return this.nextElement();
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
    public NameClassPair nextElement() {
        final NamingEntry entry = this.iterator.next();
        return new NameClassPair ( entry.name, entry.value.getClass().getName() );
    }
}

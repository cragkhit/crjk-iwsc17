package org.apache.naming;
import java.util.Iterator;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
public class NamingContextEnumeration
    implements NamingEnumeration<NameClassPair> {
    public NamingContextEnumeration ( Iterator<NamingEntry> entries ) {
        iterator = entries;
    }
    protected final Iterator<NamingEntry> iterator;
    @Override
    public NameClassPair next()
    throws NamingException {
        return nextElement();
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
    public NameClassPair nextElement() {
        NamingEntry entry = iterator.next();
        return new NameClassPair ( entry.name, entry.value.getClass().getName() );
    }
}

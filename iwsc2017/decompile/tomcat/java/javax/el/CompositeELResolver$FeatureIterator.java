package javax.el;
import java.util.NoSuchElementException;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
private static final class FeatureIterator implements Iterator<FeatureDescriptor> {
    private final ELContext context;
    private final Object base;
    private final ELResolver[] resolvers;
    private final int size;
    private Iterator<FeatureDescriptor> itr;
    private int idx;
    private FeatureDescriptor next;
    public FeatureIterator ( final ELContext context, final Object base, final ELResolver[] resolvers, final int size ) {
        this.context = context;
        this.base = base;
        this.resolvers = resolvers;
        this.size = size;
        this.idx = 0;
        this.guaranteeIterator();
    }
    private void guaranteeIterator() {
        while ( this.itr == null && this.idx < this.size ) {
            this.itr = this.resolvers[this.idx].getFeatureDescriptors ( this.context, this.base );
            ++this.idx;
        }
    }
    @Override
    public boolean hasNext() {
        if ( this.next != null ) {
            return true;
        }
        if ( this.itr != null ) {
            while ( this.next == null && this.itr.hasNext() ) {
                this.next = this.itr.next();
            }
            if ( this.next == null ) {
                this.itr = null;
                this.guaranteeIterator();
            }
            return this.hasNext();
        }
        return false;
    }
    @Override
    public FeatureDescriptor next() {
        if ( !this.hasNext() ) {
            throw new NoSuchElementException();
        }
        final FeatureDescriptor result = this.next;
        this.next = null;
        return result;
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

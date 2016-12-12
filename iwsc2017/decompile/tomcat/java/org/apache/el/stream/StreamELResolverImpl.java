package org.apache.el.stream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
public class StreamELResolverImpl extends ELResolver {
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        return null;
    }
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        return null;
    }
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
    }
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        return false;
    }
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return null;
    }
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        return null;
    }
    public Object invoke ( final ELContext context, final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params ) {
        if ( "stream".equals ( method ) && params.length == 0 ) {
            if ( base.getClass().isArray() ) {
                context.setPropertyResolved ( true );
                return new Stream ( new ArrayIterator ( base ) );
            }
            if ( base instanceof Collection ) {
                context.setPropertyResolved ( true );
                final Collection<Object> collection = ( Collection<Object> ) base;
                return new Stream ( collection.iterator() );
            }
        }
        return null;
    }
    private static class ArrayIterator implements Iterator<Object> {
        private final Object base;
        private final int size;
        private int index;
        public ArrayIterator ( final Object base ) {
            this.index = 0;
            this.base = base;
            this.size = Array.getLength ( base );
        }
        @Override
        public boolean hasNext() {
            return this.size > this.index;
        }
        @Override
        public Object next() {
            return Array.get ( this.base, this.index++ );
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

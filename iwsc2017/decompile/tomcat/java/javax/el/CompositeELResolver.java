package javax.el;
import java.util.NoSuchElementException;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Objects;
public class CompositeELResolver extends ELResolver {
    private static final Class<?> SCOPED_ATTRIBUTE_EL_RESOLVER;
    private int size;
    private ELResolver[] resolvers;
    public CompositeELResolver() {
        this.size = 0;
        this.resolvers = new ELResolver[8];
    }
    public void add ( final ELResolver elResolver ) {
        Objects.requireNonNull ( elResolver );
        if ( this.size >= this.resolvers.length ) {
            final ELResolver[] nr = new ELResolver[this.size * 2];
            System.arraycopy ( this.resolvers, 0, nr, 0, this.size );
            this.resolvers = nr;
        }
        this.resolvers[this.size++] = elResolver;
    }
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        context.setPropertyResolved ( false );
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            final Object result = this.resolvers[i].getValue ( context, base, property );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        return null;
    }
    @Override
    public Object invoke ( final ELContext context, final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params ) {
        context.setPropertyResolved ( false );
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            final Object obj = this.resolvers[i].invoke ( context, base, method, paramTypes, params );
            if ( context.isPropertyResolved() ) {
                return obj;
            }
        }
        return null;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        context.setPropertyResolved ( false );
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            final Class<?> type = this.resolvers[i].getType ( context, base, property );
            if ( context.isPropertyResolved() ) {
                if ( CompositeELResolver.SCOPED_ATTRIBUTE_EL_RESOLVER != null && CompositeELResolver.SCOPED_ATTRIBUTE_EL_RESOLVER.isAssignableFrom ( this.resolvers[i].getClass() ) ) {
                    final Object value = this.resolvers[i].getValue ( context, base, property );
                    if ( value != null ) {
                        return value.getClass();
                    }
                }
                return type;
            }
        }
        return null;
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        context.setPropertyResolved ( false );
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            this.resolvers[i].setValue ( context, base, property, value );
            if ( context.isPropertyResolved() ) {
                return;
            }
        }
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        context.setPropertyResolved ( false );
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            final boolean readOnly = this.resolvers[i].isReadOnly ( context, base, property );
            if ( context.isPropertyResolved() ) {
                return readOnly;
            }
        }
        return false;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return new FeatureIterator ( context, base, this.resolvers, this.size );
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        Class<?> commonType = null;
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            final Class<?> type = this.resolvers[i].getCommonPropertyType ( context, base );
            if ( type != null && ( commonType == null || commonType.isAssignableFrom ( type ) ) ) {
                commonType = type;
            }
        }
        return commonType;
    }
    @Override
    public Object convertToType ( final ELContext context, final Object obj, final Class<?> type ) {
        context.setPropertyResolved ( false );
        for ( int sz = this.size, i = 0; i < sz; ++i ) {
            final Object result = this.resolvers[i].convertToType ( context, obj, type );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        return null;
    }
    static {
        Class<?> clazz = null;
        try {
            clazz = Class.forName ( "javax.servlet.jsp.el.ScopedAttributeELResolver" );
        } catch ( ClassNotFoundException ex ) {}
        SCOPED_ATTRIBUTE_EL_RESOLVER = clazz;
    }
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
}

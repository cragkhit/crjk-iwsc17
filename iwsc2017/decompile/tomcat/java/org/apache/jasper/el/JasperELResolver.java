package org.apache.jasper.el;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.ELContext;
import java.util.Iterator;
import javax.servlet.jsp.el.ScopedAttributeELResolver;
import javax.el.BeanELResolver;
import javax.el.ArrayELResolver;
import javax.el.ListELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.MapELResolver;
import javax.el.StaticFieldELResolver;
import javax.servlet.jsp.el.ImplicitObjectELResolver;
import java.util.List;
import javax.el.ELResolver;
import javax.el.CompositeELResolver;
public class JasperELResolver extends CompositeELResolver {
    private static final int STANDARD_RESOLVERS_COUNT = 9;
    private int size;
    private ELResolver[] resolvers;
    private final int appResolversSize;
    public JasperELResolver ( final List<ELResolver> appResolvers, final ELResolver streamResolver ) {
        this.appResolversSize = appResolvers.size();
        this.resolvers = new ELResolver[this.appResolversSize + 9];
        this.size = 0;
        this.add ( ( ELResolver ) new ImplicitObjectELResolver() );
        for ( final ELResolver appResolver : appResolvers ) {
            this.add ( appResolver );
        }
        this.add ( streamResolver );
        this.add ( ( ELResolver ) new StaticFieldELResolver() );
        this.add ( ( ELResolver ) new MapELResolver() );
        this.add ( ( ELResolver ) new ResourceBundleELResolver() );
        this.add ( ( ELResolver ) new ListELResolver() );
        this.add ( ( ELResolver ) new ArrayELResolver() );
        this.add ( ( ELResolver ) new BeanELResolver() );
        this.add ( ( ELResolver ) new ScopedAttributeELResolver() );
    }
    public synchronized void add ( final ELResolver elResolver ) {
        super.add ( elResolver );
        if ( this.resolvers.length > this.size ) {
            this.resolvers[this.size] = elResolver;
        } else {
            final ELResolver[] nr = new ELResolver[this.size + 1];
            System.arraycopy ( this.resolvers, 0, nr, 0, this.size );
            nr[this.size] = elResolver;
            this.resolvers = nr;
        }
        ++this.size;
    }
    public Object getValue ( final ELContext context, final Object base, final Object property ) throws NullPointerException, PropertyNotFoundException, ELException {
        context.setPropertyResolved ( false );
        Object result = null;
        int start;
        if ( base == null ) {
            final int index = 1 + this.appResolversSize;
            for ( int i = 0; i < index; ++i ) {
                result = this.resolvers[i].getValue ( context, base, property );
                if ( context.isPropertyResolved() ) {
                    return result;
                }
            }
            start = index + 7;
        } else {
            start = 1;
        }
        for ( int j = start; j < this.size; ++j ) {
            result = this.resolvers[j].getValue ( context, base, property );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        return null;
    }
    public Object invoke ( final ELContext context, final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params ) {
        final String targetMethod = coerceToString ( method );
        if ( targetMethod.length() == 0 ) {
            throw new ELException ( ( Throwable ) new NoSuchMethodException() );
        }
        context.setPropertyResolved ( false );
        Object result = null;
        int index = 1 + this.appResolversSize + 2;
        for ( int i = 1; i < index; ++i ) {
            result = this.resolvers[i].invoke ( context, base, ( Object ) targetMethod, ( Class[] ) paramTypes, params );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        index += 4;
        for ( int i = index; i < this.size; ++i ) {
            result = this.resolvers[i].invoke ( context, base, ( Object ) targetMethod, ( Class[] ) paramTypes, params );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        return null;
    }
    private static final String coerceToString ( final Object obj ) {
        if ( obj == null ) {
            return "";
        }
        if ( obj instanceof String ) {
            return ( String ) obj;
        }
        if ( obj instanceof Enum ) {
            return ( ( Enum ) obj ).name();
        }
        return obj.toString();
    }
}

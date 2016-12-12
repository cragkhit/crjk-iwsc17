package org.apache.jasper.el;
import java.util.List;
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.ResourceBundleELResolver;
import javax.el.StaticFieldELResolver;
import javax.servlet.jsp.el.ImplicitObjectELResolver;
import javax.servlet.jsp.el.ScopedAttributeELResolver;
public class JasperELResolver extends CompositeELResolver {
    private static final int STANDARD_RESOLVERS_COUNT = 9;
    private int size;
    private ELResolver[] resolvers;
    private final int appResolversSize;
    public JasperELResolver ( List<ELResolver> appResolvers,
                              ELResolver streamResolver ) {
        appResolversSize = appResolvers.size();
        resolvers = new ELResolver[appResolversSize + STANDARD_RESOLVERS_COUNT];
        size = 0;
        add ( new ImplicitObjectELResolver() );
        for ( ELResolver appResolver : appResolvers ) {
            add ( appResolver );
        }
        add ( streamResolver );
        add ( new StaticFieldELResolver() );
        add ( new MapELResolver() );
        add ( new ResourceBundleELResolver() );
        add ( new ListELResolver() );
        add ( new ArrayELResolver() );
        add ( new BeanELResolver() );
        add ( new ScopedAttributeELResolver() );
    }
    @Override
    public synchronized void add ( ELResolver elResolver ) {
        super.add ( elResolver );
        if ( resolvers.length > size ) {
            resolvers[size] = elResolver;
        } else {
            ELResolver[] nr = new ELResolver[size + 1];
            System.arraycopy ( resolvers, 0, nr, 0, size );
            nr[size] = elResolver;
            resolvers = nr;
        }
        size ++;
    }
    @Override
    public Object getValue ( ELContext context, Object base, Object property )
    throws NullPointerException, PropertyNotFoundException, ELException {
        context.setPropertyResolved ( false );
        int start;
        Object result = null;
        if ( base == null ) {
            int index = 1   + appResolversSize;
            for ( int i = 0; i < index; i++ ) {
                result = resolvers[i].getValue ( context, base, property );
                if ( context.isPropertyResolved() ) {
                    return result;
                }
            }
            start = index + 7;
        } else {
            start = 1;
        }
        for ( int i = start; i < size; i++ ) {
            result = resolvers[i].getValue ( context, base, property );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        return null;
    }
    @Override
    public Object invoke ( ELContext context, Object base, Object method,
                           Class<?>[] paramTypes, Object[] params ) {
        String targetMethod = coerceToString ( method );
        if ( targetMethod.length() == 0 ) {
            throw new ELException ( new NoSuchMethodException() );
        }
        context.setPropertyResolved ( false );
        Object result = null;
        int index = 1   + appResolversSize +
                    2  ;
        for ( int i = 1; i < index; i++ ) {
            result = resolvers[i].invoke (
                         context, base, targetMethod, paramTypes, params );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        index += 4;
        for ( int i = index; i < size; i++ ) {
            result = resolvers[i].invoke (
                         context, base, targetMethod, paramTypes, params );
            if ( context.isPropertyResolved() ) {
                return result;
            }
        }
        return null;
    }
    private static final String coerceToString ( final Object obj ) {
        if ( obj == null ) {
            return "";
        } else if ( obj instanceof String ) {
            return ( String ) obj;
        } else if ( obj instanceof Enum<?> ) {
            return ( ( Enum<?> ) obj ).name();
        } else {
            return obj.toString();
        }
    }
}

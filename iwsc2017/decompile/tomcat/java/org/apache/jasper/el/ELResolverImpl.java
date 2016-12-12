package org.apache.jasper.el;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.PropertyNotWritableException;
import javax.el.ELException;
import java.util.Objects;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.el.VariableResolver;
import javax.el.ELResolver;
@Deprecated
public final class ELResolverImpl extends ELResolver {
    private final VariableResolver variableResolver;
    private final ELResolver elResolver;
    public ELResolverImpl ( final VariableResolver variableResolver, final ExpressionFactory factory ) {
        this.variableResolver = variableResolver;
        this.elResolver = ELContextImpl.getDefaultResolver ( factory );
    }
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null ) {
            context.setPropertyResolved ( base, property );
            if ( property != null ) {
                try {
                    return this.variableResolver.resolveVariable ( property.toString() );
                } catch ( javax.servlet.jsp.el.ELException e ) {
                    throw new ELException ( e.getMessage(), e.getCause() );
                }
            }
        }
        if ( !context.isPropertyResolved() ) {
            return this.elResolver.getValue ( context, base, property );
        }
        return null;
    }
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null ) {
            context.setPropertyResolved ( base, property );
            if ( property != null ) {
                try {
                    final Object obj = this.variableResolver.resolveVariable ( property.toString() );
                    return ( obj != null ) ? obj.getClass() : null;
                } catch ( javax.servlet.jsp.el.ELException e ) {
                    throw new ELException ( e.getMessage(), e.getCause() );
                }
            }
        }
        if ( !context.isPropertyResolved() ) {
            return ( Class<?> ) this.elResolver.getType ( context, base, property );
        }
        return null;
    }
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base == null ) {
            context.setPropertyResolved ( base, property );
            throw new PropertyNotWritableException ( "Legacy VariableResolver wrapped, not writable" );
        }
        if ( !context.isPropertyResolved() ) {
            this.elResolver.setValue ( context, base, property, value );
        }
    }
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null ) {
            context.setPropertyResolved ( base, property );
            return true;
        }
        return this.elResolver.isReadOnly ( context, base, property );
    }
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return ( Iterator<FeatureDescriptor> ) this.elResolver.getFeatureDescriptors ( context, base );
    }
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base == null ) {
            return String.class;
        }
        return ( Class<?> ) this.elResolver.getCommonPropertyType ( context, base );
    }
}

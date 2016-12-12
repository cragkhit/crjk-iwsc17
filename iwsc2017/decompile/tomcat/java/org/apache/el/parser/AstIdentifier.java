package org.apache.el.parser;
import javax.el.MethodNotFoundException;
import javax.el.MethodExpression;
import javax.el.ValueReference;
import org.apache.el.util.Validation;
import javax.el.MethodInfo;
import javax.el.ELClass;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.el.PropertyNotFoundException;
import org.apache.el.util.MessageFactory;
import javax.el.ELContext;
import org.apache.el.lang.EvaluationContext;
public final class AstIdentifier extends SimpleNode {
    public AstIdentifier ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        final VariableMapper varMapper = ctx.getVariableMapper();
        if ( varMapper != null ) {
            final ValueExpression expr = varMapper.resolveVariable ( this.image );
            if ( expr != null ) {
                return ( Class<?> ) expr.getType ( ctx.getELContext() );
            }
        }
        ctx.setPropertyResolved ( false );
        final Class<?> result = ( Class<?> ) ctx.getELResolver().getType ( ( ELContext ) ctx, ( Object ) null, ( Object ) this.image );
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled.null", this.image ) );
        }
        return result;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        if ( ctx.isLambdaArgument ( this.image ) ) {
            return ctx.getLambdaArgument ( this.image );
        }
        final VariableMapper varMapper = ctx.getVariableMapper();
        if ( varMapper != null ) {
            final ValueExpression expr = varMapper.resolveVariable ( this.image );
            if ( expr != null ) {
                return expr.getValue ( ctx.getELContext() );
            }
        }
        ctx.setPropertyResolved ( false );
        if ( this.parent instanceof AstValue ) {
            ctx.putContext ( this.getClass(), Boolean.FALSE );
        } else {
            ctx.putContext ( this.getClass(), Boolean.TRUE );
        }
        Object result;
        try {
            result = ctx.getELResolver().getValue ( ( ELContext ) ctx, ( Object ) null, ( Object ) this.image );
        } finally {
            ctx.putContext ( this.getClass(), Boolean.FALSE );
        }
        if ( ctx.isPropertyResolved() ) {
            return result;
        }
        result = ctx.getImportHandler().resolveClass ( this.image );
        if ( result != null ) {
            return new ELClass ( ( Class ) result );
        }
        result = ctx.getImportHandler().resolveStatic ( this.image );
        if ( result != null ) {
            try {
                return ( ( Class ) result ).getField ( this.image ).get ( null );
            } catch ( IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e ) {
                throw new ELException ( ( Throwable ) e );
            }
        }
        throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled.null", this.image ) );
    }
    @Override
    public boolean isReadOnly ( final EvaluationContext ctx ) throws ELException {
        final VariableMapper varMapper = ctx.getVariableMapper();
        if ( varMapper != null ) {
            final ValueExpression expr = varMapper.resolveVariable ( this.image );
            if ( expr != null ) {
                return expr.isReadOnly ( ctx.getELContext() );
            }
        }
        ctx.setPropertyResolved ( false );
        final boolean result = ctx.getELResolver().isReadOnly ( ( ELContext ) ctx, ( Object ) null, ( Object ) this.image );
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled.null", this.image ) );
        }
        return result;
    }
    @Override
    public void setValue ( final EvaluationContext ctx, final Object value ) throws ELException {
        final VariableMapper varMapper = ctx.getVariableMapper();
        if ( varMapper != null ) {
            final ValueExpression expr = varMapper.resolveVariable ( this.image );
            if ( expr != null ) {
                expr.setValue ( ctx.getELContext(), value );
                return;
            }
        }
        ctx.setPropertyResolved ( false );
        ctx.getELResolver().setValue ( ( ELContext ) ctx, ( Object ) null, ( Object ) this.image, value );
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled.null", this.image ) );
        }
    }
    @Override
    public Object invoke ( final EvaluationContext ctx, final Class<?>[] paramTypes, final Object[] paramValues ) throws ELException {
        return this.getMethodExpression ( ctx ).invoke ( ctx.getELContext(), paramValues );
    }
    @Override
    public MethodInfo getMethodInfo ( final EvaluationContext ctx, final Class<?>[] paramTypes ) throws ELException {
        return this.getMethodExpression ( ctx ).getMethodInfo ( ctx.getELContext() );
    }
    @Override
    public void setImage ( final String image ) {
        if ( !Validation.isIdentifier ( image ) ) {
            throw new ELException ( MessageFactory.get ( "error.identifier.notjava", image ) );
        }
        this.image = image;
    }
    @Override
    public ValueReference getValueReference ( final EvaluationContext ctx ) {
        final VariableMapper varMapper = ctx.getVariableMapper();
        if ( varMapper == null ) {
            return null;
        }
        final ValueExpression expr = varMapper.resolveVariable ( this.image );
        if ( expr == null ) {
            return null;
        }
        return expr.getValueReference ( ( ELContext ) ctx );
    }
    private final MethodExpression getMethodExpression ( final EvaluationContext ctx ) throws ELException {
        Object obj = null;
        final VariableMapper varMapper = ctx.getVariableMapper();
        ValueExpression ve = null;
        if ( varMapper != null ) {
            ve = varMapper.resolveVariable ( this.image );
            if ( ve != null ) {
                obj = ve.getValue ( ( ELContext ) ctx );
            }
        }
        if ( ve == null ) {
            ctx.setPropertyResolved ( false );
            obj = ctx.getELResolver().getValue ( ( ELContext ) ctx, ( Object ) null, ( Object ) this.image );
        }
        if ( obj instanceof MethodExpression ) {
            return ( MethodExpression ) obj;
        }
        if ( obj == null ) {
            throw new MethodNotFoundException ( "Identity '" + this.image + "' was null and was unable to invoke" );
        }
        throw new ELException ( "Identity '" + this.image + "' does not reference a MethodExpression instance, returned type: " + obj.getClass().getName() );
    }
}

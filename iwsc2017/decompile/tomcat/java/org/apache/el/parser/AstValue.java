package org.apache.el.parser;
import javax.el.ValueReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.el.util.ReflectionUtil;
import javax.el.MethodInfo;
import org.apache.el.lang.ELSupport;
import javax.el.LambdaExpression;
import org.apache.el.stream.Optional;
import javax.el.ELResolver;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import org.apache.el.util.MessageFactory;
import javax.el.ELContext;
import org.apache.el.lang.EvaluationContext;
public final class AstValue extends SimpleNode {
    private static final Object[] EMPTY_ARRAY;
    public AstValue ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        final Target t = this.getTarget ( ctx );
        ctx.setPropertyResolved ( false );
        final Class<?> result = ( Class<?> ) ctx.getELResolver().getType ( ( ELContext ) ctx, t.base, t.property );
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled", t.base, t.property ) );
        }
        return result;
    }
    private final Target getTarget ( final EvaluationContext ctx ) throws ELException {
        Object base = this.children[0].getValue ( ctx );
        if ( base == null ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.unreachable.base", this.children[0].getImage() ) );
        }
        Object property = null;
        final int propCount = this.jjtGetNumChildren();
        int i = 1;
        final ELResolver resolver = ctx.getELResolver();
        while ( i < propCount ) {
            if ( i + 2 < propCount && this.children[i + 1] instanceof AstMethodParameters ) {
                base = resolver.invoke ( ( ELContext ) ctx, base, this.children[i].getValue ( ctx ), ( Class[] ) null, ( ( AstMethodParameters ) this.children[i + 1] ).getParameters ( ctx ) );
                i += 2;
            } else if ( i + 2 == propCount && this.children[i + 1] instanceof AstMethodParameters ) {
                ctx.setPropertyResolved ( false );
                property = this.children[i].getValue ( ctx );
                i += 2;
                if ( property == null ) {
                    throw new PropertyNotFoundException ( MessageFactory.get ( "error.unreachable.property", property ) );
                }
            } else if ( i + 1 < propCount ) {
                property = this.children[i].getValue ( ctx );
                ctx.setPropertyResolved ( false );
                base = resolver.getValue ( ( ELContext ) ctx, base, property );
                ++i;
            } else {
                ctx.setPropertyResolved ( false );
                property = this.children[i].getValue ( ctx );
                ++i;
                if ( property == null ) {
                    throw new PropertyNotFoundException ( MessageFactory.get ( "error.unreachable.property", property ) );
                }
            }
            if ( base == null ) {
                throw new PropertyNotFoundException ( MessageFactory.get ( "error.unreachable.property", property ) );
            }
        }
        final Target t = new Target();
        t.base = base;
        t.property = property;
        return t;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        Object base = this.children[0].getValue ( ctx );
        final int propCount = this.jjtGetNumChildren();
        int i = 1;
        Object suffix = null;
        final ELResolver resolver = ctx.getELResolver();
        while ( base != null && i < propCount ) {
            suffix = this.children[i].getValue ( ctx );
            if ( i + 1 < propCount && this.children[i + 1] instanceof AstMethodParameters ) {
                final AstMethodParameters mps = ( AstMethodParameters ) this.children[i + 1];
                if ( base instanceof Optional && "orElseGet".equals ( suffix ) && mps.jjtGetNumChildren() == 1 ) {
                    final Node paramFoOptional = mps.jjtGetChild ( 0 );
                    if ( ! ( paramFoOptional instanceof AstLambdaExpression ) && ! ( paramFoOptional instanceof LambdaExpression ) ) {
                        throw new ELException ( MessageFactory.get ( "stream.optional.paramNotLambda", suffix ) );
                    }
                }
                final Object[] paramValues = mps.getParameters ( ctx );
                base = resolver.invoke ( ( ELContext ) ctx, base, suffix, ( Class[] ) this.getTypesFromValues ( paramValues ), paramValues );
                i += 2;
            } else {
                if ( suffix == null ) {
                    return null;
                }
                ctx.setPropertyResolved ( false );
                base = resolver.getValue ( ( ELContext ) ctx, base, suffix );
                ++i;
            }
        }
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled", base, suffix ) );
        }
        return base;
    }
    @Override
    public boolean isReadOnly ( final EvaluationContext ctx ) throws ELException {
        final Target t = this.getTarget ( ctx );
        ctx.setPropertyResolved ( false );
        final boolean result = ctx.getELResolver().isReadOnly ( ( ELContext ) ctx, t.base, t.property );
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled", t.base, t.property ) );
        }
        return result;
    }
    @Override
    public void setValue ( final EvaluationContext ctx, final Object value ) throws ELException {
        final Target t = this.getTarget ( ctx );
        ctx.setPropertyResolved ( false );
        final ELResolver resolver = ctx.getELResolver();
        final Class<?> targetClass = ( Class<?> ) resolver.getType ( ( ELContext ) ctx, t.base, t.property );
        resolver.setValue ( ( ELContext ) ctx, t.base, t.property, ELSupport.coerceToType ( ctx, value, targetClass ) );
        if ( !ctx.isPropertyResolved() ) {
            throw new PropertyNotFoundException ( MessageFactory.get ( "error.resolver.unhandled", t.base, t.property ) );
        }
    }
    @Override
    public MethodInfo getMethodInfo ( final EvaluationContext ctx, final Class[] paramTypes ) throws ELException {
        final Target t = this.getTarget ( ctx );
        final Method m = ReflectionUtil.getMethod ( ctx, t.base, t.property, paramTypes, null );
        return new MethodInfo ( m.getName(), ( Class ) m.getReturnType(), ( Class[] ) m.getParameterTypes() );
    }
    @Override
    public Object invoke ( final EvaluationContext ctx, final Class[] paramTypes, final Object[] paramValues ) throws ELException {
        final Target t = this.getTarget ( ctx );
        Method m = null;
        Object[] values = null;
        Class<?>[] types = null;
        if ( this.isParametersProvided() ) {
            values = ( ( AstMethodParameters ) this.jjtGetChild ( this.jjtGetNumChildren() - 1 ) ).getParameters ( ctx );
            types = this.getTypesFromValues ( values );
        } else {
            values = paramValues;
            types = ( Class<?>[] ) paramTypes;
        }
        m = ReflectionUtil.getMethod ( ctx, t.base, t.property, types, values );
        values = this.convertArgs ( ctx, values, m );
        Object result = null;
        try {
            result = m.invoke ( t.base, values );
        } catch ( IllegalAccessException iae ) {
            throw new ELException ( ( Throwable ) iae );
        } catch ( IllegalArgumentException iae2 ) {
            throw new ELException ( ( Throwable ) iae2 );
        } catch ( InvocationTargetException ite ) {
            final Throwable cause = ite.getCause();
            if ( cause instanceof ThreadDeath ) {
                throw ( ThreadDeath ) cause;
            }
            if ( cause instanceof VirtualMachineError ) {
                throw ( VirtualMachineError ) cause;
            }
            throw new ELException ( cause );
        }
        return result;
    }
    private Object[] convertArgs ( final EvaluationContext ctx, final Object[] src, final Method m ) {
        final Class<?>[] types = m.getParameterTypes();
        if ( types.length == 0 ) {
            return AstValue.EMPTY_ARRAY;
        }
        final int paramCount = types.length;
        if ( ( m.isVarArgs() && paramCount > 1 && ( src == null || paramCount > src.length ) ) || ( !m.isVarArgs() && ( ( paramCount > 0 && src == null ) || ( src != null && src.length != paramCount ) ) ) ) {
            String srcCount = null;
            if ( src != null ) {
                srcCount = Integer.toString ( src.length );
            }
            String msg;
            if ( m.isVarArgs() ) {
                msg = MessageFactory.get ( "error.invoke.tooFewParams", m.getName(), srcCount, Integer.toString ( paramCount ) );
            } else {
                msg = MessageFactory.get ( "error.invoke.wrongParams", m.getName(), srcCount, Integer.toString ( paramCount ) );
            }
            throw new IllegalArgumentException ( msg );
        }
        if ( src == null ) {
            return new Object[1];
        }
        final Object[] dest = new Object[paramCount];
        for ( int i = 0; i < paramCount - 1; ++i ) {
            dest[i] = ELSupport.coerceToType ( ctx, src[i], types[i] );
        }
        if ( m.isVarArgs() ) {
            final Object[] varArgs = ( Object[] ) Array.newInstance ( m.getParameterTypes() [paramCount - 1].getComponentType(), src.length - ( paramCount - 1 ) );
            for ( int j = 0; j < src.length - ( paramCount - 1 ); ++j ) {
                varArgs[j] = ELSupport.coerceToType ( ctx, src[paramCount - 1 + j], types[paramCount - 1].getComponentType() );
            }
            dest[paramCount - 1] = varArgs;
        } else {
            dest[paramCount - 1] = ELSupport.coerceToType ( ctx, src[paramCount - 1], types[paramCount - 1] );
        }
        return dest;
    }
    private Class<?>[] getTypesFromValues ( final Object[] values ) {
        if ( values == null ) {
            return null;
        }
        final Class<?>[] result = ( Class<?>[] ) new Class[values.length];
        for ( int i = 0; i < values.length; ++i ) {
            if ( values[i] == null ) {
                result[i] = null;
            } else {
                result[i] = values[i].getClass();
            }
        }
        return result;
    }
    @Override
    public ValueReference getValueReference ( final EvaluationContext ctx ) {
        if ( this.children.length > 2 && this.jjtGetChild ( 2 ) instanceof AstMethodParameters ) {
            return null;
        }
        final Target t = this.getTarget ( ctx );
        return new ValueReference ( t.base, t.property );
    }
    @Override
    public boolean isParametersProvided() {
        final int len = this.children.length;
        return len > 2 && this.jjtGetChild ( len - 1 ) instanceof AstMethodParameters;
    }
    static {
        EMPTY_ARRAY = new Object[0];
    }
    protected static class Target {
        protected Object base;
        protected Object property;
    }
}

package org.apache.el.parser;
import javax.el.VariableMapper;
import java.lang.reflect.InvocationTargetException;
import org.apache.el.lang.ELSupport;
import javax.el.ELClass;
import javax.el.LambdaExpression;
import javax.el.ELContext;
import javax.el.ValueExpression;
import java.lang.reflect.Method;
import javax.el.FunctionMapper;
import javax.el.ELException;
import org.apache.el.util.MessageFactory;
import org.apache.el.lang.EvaluationContext;
public final class AstFunction extends SimpleNode {
    protected String localName;
    protected String prefix;
    public AstFunction ( final int id ) {
        super ( id );
        this.localName = "";
        this.prefix = "";
    }
    public String getLocalName() {
        return this.localName;
    }
    public String getOutputName() {
        if ( this.prefix == null ) {
            return this.localName;
        }
        return this.prefix + ":" + this.localName;
    }
    public String getPrefix() {
        return this.prefix;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        final FunctionMapper fnMapper = ctx.getFunctionMapper();
        if ( fnMapper == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.null" ) );
        }
        final Method m = fnMapper.resolveFunction ( this.prefix, this.localName );
        if ( m == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.method", this.getOutputName() ) );
        }
        return m.getReturnType();
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final FunctionMapper fnMapper = ctx.getFunctionMapper();
        if ( fnMapper == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.null" ) );
        }
        final Method m = fnMapper.resolveFunction ( this.prefix, this.localName );
        if ( m == null && this.prefix.length() == 0 ) {
            Object obj = null;
            if ( ctx.isLambdaArgument ( this.localName ) ) {
                obj = ctx.getLambdaArgument ( this.localName );
            }
            if ( obj == null ) {
                final VariableMapper varMapper = ctx.getVariableMapper();
                if ( varMapper != null ) {
                    obj = varMapper.resolveVariable ( this.localName );
                    if ( obj instanceof ValueExpression ) {
                        obj = ( ( ValueExpression ) obj ).getValue ( ( ELContext ) ctx );
                    }
                }
            }
            if ( obj == null ) {
                obj = ctx.getELResolver().getValue ( ( ELContext ) ctx, ( Object ) null, ( Object ) this.localName );
            }
            if ( obj instanceof LambdaExpression ) {
                int i;
                Node args;
                for ( i = 0; obj instanceof LambdaExpression && i < this.jjtGetNumChildren(); obj = ( ( LambdaExpression ) obj ).invoke ( ( ( AstMethodParameters ) args ).getParameters ( ctx ) ), ++i ) {
                    args = this.jjtGetChild ( i );
                }
                if ( i < this.jjtGetNumChildren() ) {
                    throw new ELException ( MessageFactory.get ( "error.lambda.tooManyMethodParameterSets" ) );
                }
                return obj;
            } else {
                obj = ctx.getImportHandler().resolveClass ( this.localName );
                if ( obj != null ) {
                    return ctx.getELResolver().invoke ( ( ELContext ) ctx, ( Object ) new ELClass ( ( Class ) obj ), ( Object ) "<init>", ( Class[] ) null, ( ( AstMethodParameters ) this.children[0] ).getParameters ( ctx ) );
                }
                obj = ctx.getImportHandler().resolveStatic ( this.localName );
                if ( obj != null ) {
                    return ctx.getELResolver().invoke ( ( ELContext ) ctx, ( Object ) new ELClass ( ( Class ) obj ), ( Object ) this.localName, ( Class[] ) null, ( ( AstMethodParameters ) this.children[0] ).getParameters ( ctx ) );
                }
            }
        }
        if ( m == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.method", this.getOutputName() ) );
        }
        if ( this.jjtGetNumChildren() != 1 ) {
            throw new ELException ( MessageFactory.get ( "error.funciton.tooManyMethodParameterSets", this.getOutputName() ) );
        }
        final Node parameters = this.jjtGetChild ( 0 );
        final Class<?>[] paramTypes = m.getParameterTypes();
        Object[] params = null;
        Object result = null;
        final int inputParameterCount = parameters.jjtGetNumChildren();
        final int methodParameterCount = paramTypes.length;
        if ( inputParameterCount > 0 ) {
            params = new Object[methodParameterCount];
            try {
                for ( int j = 0; j < methodParameterCount; ++j ) {
                    if ( m.isVarArgs() && j == methodParameterCount - 1 ) {
                        if ( inputParameterCount < methodParameterCount ) {
                            params[j] = null;
                        } else {
                            final Object[] varargs = new Object[inputParameterCount - methodParameterCount + 1];
                            final Class<?> target = paramTypes[j].getComponentType();
                            for ( int k = j; k < inputParameterCount; ++k ) {
                                varargs[k - j] = parameters.jjtGetChild ( k ).getValue ( ctx );
                                varargs[k - j] = ELSupport.coerceToType ( ctx, varargs[k - j], target );
                            }
                            params[j] = varargs;
                            params[j] = ELSupport.coerceToType ( ctx, params[j], paramTypes[j] );
                        }
                    } else {
                        params[j] = parameters.jjtGetChild ( j ).getValue ( ctx );
                        params[j] = ELSupport.coerceToType ( ctx, params[j], paramTypes[j] );
                    }
                }
            } catch ( ELException ele ) {
                throw new ELException ( MessageFactory.get ( "error.function", this.getOutputName() ), ( Throwable ) ele );
            }
        }
        try {
            result = m.invoke ( null, params );
        } catch ( IllegalAccessException iae ) {
            throw new ELException ( MessageFactory.get ( "error.function", this.getOutputName() ), ( Throwable ) iae );
        } catch ( InvocationTargetException ite ) {
            final Throwable cause = ite.getCause();
            if ( cause instanceof ThreadDeath ) {
                throw ( ThreadDeath ) cause;
            }
            if ( cause instanceof VirtualMachineError ) {
                throw ( VirtualMachineError ) cause;
            }
            throw new ELException ( MessageFactory.get ( "error.function", this.getOutputName() ), cause );
        }
        return result;
    }
    public void setLocalName ( final String localName ) {
        this.localName = localName;
    }
    public void setPrefix ( final String prefix ) {
        this.prefix = prefix;
    }
    @Override
    public String toString() {
        return ELParserTreeConstants.jjtNodeName[this.id] + "[" + this.getOutputName() + "]";
    }
}

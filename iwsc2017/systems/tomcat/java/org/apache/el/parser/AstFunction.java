package org.apache.el.parser;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.el.ELClass;
import javax.el.ELException;
import javax.el.FunctionMapper;
import javax.el.LambdaExpression;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import org.apache.el.lang.EvaluationContext;
import org.apache.el.util.MessageFactory;
public final class AstFunction extends SimpleNode {
    protected String localName = "";
    protected String prefix = "";
    public AstFunction ( int id ) {
        super ( id );
    }
    public String getLocalName() {
        return localName;
    }
    public String getOutputName() {
        if ( this.prefix == null ) {
            return this.localName;
        } else {
            return this.prefix + ":" + this.localName;
        }
    }
    public String getPrefix() {
        return prefix;
    }
    @Override
    public Class<?> getType ( EvaluationContext ctx )
    throws ELException {
        FunctionMapper fnMapper = ctx.getFunctionMapper();
        if ( fnMapper == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.null" ) );
        }
        Method m = fnMapper.resolveFunction ( this.prefix, this.localName );
        if ( m == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.method",
                                    this.getOutputName() ) );
        }
        return m.getReturnType();
    }
    @Override
    public Object getValue ( EvaluationContext ctx )
    throws ELException {
        FunctionMapper fnMapper = ctx.getFunctionMapper();
        if ( fnMapper == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.null" ) );
        }
        Method m = fnMapper.resolveFunction ( this.prefix, this.localName );
        if ( m == null && this.prefix.length() == 0 ) {
            Object obj = null;
            if ( ctx.isLambdaArgument ( this.localName ) ) {
                obj = ctx.getLambdaArgument ( this.localName );
            }
            if ( obj == null ) {
                VariableMapper varMapper = ctx.getVariableMapper();
                if ( varMapper != null ) {
                    obj = varMapper.resolveVariable ( this.localName );
                    if ( obj instanceof ValueExpression ) {
                        obj = ( ( ValueExpression ) obj ).getValue ( ctx );
                    }
                }
            }
            if ( obj == null ) {
                obj = ctx.getELResolver().getValue ( ctx, null, this.localName );
            }
            if ( obj instanceof LambdaExpression ) {
                int i = 0;
                while ( obj instanceof LambdaExpression &&
                        i < jjtGetNumChildren() ) {
                    Node args = jjtGetChild ( i );
                    obj = ( ( LambdaExpression ) obj ).invoke (
                              ( ( AstMethodParameters ) args ).getParameters ( ctx ) );
                    i++;
                }
                if ( i < jjtGetNumChildren() ) {
                    throw new ELException ( MessageFactory.get (
                                                "error.lambda.tooManyMethodParameterSets" ) );
                }
                return obj;
            }
            obj = ctx.getImportHandler().resolveClass ( this.localName );
            if ( obj != null ) {
                return ctx.getELResolver().invoke ( ctx, new ELClass ( ( Class<?> ) obj ), "<init>", null,
                                                    ( ( AstMethodParameters ) this.children[0] ).getParameters ( ctx ) );
            }
            obj = ctx.getImportHandler().resolveStatic ( this.localName );
            if ( obj != null ) {
                return ctx.getELResolver().invoke ( ctx, new ELClass ( ( Class<?> ) obj ), this.localName,
                                                    null, ( ( AstMethodParameters ) this.children[0] ).getParameters ( ctx ) );
            }
        }
        if ( m == null ) {
            throw new ELException ( MessageFactory.get ( "error.fnMapper.method",
                                    this.getOutputName() ) );
        }
        if ( this.jjtGetNumChildren() != 1 ) {
            throw new ELException ( MessageFactory.get (
                                        "error.funciton.tooManyMethodParameterSets",
                                        getOutputName() ) );
        }
        Node parameters = jjtGetChild ( 0 );
        Class<?>[] paramTypes = m.getParameterTypes();
        Object[] params = null;
        Object result = null;
        int inputParameterCount = parameters.jjtGetNumChildren();
        int methodParameterCount = paramTypes.length;
        if ( inputParameterCount > 0 ) {
            params = new Object[methodParameterCount];
            try {
                for ( int i = 0; i < methodParameterCount; i++ ) {
                    if ( m.isVarArgs() && i == methodParameterCount - 1 ) {
                        if ( inputParameterCount < methodParameterCount ) {
                            params[i] = null;
                        } else {
                            Object[] varargs =
                                new Object[inputParameterCount - methodParameterCount + 1];
                            Class<?> target = paramTypes[i].getComponentType();
                            for ( int j = i; j < inputParameterCount; j++ ) {
                                varargs[j - i] = parameters.jjtGetChild ( j ).getValue ( ctx );
                                varargs[j - i] = coerceToType ( ctx, varargs[j - i], target );
                            }
                            params[i] = varargs;
                            params[i] = coerceToType ( ctx, params[i], paramTypes[i] );
                        }
                    } else {
                        params[i] = parameters.jjtGetChild ( i ).getValue ( ctx );
                        params[i] = coerceToType ( ctx, params[i], paramTypes[i] );
                    }
                }
            } catch ( ELException ele ) {
                throw new ELException ( MessageFactory.get ( "error.function", this
                                        .getOutputName() ), ele );
            }
        }
        try {
            result = m.invoke ( null, params );
        } catch ( IllegalAccessException iae ) {
            throw new ELException ( MessageFactory.get ( "error.function", this
                                    .getOutputName() ), iae );
        } catch ( InvocationTargetException ite ) {
            Throwable cause = ite.getCause();
            if ( cause instanceof ThreadDeath ) {
                throw ( ThreadDeath ) cause;
            }
            if ( cause instanceof VirtualMachineError ) {
                throw ( VirtualMachineError ) cause;
            }
            throw new ELException ( MessageFactory.get ( "error.function", this
                                    .getOutputName() ), cause );
        }
        return result;
    }
    public void setLocalName ( String localName ) {
        this.localName = localName;
    }
    public void setPrefix ( String prefix ) {
        this.prefix = prefix;
    }
    @Override
    public String toString() {
        return ELParserTreeConstants.jjtNodeName[id] + "[" + this.getOutputName() + "]";
    }
}

package javax.el;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
public class LambdaExpression {
    private final List<String> formalParameters;
    private final ValueExpression expression;
    private final Map<String, Object> nestedArguments;
    private ELContext context;
    public LambdaExpression ( final List<String> formalParameters, final ValueExpression expression ) {
        this.nestedArguments = new HashMap<String, Object>();
        this.context = null;
        this.formalParameters = formalParameters;
        this.expression = expression;
    }
    public void setELContext ( final ELContext context ) {
        this.context = context;
    }
    public Object invoke ( final ELContext context, final Object... args ) throws ELException {
        Objects.requireNonNull ( context );
        int formalParamCount = 0;
        if ( this.formalParameters != null ) {
            formalParamCount = this.formalParameters.size();
        }
        int argCount = 0;
        if ( args != null ) {
            argCount = args.length;
        }
        if ( formalParamCount > argCount ) {
            throw new ELException ( Util.message ( context, "lambdaExpression.tooFewArgs", argCount, formalParamCount ) );
        }
        final Map<String, Object> lambdaArguments = new HashMap<String, Object>();
        lambdaArguments.putAll ( this.nestedArguments );
        for ( int i = 0; i < formalParamCount; ++i ) {
            lambdaArguments.put ( this.formalParameters.get ( i ), args[i] );
        }
        context.enterLambdaScope ( lambdaArguments );
        try {
            final Object result = this.expression.getValue ( context );
            if ( result instanceof LambdaExpression ) {
                ( ( LambdaExpression ) result ).nestedArguments.putAll ( lambdaArguments );
            }
            return result;
        } finally {
            context.exitLambdaScope();
        }
    }
    public Object invoke ( final Object... args ) {
        return this.invoke ( this.context, args );
    }
}

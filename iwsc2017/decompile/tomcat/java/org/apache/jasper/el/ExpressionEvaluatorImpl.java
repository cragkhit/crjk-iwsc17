package org.apache.jasper.el;
import javax.servlet.jsp.el.VariableResolver;
import javax.el.ValueExpression;
import javax.el.ELException;
import javax.servlet.jsp.el.ELParseException;
import javax.el.ELContext;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.FunctionMapper;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.el.ExpressionEvaluator;
@Deprecated
public final class ExpressionEvaluatorImpl extends ExpressionEvaluator {
    private final ExpressionFactory factory;
    public ExpressionEvaluatorImpl ( final ExpressionFactory factory ) {
        this.factory = factory;
    }
    public Expression parseExpression ( final String expression, final Class expectedType, final FunctionMapper fMapper ) throws javax.servlet.jsp.el.ELException {
        try {
            final ELContextImpl ctx = new ELContextImpl ( ELContextImpl.getDefaultResolver ( this.factory ) );
            if ( fMapper != null ) {
                ctx.setFunctionMapper ( new FunctionMapperImpl ( fMapper ) );
            }
            final ValueExpression ve = this.factory.createValueExpression ( ( ELContext ) ctx, expression, expectedType );
            return new ExpressionImpl ( ve, this.factory );
        } catch ( ELException e ) {
            throw new ELParseException ( e.getMessage() );
        }
    }
    public Object evaluate ( final String expression, final Class expectedType, final VariableResolver vResolver, final FunctionMapper fMapper ) throws javax.servlet.jsp.el.ELException {
        return this.parseExpression ( expression, expectedType, fMapper ).evaluate ( vResolver );
    }
}

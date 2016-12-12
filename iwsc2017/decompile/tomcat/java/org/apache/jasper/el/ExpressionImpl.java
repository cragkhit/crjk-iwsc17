package org.apache.jasper.el;
import javax.servlet.jsp.el.ELException;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.jsp.el.VariableResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.servlet.jsp.el.Expression;
@Deprecated
public final class ExpressionImpl extends Expression {
    private final ValueExpression ve;
    private final ExpressionFactory factory;
    public ExpressionImpl ( final ValueExpression ve, final ExpressionFactory factory ) {
        this.ve = ve;
        this.factory = factory;
    }
    public Object evaluate ( final VariableResolver vResolver ) throws ELException {
        final ELContext ctx = new ELContextImpl ( new ELResolverImpl ( vResolver, this.factory ) );
        return this.ve.getValue ( ctx );
    }
}

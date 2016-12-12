package javax.servlet.jsp.el;
public abstract class ExpressionEvaluator {
    public abstract Expression parseExpression ( final String p0, final Class p1, final FunctionMapper p2 ) throws ELException;
    public abstract Object evaluate ( final String p0, final Class p1, final VariableResolver p2, final FunctionMapper p3 ) throws ELException;
}

package javax.servlet.jsp.el;
@SuppressWarnings ( "dep-ann" )
public abstract class ExpressionEvaluator {
    public abstract Expression parseExpression ( String expression,
            @SuppressWarnings ( "rawtypes" )
            Class expectedType, FunctionMapper fMapper ) throws ELException;
    public abstract Object evaluate (
        String expression,
        @SuppressWarnings ( "rawtypes" )
        Class expectedType, VariableResolver vResolver,
        FunctionMapper fMapper ) throws ELException;
}

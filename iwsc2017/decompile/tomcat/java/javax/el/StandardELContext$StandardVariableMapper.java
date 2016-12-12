package javax.el;
import java.util.HashMap;
import java.util.Map;
private static class StandardVariableMapper extends VariableMapper {
    private Map<String, ValueExpression> vars;
    @Override
    public ValueExpression resolveVariable ( final String variable ) {
        if ( this.vars == null ) {
            return null;
        }
        return this.vars.get ( variable );
    }
    @Override
    public ValueExpression setVariable ( final String variable, final ValueExpression expression ) {
        if ( this.vars == null ) {
            this.vars = new HashMap<String, ValueExpression>();
        }
        if ( expression == null ) {
            return this.vars.remove ( variable );
        }
        return this.vars.put ( variable, expression );
    }
}

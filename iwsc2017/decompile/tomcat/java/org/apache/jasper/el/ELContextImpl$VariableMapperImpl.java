package org.apache.jasper.el;
import java.util.HashMap;
import javax.el.ValueExpression;
import java.util.Map;
import javax.el.VariableMapper;
private static final class VariableMapperImpl extends VariableMapper {
    private Map<String, ValueExpression> vars;
    public ValueExpression resolveVariable ( final String variable ) {
        if ( this.vars == null ) {
            return null;
        }
        return this.vars.get ( variable );
    }
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

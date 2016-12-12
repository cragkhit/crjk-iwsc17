package org.apache.el.lang;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.HashMap;
import javax.el.ValueExpression;
import java.util.Map;
import java.io.Externalizable;
import javax.el.VariableMapper;
public class VariableMapperImpl extends VariableMapper implements Externalizable {
    private static final long serialVersionUID = 1L;
    private Map<String, ValueExpression> vars;
    public VariableMapperImpl() {
        this.vars = new HashMap<String, ValueExpression>();
    }
    public ValueExpression resolveVariable ( final String variable ) {
        return this.vars.get ( variable );
    }
    public ValueExpression setVariable ( final String variable, final ValueExpression expression ) {
        if ( expression == null ) {
            return this.vars.remove ( variable );
        }
        return this.vars.put ( variable, expression );
    }
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.vars = ( Map<String, ValueExpression> ) in.readObject();
    }
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeObject ( this.vars );
    }
}

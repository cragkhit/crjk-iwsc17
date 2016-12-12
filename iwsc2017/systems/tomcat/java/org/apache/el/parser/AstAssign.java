package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public class AstAssign extends SimpleNode {
    public AstAssign ( int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( EvaluationContext ctx ) throws ELException {
        Object value = children[1].getValue ( ctx );
        children[0].setValue ( ctx, value );
        return value;
    }
    @Override
    public Class<?> getType ( EvaluationContext ctx ) throws ELException {
        Object value = children[1].getValue ( ctx );
        children[0].setValue ( ctx, value );
        return children[1].getType ( ctx );
    }
}

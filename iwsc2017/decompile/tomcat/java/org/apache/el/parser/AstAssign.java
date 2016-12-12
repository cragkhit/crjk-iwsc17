package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public class AstAssign extends SimpleNode {
    public AstAssign ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object value = this.children[1].getValue ( ctx );
        this.children[0].setValue ( ctx, value );
        return value;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        final Object value = this.children[1].getValue ( ctx );
        this.children[0].setValue ( ctx, value );
        return this.children[1].getType ( ctx );
    }
}

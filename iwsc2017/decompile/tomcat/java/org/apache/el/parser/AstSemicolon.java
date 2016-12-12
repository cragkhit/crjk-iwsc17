package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public class AstSemicolon extends SimpleNode {
    public AstSemicolon ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        this.children[0].getValue ( ctx );
        return this.children[1].getValue ( ctx );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        this.children[0].getType ( ctx );
        return this.children[1].getType ( ctx );
    }
}

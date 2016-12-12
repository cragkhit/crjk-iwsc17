package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public class AstSemicolon extends SimpleNode {
    public AstSemicolon ( int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( EvaluationContext ctx ) throws ELException {
        children[0].getValue ( ctx );
        return children[1].getValue ( ctx );
    }
    @Override
    public Class<?> getType ( EvaluationContext ctx ) throws ELException {
        children[0].getType ( ctx );
        return children[1].getType ( ctx );
    }
}

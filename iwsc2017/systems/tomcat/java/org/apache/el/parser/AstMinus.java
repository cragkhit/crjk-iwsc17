package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.ELArithmetic;
import org.apache.el.lang.EvaluationContext;
public final class AstMinus extends ArithmeticNode {
    public AstMinus ( int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( EvaluationContext ctx )
    throws ELException {
        Object obj0 = this.children[0].getValue ( ctx );
        Object obj1 = this.children[1].getValue ( ctx );
        return ELArithmetic.subtract ( obj0, obj1 );
    }
}
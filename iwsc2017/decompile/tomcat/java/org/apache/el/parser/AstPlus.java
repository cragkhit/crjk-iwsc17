package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.ELArithmetic;
import org.apache.el.lang.EvaluationContext;
public final class AstPlus extends ArithmeticNode {
    public AstPlus ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj0 = this.children[0].getValue ( ctx );
        final Object obj = this.children[1].getValue ( ctx );
        return ELArithmetic.add ( obj0, obj );
    }
}

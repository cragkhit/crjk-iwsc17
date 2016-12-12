package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public class AstConcatenation extends SimpleNode {
    public AstConcatenation ( int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( EvaluationContext ctx ) throws ELException {
        String s1 = coerceToString ( ctx, children[0].getValue ( ctx ) );
        String s2 = coerceToString ( ctx, children[1].getValue ( ctx ) );
        return s1 + s2;
    }
    @Override
    public Class<?> getType ( EvaluationContext ctx ) throws ELException {
        return String.class;
    }
}

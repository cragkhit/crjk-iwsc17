package org.apache.el.parser;
import javax.el.ELException;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import org.apache.el.lang.EvaluationContext;
public class AstConcatenation extends SimpleNode {
    public AstConcatenation ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final String s1 = ELSupport.coerceToString ( ctx, this.children[0].getValue ( ctx ) );
        final String s2 = ELSupport.coerceToString ( ctx, this.children[1].getValue ( ctx ) );
        return s1 + s2;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return String.class;
    }
}

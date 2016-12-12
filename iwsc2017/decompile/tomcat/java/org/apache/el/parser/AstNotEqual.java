package org.apache.el.parser;
import javax.el.ELException;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import org.apache.el.lang.EvaluationContext;
public final class AstNotEqual extends BooleanNode {
    public AstNotEqual ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj0 = this.children[0].getValue ( ctx );
        final Object obj = this.children[1].getValue ( ctx );
        return !ELSupport.equals ( ctx, obj0, obj );
    }
}

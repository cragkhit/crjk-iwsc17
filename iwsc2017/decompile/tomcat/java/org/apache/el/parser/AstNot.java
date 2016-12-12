package org.apache.el.parser;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstNot extends SimpleNode {
    public AstNot ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Boolean.class;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj = this.children[0].getValue ( ctx );
        final Boolean b = ELSupport.coerceToBoolean ( ctx, obj, true );
        return !b;
    }
}

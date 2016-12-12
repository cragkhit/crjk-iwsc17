package org.apache.el.parser;
import javax.el.ELException;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import org.apache.el.lang.EvaluationContext;
public final class AstOr extends BooleanNode {
    public AstOr ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        Object obj = this.children[0].getValue ( ctx );
        Boolean b = ELSupport.coerceToBoolean ( ctx, obj, true );
        if ( b ) {
            return b;
        }
        obj = this.children[1].getValue ( ctx );
        b = ELSupport.coerceToBoolean ( ctx, obj, true );
        return b;
    }
}

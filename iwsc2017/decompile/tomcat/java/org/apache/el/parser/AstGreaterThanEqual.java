package org.apache.el.parser;
import javax.el.ELException;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import org.apache.el.lang.EvaluationContext;
public final class AstGreaterThanEqual extends BooleanNode {
    public AstGreaterThanEqual ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj0 = this.children[0].getValue ( ctx );
        final Object obj = this.children[1].getValue ( ctx );
        if ( obj0 == obj ) {
            return Boolean.TRUE;
        }
        if ( obj0 == null || obj == null ) {
            return Boolean.FALSE;
        }
        return ( ELSupport.compare ( ctx, obj0, obj ) >= 0 ) ? Boolean.TRUE : Boolean.FALSE;
    }
}

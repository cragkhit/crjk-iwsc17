package org.apache.el.parser;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstChoice extends SimpleNode {
    public AstChoice ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        final Object val = this.getValue ( ctx );
        return ( val != null ) ? val.getClass() : null;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj0 = this.children[0].getValue ( ctx );
        final Boolean b0 = ELSupport.coerceToBoolean ( ctx, obj0, true );
        return this.children[ ( ( boolean ) b0 ) ? 1 : 2].getValue ( ctx );
    }
}

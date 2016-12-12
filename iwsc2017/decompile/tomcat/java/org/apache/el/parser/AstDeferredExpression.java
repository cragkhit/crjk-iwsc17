package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstDeferredExpression extends SimpleNode {
    public AstDeferredExpression ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return this.children[0].getType ( ctx );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return this.children[0].getValue ( ctx );
    }
    @Override
    public boolean isReadOnly ( final EvaluationContext ctx ) throws ELException {
        return this.children[0].isReadOnly ( ctx );
    }
    @Override
    public void setValue ( final EvaluationContext ctx, final Object value ) throws ELException {
        this.children[0].setValue ( ctx, value );
    }
}

package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstNull extends SimpleNode {
    public AstNull ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return null;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return null;
    }
}

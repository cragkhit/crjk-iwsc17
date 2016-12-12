package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public abstract class BooleanNode extends SimpleNode {
    public BooleanNode ( final int i ) {
        super ( i );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Boolean.class;
    }
}

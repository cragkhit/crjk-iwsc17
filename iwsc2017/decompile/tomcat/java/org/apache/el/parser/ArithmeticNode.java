package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public abstract class ArithmeticNode extends SimpleNode {
    public ArithmeticNode ( final int i ) {
        super ( i );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Number.class;
    }
}

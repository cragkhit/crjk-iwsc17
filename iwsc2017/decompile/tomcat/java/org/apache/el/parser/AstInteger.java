package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
import java.math.BigInteger;
public final class AstInteger extends SimpleNode {
    private volatile Number number;
    public AstInteger ( final int id ) {
        super ( id );
    }
    protected Number getInteger() {
        if ( this.number == null ) {
            try {
                this.number = new Long ( this.image );
            } catch ( ArithmeticException e1 ) {
                this.number = new BigInteger ( this.image );
            }
        }
        return this.number;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return this.getInteger().getClass();
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return this.getInteger();
    }
}

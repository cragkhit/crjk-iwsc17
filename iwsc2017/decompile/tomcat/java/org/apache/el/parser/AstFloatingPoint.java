package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
import java.math.BigDecimal;
public final class AstFloatingPoint extends SimpleNode {
    private volatile Number number;
    public AstFloatingPoint ( final int id ) {
        super ( id );
    }
    public Number getFloatingPoint() {
        if ( this.number == null ) {
            try {
                this.number = new Double ( this.image );
            } catch ( ArithmeticException e0 ) {
                this.number = new BigDecimal ( this.image );
            }
        }
        return this.number;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return this.getFloatingPoint();
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return this.getFloatingPoint().getClass();
    }
}

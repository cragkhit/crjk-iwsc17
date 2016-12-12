package org.apache.el.parser;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import java.math.BigInteger;
import java.math.BigDecimal;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstNegative extends SimpleNode {
    public AstNegative ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Number.class;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj = this.children[0].getValue ( ctx );
        if ( obj == null ) {
            return 0L;
        }
        if ( obj instanceof BigDecimal ) {
            return ( ( BigDecimal ) obj ).negate();
        }
        if ( obj instanceof BigInteger ) {
            return ( ( BigInteger ) obj ).negate();
        }
        if ( obj instanceof String ) {
            if ( ELSupport.isStringFloat ( ( String ) obj ) ) {
                return new Double ( -Double.parseDouble ( ( String ) obj ) );
            }
            return -Long.parseLong ( ( String ) obj );
        } else {
            if ( obj instanceof Long ) {
                return - ( long ) obj;
            }
            if ( obj instanceof Double ) {
                return new Double ( - ( double ) obj );
            }
            if ( obj instanceof Integer ) {
                return - ( int ) obj;
            }
            if ( obj instanceof Float ) {
                return new Float ( - ( float ) obj );
            }
            if ( obj instanceof Short ) {
                return ( short ) ( - ( short ) obj );
            }
            if ( obj instanceof Byte ) {
                return ( byte ) ( - ( byte ) obj );
            }
            final Long num = ( Long ) ELSupport.coerceToNumber ( ctx, obj, Long.class );
            return -num;
        }
    }
}

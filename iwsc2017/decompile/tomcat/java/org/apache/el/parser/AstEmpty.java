package org.apache.el.parser;
import java.util.Map;
import java.util.Collection;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstEmpty extends SimpleNode {
    public AstEmpty ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Boolean.class;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Object obj = this.children[0].getValue ( ctx );
        if ( obj == null ) {
            return Boolean.TRUE;
        }
        if ( obj instanceof String ) {
            return ( ( String ) obj ).length() == 0;
        }
        if ( obj instanceof Object[] ) {
            return ( ( Object[] ) obj ).length == 0;
        }
        if ( obj instanceof Collection ) {
            return ( ( Collection ) obj ).isEmpty();
        }
        if ( obj instanceof Map ) {
            return ( ( Map ) obj ).isEmpty();
        }
        return Boolean.FALSE;
    }
}

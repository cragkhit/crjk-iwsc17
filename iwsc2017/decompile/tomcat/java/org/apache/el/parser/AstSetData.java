package org.apache.el.parser;
import javax.el.ELException;
import java.util.Set;
import java.util.HashSet;
import org.apache.el.lang.EvaluationContext;
public class AstSetData extends SimpleNode {
    public AstSetData ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final Set<Object> result = new HashSet<Object>();
        if ( this.children != null ) {
            for ( final Node child : this.children ) {
                result.add ( child.getValue ( ctx ) );
            }
        }
        return result;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return Set.class;
    }
}

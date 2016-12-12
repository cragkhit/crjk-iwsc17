package org.apache.el.parser;
import javax.el.ELException;
import java.util.List;
import java.util.ArrayList;
import org.apache.el.lang.EvaluationContext;
public class AstListData extends SimpleNode {
    public AstListData ( final int id ) {
        super ( id );
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final List<Object> result = new ArrayList<Object>();
        if ( this.children != null ) {
            for ( final Node child : this.children ) {
                result.add ( child.getValue ( ctx ) );
            }
        }
        return result;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return List.class;
    }
}

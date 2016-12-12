package org.apache.el.parser;
import java.util.ArrayList;
import org.apache.el.lang.EvaluationContext;
public final class AstMethodParameters extends SimpleNode {
    public AstMethodParameters ( final int id ) {
        super ( id );
    }
    public Object[] getParameters ( final EvaluationContext ctx ) {
        final ArrayList<Object> params = new ArrayList<Object>();
        for ( int i = 0; i < this.jjtGetNumChildren(); ++i ) {
            params.add ( this.jjtGetChild ( i ).getValue ( ctx ) );
        }
        return params.toArray ( new Object[params.size()] );
    }
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append ( '(' );
        if ( this.children != null ) {
            for ( final Node n : this.children ) {
                result.append ( n.toString() );
                result.append ( ',' );
            }
        }
        result.append ( ')' );
        return result.toString();
    }
}

package org.apache.el.parser;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstCompositeExpression extends SimpleNode {
    public AstCompositeExpression ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return String.class;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        final StringBuilder sb = new StringBuilder ( 16 );
        Object obj = null;
        if ( this.children != null ) {
            for ( int i = 0; i < this.children.length; ++i ) {
                obj = this.children[i].getValue ( ctx );
                if ( obj != null ) {
                    sb.append ( ELSupport.coerceToString ( ctx, obj ) );
                }
            }
        }
        return sb.toString();
    }
}

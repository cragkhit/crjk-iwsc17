package org.apache.el.parser;
public class AstLambdaParameters extends SimpleNode {
    public AstLambdaParameters ( final int id ) {
        super ( id );
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
        result.append ( ")->" );
        return result.toString();
    }
}

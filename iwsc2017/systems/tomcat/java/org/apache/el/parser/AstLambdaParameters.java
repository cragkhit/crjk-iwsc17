package org.apache.el.parser;
public class AstLambdaParameters extends SimpleNode {
    public AstLambdaParameters ( int id ) {
        super ( id );
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append ( '(' );
        if ( children != null ) {
            for ( Node n : children ) {
                result.append ( n.toString() );
                result.append ( ',' );
            }
        }
        result.append ( ")->" );
        return result.toString();
    }
}

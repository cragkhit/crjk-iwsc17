package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstLiteralExpression extends SimpleNode {
    public AstLiteralExpression ( final int id ) {
        super ( id );
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return String.class;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return this.image;
    }
    @Override
    public void setImage ( final String image ) {
        if ( image.indexOf ( 92 ) == -1 ) {
            this.image = image;
            return;
        }
        final int size = image.length();
        final StringBuilder buf = new StringBuilder ( size );
        for ( int i = 0; i < size; ++i ) {
            char c = image.charAt ( i );
            if ( c == '\\' && i + 2 < size ) {
                final char c2 = image.charAt ( i + 1 );
                final char c3 = image.charAt ( i + 2 );
                if ( ( c2 == '#' || c2 == '$' ) && c3 == '{' ) {
                    c = c2;
                    ++i;
                }
            }
            buf.append ( c );
        }
        this.image = buf.toString();
    }
}

package org.apache.el.parser;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public final class AstString extends SimpleNode {
    private volatile String string;
    public AstString ( final int id ) {
        super ( id );
    }
    public String getString() {
        if ( this.string == null ) {
            this.string = this.image.substring ( 1, this.image.length() - 1 );
        }
        return this.string;
    }
    @Override
    public Class<?> getType ( final EvaluationContext ctx ) throws ELException {
        return String.class;
    }
    @Override
    public Object getValue ( final EvaluationContext ctx ) throws ELException {
        return this.getString();
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
            if ( c == '\\' && i + 1 < size ) {
                final char c2 = image.charAt ( i + 1 );
                if ( c2 == '\\' || c2 == '\"' || c2 == '\'' ) {
                    c = c2;
                    ++i;
                }
            }
            buf.append ( c );
        }
        this.image = buf.toString();
    }
}

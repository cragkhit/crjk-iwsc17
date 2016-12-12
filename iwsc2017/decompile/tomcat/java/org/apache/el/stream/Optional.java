package org.apache.el.stream;
import javax.el.LambdaExpression;
import javax.el.ELException;
import org.apache.el.util.MessageFactory;
public class Optional {
    private final Object obj;
    static final Optional EMPTY;
    Optional ( final Object obj ) {
        this.obj = obj;
    }
    public Object get() throws ELException {
        if ( this.obj == null ) {
            throw new ELException ( MessageFactory.get ( "stream.optional.empty" ) );
        }
        return this.obj;
    }
    public void ifPresent ( final LambdaExpression le ) {
        if ( this.obj != null ) {
            le.invoke ( new Object[] { this.obj } );
        }
    }
    public Object orElse ( final Object other ) {
        if ( this.obj == null ) {
            return other;
        }
        return this.obj;
    }
    public Object orElseGet ( final Object le ) {
        if ( this.obj != null ) {
            return this.obj;
        }
        if ( le instanceof LambdaExpression ) {
            return ( ( LambdaExpression ) le ).invoke ( ( Object[] ) null );
        }
        return le;
    }
    static {
        EMPTY = new Optional ( null );
    }
}

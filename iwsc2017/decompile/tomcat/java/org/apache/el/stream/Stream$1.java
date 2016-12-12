package org.apache.el.stream;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.LambdaExpression;
class Stream$1 extends OpIterator {
    final   LambdaExpression val$le;
    @Override
    protected void findNext() {
        while ( Stream.access$100 ( Stream.this ).hasNext() ) {
            final Object obj = Stream.access$100 ( Stream.this ).next();
            if ( ELSupport.coerceToBoolean ( null, this.val$le.invoke ( new Object[] { obj } ), true ) ) {
                this.next = obj;
                this.foundNext = true;
                break;
            }
        }
    }
}

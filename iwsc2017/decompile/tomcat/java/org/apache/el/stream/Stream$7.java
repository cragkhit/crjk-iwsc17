package org.apache.el.stream;
import javax.el.LambdaExpression;
class Stream$7 extends OpIterator {
    final   LambdaExpression val$le;
    @Override
    protected void findNext() {
        if ( Stream.access$100 ( Stream.this ).hasNext() ) {
            final Object obj = Stream.access$100 ( Stream.this ).next();
            this.val$le.invoke ( new Object[] { obj } );
            this.next = obj;
            this.foundNext = true;
        }
    }
}

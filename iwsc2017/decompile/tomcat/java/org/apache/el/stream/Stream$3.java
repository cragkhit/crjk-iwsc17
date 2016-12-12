package org.apache.el.stream;
import javax.el.LambdaExpression;
import java.util.Iterator;
class Stream$3 extends OpIterator {
    private Iterator<?> inner;
    final   LambdaExpression val$le;
    @Override
    protected void findNext() {
        while ( Stream.access$100 ( Stream.this ).hasNext() || ( this.inner != null && this.inner.hasNext() ) ) {
            if ( this.inner == null || !this.inner.hasNext() ) {
                this.inner = ( Iterator<?> ) Stream.access$100 ( ( Stream ) this.val$le.invoke ( new Object[] { Stream.access$100 ( Stream.this ).next() } ) );
            }
            if ( this.inner.hasNext() ) {
                this.next = this.inner.next();
                this.foundNext = true;
                break;
            }
        }
    }
}

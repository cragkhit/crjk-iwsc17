package org.apache.el.stream;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import javax.el.LambdaExpression;
import java.util.Iterator;
class Stream$6 extends OpIterator {
    private Iterator<Object> sorted = null;
    final   LambdaExpression val$le;
    @Override
    protected void findNext() {
        if ( this.sorted == null ) {
            this.sort ( this.val$le );
        }
        if ( this.sorted.hasNext() ) {
            this.next = this.sorted.next();
            this.foundNext = true;
        }
    }
    private final void sort ( final LambdaExpression le ) {
        final List list = new ArrayList();
        final Comparator<Object> c = new LambdaExpressionComparator ( le );
        while ( Stream.access$100 ( Stream.this ).hasNext() ) {
            list.add ( Stream.access$100 ( Stream.this ).next() );
        }
        Collections.sort ( ( List<Object> ) list, c );
        this.sorted = list.iterator();
    }
}

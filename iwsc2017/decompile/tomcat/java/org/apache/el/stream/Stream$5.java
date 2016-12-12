package org.apache.el.stream;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
class Stream$5 extends OpIterator {
    private Iterator<Object> sorted = null;
    @Override
    protected void findNext() {
        if ( this.sorted == null ) {
            this.sort();
        }
        if ( this.sorted.hasNext() ) {
            this.next = this.sorted.next();
            this.foundNext = true;
        }
    }
    private final void sort() {
        final List list = new ArrayList();
        while ( Stream.access$100 ( Stream.this ).hasNext() ) {
            list.add ( Stream.access$100 ( Stream.this ).next() );
        }
        Collections.sort ( ( List<Comparable> ) list );
        this.sorted = list.iterator();
    }
}

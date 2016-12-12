package org.apache.el.stream;
import java.util.HashSet;
import java.util.Set;
class Stream$4 extends OpIterator {
    private Set<Object> values = new HashSet<Object>();
    @Override
    protected void findNext() {
        while ( Stream.access$100 ( Stream.this ).hasNext() ) {
            final Object obj = Stream.access$100 ( Stream.this ).next();
            if ( this.values.add ( obj ) ) {
                this.next = obj;
                this.foundNext = true;
                break;
            }
        }
    }
}

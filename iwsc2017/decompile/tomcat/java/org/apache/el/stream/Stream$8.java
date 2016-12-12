package org.apache.el.stream;
class Stream$8 extends OpIterator {
    private final int startPos = this.val$start.intValue();
    private final int endPos = this.val$end.intValue();
    private int itemCount = 0;
    final   Number val$start;
    final   Number val$end;
    @Override
    protected void findNext() {
        while ( this.itemCount < this.startPos && Stream.access$100 ( Stream.this ).hasNext() ) {
            Stream.access$100 ( Stream.this ).next();
            ++this.itemCount;
        }
        if ( this.itemCount < this.endPos && Stream.access$100 ( Stream.this ).hasNext() ) {
            ++this.itemCount;
            this.next = Stream.access$100 ( Stream.this ).next();
            this.foundNext = true;
        }
    }
}

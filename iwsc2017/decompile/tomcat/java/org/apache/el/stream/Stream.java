package org.apache.el.stream;
import java.util.NoSuchElementException;
import javax.el.ELException;
import org.apache.el.util.MessageFactory;
import org.apache.el.lang.ELArithmetic;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.LambdaExpression;
import java.util.Iterator;
public class Stream {
    private final Iterator<Object> iterator;
    public Stream ( final Iterator<Object> iterator ) {
        this.iterator = iterator;
    }
    public Stream filter ( final LambdaExpression le ) {
        final Iterator<Object> downStream = new OpIterator() {
            @Override
            protected void findNext() {
                while ( Stream.this.iterator.hasNext() ) {
                    final Object obj = Stream.this.iterator.next();
                    if ( ELSupport.coerceToBoolean ( null, le.invoke ( new Object[] { obj } ), true ) ) {
                        this.next = obj;
                        this.foundNext = true;
                        break;
                    }
                }
            }
        };
        return new Stream ( downStream );
    }
    public Stream map ( final LambdaExpression le ) {
        final Iterator<Object> downStream = new OpIterator() {
            @Override
            protected void findNext() {
                if ( Stream.this.iterator.hasNext() ) {
                    final Object obj = Stream.this.iterator.next();
                    this.next = le.invoke ( new Object[] { obj } );
                    this.foundNext = true;
                }
            }
        };
        return new Stream ( downStream );
    }
    public Stream flatMap ( final LambdaExpression le ) {
        final Iterator<Object> downStream = new OpIterator() {
            private Iterator<?> inner;
            @Override
            protected void findNext() {
                while ( Stream.this.iterator.hasNext() || ( this.inner != null && this.inner.hasNext() ) ) {
                    if ( this.inner == null || !this.inner.hasNext() ) {
                        this.inner = ( ( Stream ) le.invoke ( new Object[] { Stream.this.iterator.next() } ) ).iterator;
                    }
                    if ( this.inner.hasNext() ) {
                        this.next = this.inner.next();
                        this.foundNext = true;
                        break;
                    }
                }
            }
        };
        return new Stream ( downStream );
    }
    public Stream distinct() {
        final Iterator<Object> downStream = new OpIterator() {
            private Set<Object> values = new HashSet<Object>();
            @Override
            protected void findNext() {
                while ( Stream.this.iterator.hasNext() ) {
                    final Object obj = Stream.this.iterator.next();
                    if ( this.values.add ( obj ) ) {
                        this.next = obj;
                        this.foundNext = true;
                        break;
                    }
                }
            }
        };
        return new Stream ( downStream );
    }
    public Stream sorted() {
        final Iterator<Object> downStream = new OpIterator() {
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
                while ( Stream.this.iterator.hasNext() ) {
                    list.add ( Stream.this.iterator.next() );
                }
                Collections.sort ( ( List<Comparable> ) list );
                this.sorted = list.iterator();
            }
        };
        return new Stream ( downStream );
    }
    public Stream sorted ( final LambdaExpression le ) {
        final Iterator<Object> downStream = new OpIterator() {
            private Iterator<Object> sorted = null;
            @Override
            protected void findNext() {
                if ( this.sorted == null ) {
                    this.sort ( le );
                }
                if ( this.sorted.hasNext() ) {
                    this.next = this.sorted.next();
                    this.foundNext = true;
                }
            }
            private final void sort ( final LambdaExpression le ) {
                final List list = new ArrayList();
                final Comparator<Object> c = new LambdaExpressionComparator ( le );
                while ( Stream.this.iterator.hasNext() ) {
                    list.add ( Stream.this.iterator.next() );
                }
                Collections.sort ( ( List<Object> ) list, c );
                this.sorted = list.iterator();
            }
        };
        return new Stream ( downStream );
    }
    public Object forEach ( final LambdaExpression le ) {
        while ( this.iterator.hasNext() ) {
            le.invoke ( new Object[] { this.iterator.next() } );
        }
        return null;
    }
    public Stream peek ( final LambdaExpression le ) {
        final Iterator<Object> downStream = new OpIterator() {
            @Override
            protected void findNext() {
                if ( Stream.this.iterator.hasNext() ) {
                    final Object obj = Stream.this.iterator.next();
                    le.invoke ( new Object[] { obj } );
                    this.next = obj;
                    this.foundNext = true;
                }
            }
        };
        return new Stream ( downStream );
    }
    public Iterator<?> iterator() {
        return this.iterator;
    }
    public Stream limit ( final Number count ) {
        return this.substream ( 0, count );
    }
    public Stream substream ( final Number start ) {
        return this.substream ( start, Integer.MAX_VALUE );
    }
    public Stream substream ( final Number start, final Number end ) {
        final Iterator<Object> downStream = new OpIterator() {
            private final int startPos = start.intValue();
            private final int endPos = end.intValue();
            private int itemCount = 0;
            @Override
            protected void findNext() {
                while ( this.itemCount < this.startPos && Stream.this.iterator.hasNext() ) {
                    Stream.this.iterator.next();
                    ++this.itemCount;
                }
                if ( this.itemCount < this.endPos && Stream.this.iterator.hasNext() ) {
                    ++this.itemCount;
                    this.next = Stream.this.iterator.next();
                    this.foundNext = true;
                }
            }
        };
        return new Stream ( downStream );
    }
    public List<Object> toList() {
        final List<Object> result = new ArrayList<Object>();
        while ( this.iterator.hasNext() ) {
            result.add ( this.iterator.next() );
        }
        return result;
    }
    public Object[] toArray() {
        final List<Object> result = new ArrayList<Object>();
        while ( this.iterator.hasNext() ) {
            result.add ( this.iterator.next() );
        }
        return result.toArray ( new Object[result.size()] );
    }
    public Optional reduce ( final LambdaExpression le ) {
        Object seed = null;
        if ( this.iterator.hasNext() ) {
            seed = this.iterator.next();
        }
        if ( seed == null ) {
            return Optional.EMPTY;
        }
        return new Optional ( this.reduce ( seed, le ) );
    }
    public Object reduce ( final Object seed, final LambdaExpression le ) {
        Object result = seed;
        while ( this.iterator.hasNext() ) {
            result = le.invoke ( new Object[] { result, this.iterator.next() } );
        }
        return result;
    }
    public Optional max() {
        return this.compare ( true );
    }
    public Optional max ( final LambdaExpression le ) {
        return this.compare ( true, le );
    }
    public Optional min() {
        return this.compare ( false );
    }
    public Optional min ( final LambdaExpression le ) {
        return this.compare ( false, le );
    }
    public Optional average() {
        long count = 0L;
        Number sum = 0L;
        while ( this.iterator.hasNext() ) {
            ++count;
            sum = ELArithmetic.add ( sum, this.iterator.next() );
        }
        if ( count == 0L ) {
            return Optional.EMPTY;
        }
        return new Optional ( ELArithmetic.divide ( sum, ( Object ) count ) );
    }
    public Number sum() {
        Number sum = 0L;
        while ( this.iterator.hasNext() ) {
            sum = ELArithmetic.add ( sum, this.iterator.next() );
        }
        return sum;
    }
    public Long count() {
        long count = 0L;
        while ( this.iterator.hasNext() ) {
            this.iterator.next();
            ++count;
        }
        return count;
    }
    public Optional anyMatch ( final LambdaExpression le ) {
        if ( !this.iterator.hasNext() ) {
            return Optional.EMPTY;
        }
        Boolean match;
        for ( match = Boolean.FALSE; !match && this.iterator.hasNext(); match = ( Boolean ) le.invoke ( new Object[] { this.iterator.next() } ) ) {}
        return new Optional ( match );
    }
    public Optional allMatch ( final LambdaExpression le ) {
        if ( !this.iterator.hasNext() ) {
            return Optional.EMPTY;
        }
        Boolean match;
        for ( match = Boolean.TRUE; match && this.iterator.hasNext(); match = ( Boolean ) le.invoke ( new Object[] { this.iterator.next() } ) ) {}
        return new Optional ( match );
    }
    public Optional noneMatch ( final LambdaExpression le ) {
        if ( !this.iterator.hasNext() ) {
            return Optional.EMPTY;
        }
        Boolean match;
        for ( match = Boolean.FALSE; !match && this.iterator.hasNext(); match = ( Boolean ) le.invoke ( new Object[] { this.iterator.next() } ) ) {}
        return new Optional ( !match );
    }
    public Optional findFirst() {
        if ( this.iterator.hasNext() ) {
            return new Optional ( this.iterator.next() );
        }
        return Optional.EMPTY;
    }
    private Optional compare ( final boolean isMax ) {
        Comparable result = null;
        if ( this.iterator.hasNext() ) {
            final Object obj = this.iterator.next();
            if ( ! ( obj instanceof Comparable ) ) {
                throw new ELException ( MessageFactory.get ( "stream.compare.notComparable" ) );
            }
            result = ( Comparable ) obj;
        }
        while ( this.iterator.hasNext() ) {
            final Object obj = this.iterator.next();
            if ( ! ( obj instanceof Comparable ) ) {
                throw new ELException ( MessageFactory.get ( "stream.compare.notComparable" ) );
            }
            if ( isMax && ( ( Comparable ) obj ).compareTo ( result ) > 0 ) {
                result = ( Comparable ) obj;
            } else {
                if ( isMax || ( ( Comparable ) obj ).compareTo ( result ) >= 0 ) {
                    continue;
                }
                result = ( Comparable ) obj;
            }
        }
        if ( result == null ) {
            return Optional.EMPTY;
        }
        return new Optional ( result );
    }
    private Optional compare ( final boolean isMax, final LambdaExpression le ) {
        Object result = null;
        if ( this.iterator.hasNext() ) {
            final Object obj = result = this.iterator.next();
        }
        while ( this.iterator.hasNext() ) {
            final Object obj = this.iterator.next();
            if ( isMax && ELSupport.coerceToNumber ( null, le.invoke ( new Object[] { obj, result } ), Integer.class ).intValue() > 0 ) {
                result = obj;
            }
            else {
                if ( isMax || ELSupport.coerceToNumber ( null, le.invoke ( new Object[] { obj, result } ), Integer.class ).intValue() >= 0 ) {
                    continue;
                }
                result = obj;
            }
        }
        if ( result == null ) {
            return Optional.EMPTY;
        }
        return new Optional ( result );
    }
    private static class LambdaExpressionComparator implements Comparator<Object> {
        private final LambdaExpression le;
        public LambdaExpressionComparator ( final LambdaExpression le ) {
            this.le = le;
        }
        @Override
        public int compare ( final Object o1, final Object o2 ) {
            return ELSupport.coerceToNumber ( null, this.le.invoke ( new Object[] { o1, o2 } ), Integer.class ).intValue();
        }
    }
    private abstract static class OpIterator implements Iterator<Object> {
        protected boolean foundNext;
        protected Object next;
        private OpIterator() {
            this.foundNext = false;
        }
        @Override
        public boolean hasNext() {
            if ( this.foundNext ) {
                return true;
            }
            this.findNext();
            return this.foundNext;
        }
        @Override
        public Object next() {
            if ( this.foundNext ) {
                this.foundNext = false;
                return this.next;
            }
            this.findNext();
            if ( this.foundNext ) {
                this.foundNext = false;
                return this.next;
            }
            throw new NoSuchElementException();
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        protected abstract void findNext();
    }
}

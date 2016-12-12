package org.apache.jasper.xmlparser;
public class SymbolTable {
    private static final int TABLE_SIZE = 101;
    private final Entry[] fBuckets;
    private final int fTableSize;
    public SymbolTable() {
        this ( 101 );
    }
    public SymbolTable ( final int tableSize ) {
        this.fTableSize = tableSize;
        this.fBuckets = new Entry[this.fTableSize];
    }
    public String addSymbol ( final char[] buffer, final int offset, final int length ) {
        final int bucket = this.hash ( buffer, offset, length ) % this.fTableSize;
        Label_0079:
        for ( Entry entry = this.fBuckets[bucket]; entry != null; entry = entry.next ) {
            if ( length == entry.characters.length ) {
                for ( int i = 0; i < length; ++i ) {
                    if ( buffer[offset + i] != entry.characters[i] ) {
                        continue Label_0079;
                    }
                }
                return entry.symbol;
            }
        }
        Entry entry = new Entry ( buffer, offset, length, this.fBuckets[bucket] );
        this.fBuckets[bucket] = entry;
        return entry.symbol;
    }
    public int hash ( final char[] buffer, final int offset, final int length ) {
        int code = 0;
        for ( int i = 0; i < length; ++i ) {
            code = code * 37 + buffer[offset + i];
        }
        return code & 0x7FFFFFF;
    }
    private static final class Entry {
        private final String symbol;
        private final char[] characters;
        private final Entry next;
        public Entry ( final char[] ch, final int offset, final int length, final Entry next ) {
            System.arraycopy ( ch, offset, this.characters = new char[length], 0, length );
            this.symbol = new String ( this.characters ).intern();
            this.next = next;
        }
    }
}

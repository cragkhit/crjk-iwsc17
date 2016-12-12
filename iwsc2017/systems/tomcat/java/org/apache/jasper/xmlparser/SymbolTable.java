package org.apache.jasper.xmlparser;
public class SymbolTable {
    private static final int TABLE_SIZE = 101;
    private final Entry[] fBuckets;
    private final int fTableSize;
    public SymbolTable() {
        this ( TABLE_SIZE );
    }
    public SymbolTable ( int tableSize ) {
        fTableSize = tableSize;
        fBuckets = new Entry[fTableSize];
    }
    public String addSymbol ( char[] buffer, int offset, int length ) {
        int bucket = hash ( buffer, offset, length ) % fTableSize;
        OUTER: for ( Entry entry = fBuckets[bucket]; entry != null; entry = entry.next ) {
            if ( length == entry.characters.length ) {
                for ( int i = 0; i < length; i++ ) {
                    if ( buffer[offset + i] != entry.characters[i] ) {
                        continue OUTER;
                    }
                }
                return entry.symbol;
            }
        }
        Entry entry = new Entry ( buffer, offset, length, fBuckets[bucket] );
        fBuckets[bucket] = entry;
        return entry.symbol;
    }
    public int hash ( char[] buffer, int offset, int length ) {
        int code = 0;
        for ( int i = 0; i < length; i++ ) {
            code = code * 37 + buffer[offset + i];
        }
        return code & 0x7FFFFFF;
    }
    private static final class Entry {
        private final String symbol;
        private final char[] characters;
        private final Entry next;
        public Entry ( char[] ch, int offset, int length, Entry next ) {
            characters = new char[length];
            System.arraycopy ( ch, offset, characters, 0, length );
            symbol = new String ( characters ).intern();
            this.next = next;
        }
    }
}

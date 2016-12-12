package org.apache.jasper.xmlparser;
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

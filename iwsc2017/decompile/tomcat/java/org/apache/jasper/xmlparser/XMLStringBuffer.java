package org.apache.jasper.xmlparser;
public class XMLStringBuffer extends XMLString {
    private static final int DEFAULT_SIZE = 32;
    public XMLStringBuffer() {
        this ( 32 );
    }
    public XMLStringBuffer ( final int size ) {
        this.ch = new char[size];
    }
    @Override
    public void clear() {
        this.offset = 0;
        this.length = 0;
    }
    public void append ( final char c ) {
        if ( this.length + 1 > this.ch.length ) {
            int newLength = this.ch.length * 2;
            if ( newLength < this.ch.length + 32 ) {
                newLength = this.ch.length + 32;
            }
            final char[] newch = new char[newLength];
            System.arraycopy ( this.ch, 0, newch, 0, this.length );
            this.ch = newch;
        }
        this.ch[this.length] = c;
        ++this.length;
    }
    public void append ( final String s ) {
        final int length = s.length();
        if ( this.length + length > this.ch.length ) {
            int newLength = this.ch.length * 2;
            if ( newLength < this.length + length + 32 ) {
                newLength = this.ch.length + length + 32;
            }
            final char[] newch = new char[newLength];
            System.arraycopy ( this.ch, 0, newch, 0, this.length );
            this.ch = newch;
        }
        s.getChars ( 0, length, this.ch, this.length );
        this.length += length;
    }
    public void append ( final char[] ch, final int offset, final int length ) {
        if ( this.length + length > this.ch.length ) {
            final char[] newch = new char[this.ch.length + length + 32];
            System.arraycopy ( this.ch, 0, newch, 0, this.length );
            this.ch = newch;
        }
        System.arraycopy ( ch, offset, this.ch, this.length, length );
        this.length += length;
    }
    public void append ( final XMLString s ) {
        this.append ( s.ch, s.offset, s.length );
    }
}

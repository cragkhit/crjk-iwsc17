package org.apache.jasper.xmlparser;
public class XMLStringBuffer
    extends XMLString {
    private static final int DEFAULT_SIZE = 32;
    public XMLStringBuffer() {
        this ( DEFAULT_SIZE );
    }
    public XMLStringBuffer ( int size ) {
        ch = new char[size];
    }
    @Override
    public void clear() {
        offset = 0;
        length = 0;
    }
    public void append ( char c ) {
        if ( this.length + 1 > this.ch.length ) {
            int newLength = this.ch.length * 2;
            if ( newLength < this.ch.length + DEFAULT_SIZE ) {
                newLength = this.ch.length + DEFAULT_SIZE;
            }
            char[] newch = new char[newLength];
            System.arraycopy ( this.ch, 0, newch, 0, this.length );
            this.ch = newch;
        }
        this.ch[this.length] = c;
        this.length++;
    }
    public void append ( String s ) {
        int length = s.length();
        if ( this.length + length > this.ch.length ) {
            int newLength = this.ch.length * 2;
            if ( newLength < this.length + length + DEFAULT_SIZE ) {
                newLength = this.ch.length + length + DEFAULT_SIZE;
            }
            char[] newch = new char[newLength];
            System.arraycopy ( this.ch, 0, newch, 0, this.length );
            this.ch = newch;
        }
        s.getChars ( 0, length, this.ch, this.length );
        this.length += length;
    }
    public void append ( char[] ch, int offset, int length ) {
        if ( this.length + length > this.ch.length ) {
            char[] newch = new char[this.ch.length + length + DEFAULT_SIZE];
            System.arraycopy ( this.ch, 0, newch, 0, this.length );
            this.ch = newch;
        }
        System.arraycopy ( ch, offset, this.ch, this.length, length );
        this.length += length;
    }
    public void append ( XMLString s ) {
        append ( s.ch, s.offset, s.length );
    }
}

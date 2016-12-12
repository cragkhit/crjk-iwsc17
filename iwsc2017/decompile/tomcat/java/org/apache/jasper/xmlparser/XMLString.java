package org.apache.jasper.xmlparser;
public class XMLString {
    public char[] ch;
    public int offset;
    public int length;
    public void setValues ( final char[] ch, final int offset, final int length ) {
        this.ch = ch;
        this.offset = offset;
        this.length = length;
    }
    public void setValues ( final XMLString s ) {
        this.setValues ( s.ch, s.offset, s.length );
    }
    public void clear() {
        this.ch = null;
        this.offset = 0;
        this.length = -1;
    }
    public boolean equals ( final String s ) {
        if ( s == null ) {
            return false;
        }
        if ( this.length != s.length() ) {
            return false;
        }
        for ( int i = 0; i < this.length; ++i ) {
            if ( this.ch[this.offset + i] != s.charAt ( i ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public String toString() {
        return ( this.length > 0 ) ? new String ( this.ch, this.offset, this.length ) : "";
    }
}

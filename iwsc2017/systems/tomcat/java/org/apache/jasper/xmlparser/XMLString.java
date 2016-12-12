package org.apache.jasper.xmlparser;
public class XMLString {
    public char[] ch;
    public int offset;
    public int length;
    public XMLString() {
    }
    public void setValues ( char[] ch, int offset, int length ) {
        this.ch = ch;
        this.offset = offset;
        this.length = length;
    }
    public void setValues ( XMLString s ) {
        setValues ( s.ch, s.offset, s.length );
    }
    public void clear() {
        this.ch = null;
        this.offset = 0;
        this.length = -1;
    }
    public boolean equals ( String s ) {
        if ( s == null ) {
            return false;
        }
        if ( length != s.length() ) {
            return false;
        }
        for ( int i = 0; i < length; i++ ) {
            if ( ch[offset + i] != s.charAt ( i ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public String toString() {
        return length > 0 ? new String ( ch, offset, length ) : "";
    }
}

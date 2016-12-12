package org.apache.tomcat.util.codec.binary;
import org.apache.tomcat.util.buf.HexUtils;
static class Context {
    int ibitWorkArea;
    long lbitWorkArea;
    byte[] buffer;
    int pos;
    int readPos;
    boolean eof;
    int currentLinePos;
    int modulus;
    @Override
    public String toString() {
        return String.format ( "%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, modulus=%s, pos=%s, readPos=%s]", this.getClass().getSimpleName(), HexUtils.toHexString ( this.buffer ), this.currentLinePos, this.eof, this.ibitWorkArea, this.lbitWorkArea, this.modulus, this.pos, this.readPos );
    }
}

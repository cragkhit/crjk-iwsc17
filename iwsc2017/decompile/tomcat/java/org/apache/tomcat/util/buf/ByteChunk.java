package org.apache.tomcat.util.buf;
import java.nio.charset.StandardCharsets;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.Serializable;
public final class ByteChunk implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Charset DEFAULT_CHARSET;
    private int hashCode;
    private boolean hasHashCode;
    private byte[] buff;
    private int start;
    private int end;
    private Charset charset;
    private boolean isSet;
    private int limit;
    private ByteInputChannel in;
    private ByteOutputChannel out;
    public ByteChunk() {
        this.hashCode = 0;
        this.hasHashCode = false;
        this.start = 0;
        this.isSet = false;
        this.limit = -1;
        this.in = null;
        this.out = null;
    }
    public ByteChunk ( final int initial ) {
        this.hashCode = 0;
        this.hasHashCode = false;
        this.start = 0;
        this.isSet = false;
        this.limit = -1;
        this.in = null;
        this.out = null;
        this.allocate ( initial, -1 );
    }
    public boolean isNull() {
        return !this.isSet;
    }
    public void recycle() {
        this.charset = null;
        this.start = 0;
        this.end = 0;
        this.isSet = false;
        this.hasHashCode = false;
    }
    public void allocate ( final int initial, final int limit ) {
        if ( this.buff == null || this.buff.length < initial ) {
            this.buff = new byte[initial];
        }
        this.limit = limit;
        this.start = 0;
        this.end = 0;
        this.isSet = true;
        this.hasHashCode = false;
    }
    public void setBytes ( final byte[] b, final int off, final int len ) {
        this.buff = b;
        this.start = off;
        this.end = this.start + len;
        this.isSet = true;
        this.hasHashCode = false;
    }
    public void setCharset ( final Charset charset ) {
        this.charset = charset;
    }
    public Charset getCharset() {
        if ( this.charset == null ) {
            this.charset = ByteChunk.DEFAULT_CHARSET;
        }
        return this.charset;
    }
    public byte[] getBytes() {
        return this.getBuffer();
    }
    public byte[] getBuffer() {
        return this.buff;
    }
    public int getStart() {
        return this.start;
    }
    public int getOffset() {
        return this.start;
    }
    public void setOffset ( final int off ) {
        if ( this.end < off ) {
            this.end = off;
        }
        this.start = off;
    }
    public int getLength() {
        return this.end - this.start;
    }
    public void setLimit ( final int limit ) {
        this.limit = limit;
    }
    public int getLimit() {
        return this.limit;
    }
    public void setByteInputChannel ( final ByteInputChannel in ) {
        this.in = in;
    }
    public void setByteOutputChannel ( final ByteOutputChannel out ) {
        this.out = out;
    }
    public int getEnd() {
        return this.end;
    }
    public void setEnd ( final int i ) {
        this.end = i;
    }
    public void append ( final byte b ) throws IOException {
        this.makeSpace ( 1 );
        if ( this.limit > 0 && this.end >= this.limit ) {
            this.flushBuffer();
        }
        this.buff[this.end++] = b;
    }
    public void append ( final ByteChunk src ) throws IOException {
        this.append ( src.getBytes(), src.getStart(), src.getLength() );
    }
    public void append ( final byte[] src, final int off, final int len ) throws IOException {
        this.makeSpace ( len );
        if ( this.limit < 0 ) {
            System.arraycopy ( src, off, this.buff, this.end, len );
            this.end += len;
            return;
        }
        if ( len == this.limit && this.end == this.start && this.out != null ) {
            this.out.realWriteBytes ( src, off, len );
            return;
        }
        if ( len <= this.limit - this.end ) {
            System.arraycopy ( src, off, this.buff, this.end, len );
            this.end += len;
            return;
        }
        final int avail = this.limit - this.end;
        System.arraycopy ( src, off, this.buff, this.end, avail );
        this.end += avail;
        this.flushBuffer();
        int remain;
        for ( remain = len - avail; remain > this.limit - this.end; remain -= this.limit - this.end ) {
            this.out.realWriteBytes ( src, off + len - remain, this.limit - this.end );
        }
        System.arraycopy ( src, off + len - remain, this.buff, this.end, remain );
        this.end += remain;
    }
    public void append ( final ByteBuffer from ) throws IOException {
        final int len = from.remaining();
        this.makeSpace ( len );
        if ( this.limit < 0 ) {
            from.get ( this.buff, this.end, len );
            this.end += len;
            return;
        }
        if ( len == this.limit && this.end == this.start && this.out != null ) {
            this.out.realWriteBytes ( from );
            from.position ( from.limit() );
            return;
        }
        if ( len <= this.limit - this.end ) {
            from.get ( this.buff, this.end, len );
            this.end += len;
            return;
        }
        int avail = this.limit - this.end;
        from.get ( this.buff, this.end, avail );
        this.end += avail;
        this.flushBuffer();
        final int fromLimit = from.limit();
        int remain;
        for ( remain = len - avail, avail = this.limit - this.end; remain >= avail; remain -= avail ) {
            from.limit ( from.position() + avail );
            this.out.realWriteBytes ( from );
            from.position ( from.limit() );
        }
        from.limit ( fromLimit );
        from.get ( this.buff, this.end, remain );
        this.end += remain;
    }
    public int substract() throws IOException {
        if ( this.checkEof() ) {
            return -1;
        }
        return this.buff[this.start++] & 0xFF;
    }
    public byte substractB() throws IOException {
        if ( this.checkEof() ) {
            return -1;
        }
        return this.buff[this.start++];
    }
    public int substract ( final byte[] dest, final int off, final int len ) throws IOException {
        if ( this.checkEof() ) {
            return -1;
        }
        int n;
        if ( ( n = len ) > this.getLength() ) {
            n = this.getLength();
        }
        System.arraycopy ( this.buff, this.start, dest, off, n );
        this.start += n;
        return n;
    }
    public int substract ( final ByteBuffer to ) throws IOException {
        if ( this.checkEof() ) {
            return -1;
        }
        final int n = Math.min ( to.remaining(), this.getLength() );
        to.put ( this.buff, this.start, n );
        to.limit ( to.position() );
        to.position ( to.position() - n );
        this.start += n;
        return n;
    }
    private boolean checkEof() throws IOException {
        if ( this.end - this.start == 0 ) {
            if ( this.in == null ) {
                return true;
            }
            final int n = this.in.realReadBytes();
            if ( n < 0 ) {
                return true;
            }
        }
        return false;
    }
    public void flushBuffer() throws IOException {
        if ( this.out == null ) {
            throw new IOException ( "Buffer overflow, no sink " + this.limit + " " + this.buff.length );
        }
        this.out.realWriteBytes ( this.buff, this.start, this.end - this.start );
        this.end = this.start;
    }
    public void makeSpace ( final int count ) {
        byte[] tmp = null;
        int desiredSize = this.end + count;
        if ( this.limit > 0 && desiredSize > this.limit ) {
            desiredSize = this.limit;
        }
        if ( this.buff == null ) {
            if ( desiredSize < 256 ) {
                desiredSize = 256;
            }
            this.buff = new byte[desiredSize];
        }
        if ( desiredSize <= this.buff.length ) {
            return;
        }
        int newSize;
        if ( desiredSize < 2 * this.buff.length ) {
            newSize = this.buff.length * 2;
        } else {
            newSize = this.buff.length * 2 + count;
        }
        if ( this.limit > 0 && newSize > this.limit ) {
            newSize = this.limit;
        }
        tmp = new byte[newSize];
        System.arraycopy ( this.buff, this.start, tmp, 0, this.end - this.start );
        this.buff = tmp;
        tmp = null;
        this.end -= this.start;
        this.start = 0;
    }
    @Override
    public String toString() {
        if ( null == this.buff ) {
            return null;
        }
        if ( this.end - this.start == 0 ) {
            return "";
        }
        return StringCache.toString ( this );
    }
    public String toStringInternal() {
        if ( this.charset == null ) {
            this.charset = ByteChunk.DEFAULT_CHARSET;
        }
        final CharBuffer cb = this.charset.decode ( ByteBuffer.wrap ( this.buff, this.start, this.end - this.start ) );
        return new String ( cb.array(), cb.arrayOffset(), cb.length() );
    }
    public long getLong() {
        return Ascii.parseLong ( this.buff, this.start, this.end - this.start );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj instanceof ByteChunk && this.equals ( ( ByteChunk ) obj );
    }
    public boolean equals ( final String s ) {
        final byte[] b = this.buff;
        final int blen = this.end - this.start;
        if ( b == null || blen != s.length() ) {
            return false;
        }
        int boff = this.start;
        for ( int i = 0; i < blen; ++i ) {
            if ( b[boff++] != s.charAt ( i ) ) {
                return false;
            }
        }
        return true;
    }
    public boolean equalsIgnoreCase ( final String s ) {
        final byte[] b = this.buff;
        final int blen = this.end - this.start;
        if ( b == null || blen != s.length() ) {
            return false;
        }
        int boff = this.start;
        for ( int i = 0; i < blen; ++i ) {
            if ( Ascii.toLower ( b[boff++] ) != Ascii.toLower ( s.charAt ( i ) ) ) {
                return false;
            }
        }
        return true;
    }
    public boolean equals ( final ByteChunk bb ) {
        return this.equals ( bb.getBytes(), bb.getStart(), bb.getLength() );
    }
    public boolean equals ( final byte[] b2, int off2, final int len2 ) {
        final byte[] b3 = this.buff;
        if ( b3 == null && b2 == null ) {
            return true;
        }
        int len3 = this.end - this.start;
        if ( len2 != len3 || b3 == null || b2 == null ) {
            return false;
        }
        int off3 = this.start;
        while ( len3-- > 0 ) {
            if ( b3[off3++] != b2[off2++] ) {
                return false;
            }
        }
        return true;
    }
    public boolean equals ( final CharChunk cc ) {
        return this.equals ( cc.getChars(), cc.getStart(), cc.getLength() );
    }
    public boolean equals ( final char[] c2, int off2, final int len2 ) {
        final byte[] b1 = this.buff;
        if ( c2 == null && b1 == null ) {
            return true;
        }
        if ( b1 == null || c2 == null || this.end - this.start != len2 ) {
            return false;
        }
        int off3 = this.start;
        int len3 = this.end - this.start;
        while ( len3-- > 0 ) {
            if ( ( char ) b1[off3++] != c2[off2++] ) {
                return false;
            }
        }
        return true;
    }
    public boolean startsWithIgnoreCase ( final String s, final int pos ) {
        final byte[] b = this.buff;
        final int len = s.length();
        if ( b == null || len + pos > this.end - this.start ) {
            return false;
        }
        int off = this.start + pos;
        for ( int i = 0; i < len; ++i ) {
            if ( Ascii.toLower ( b[off++] ) != Ascii.toLower ( s.charAt ( i ) ) ) {
                return false;
            }
        }
        return true;
    }
    public int indexOf ( final String src, final int srcOff, final int srcLen, final int myOff ) {
        final char first = src.charAt ( srcOff );
        final int srcEnd = srcOff + srcLen;
        Label_0098:
        for ( int i = myOff + this.start; i <= this.end - srcLen; ++i ) {
            if ( this.buff[i] == first ) {
                int myPos = i + 1;
                int srcPos = srcOff + 1;
                while ( srcPos < srcEnd ) {
                    if ( this.buff[myPos++] != src.charAt ( srcPos++ ) ) {
                        continue Label_0098;
                    }
                }
                return i - this.start;
            }
        }
        return -1;
    }
    @Override
    public int hashCode() {
        if ( this.hasHashCode ) {
            return this.hashCode;
        }
        int code = 0;
        code = this.hash();
        this.hashCode = code;
        this.hasHashCode = true;
        return code;
    }
    public int hash() {
        return hashBytes ( this.buff, this.start, this.end - this.start );
    }
    private static int hashBytes ( final byte[] buff, final int start, final int bytesLen ) {
        final int max = start + bytesLen;
        int code = 0;
        for ( int i = start; i < max; ++i ) {
            code = code * 37 + buff[i];
        }
        return code;
    }
    public int indexOf ( final char c, final int starting ) {
        final int ret = indexOf ( this.buff, this.start + starting, this.end, c );
        return ( ret >= this.start ) ? ( ret - this.start ) : -1;
    }
    public static int indexOf ( final byte[] bytes, final int start, final int end, final char c ) {
        for ( int offset = start; offset < end; ++offset ) {
            final byte b = bytes[offset];
            if ( b == c ) {
                return offset;
            }
        }
        return -1;
    }
    public static int findByte ( final byte[] bytes, final int start, final int end, final byte b ) {
        for ( int offset = start; offset < end; ++offset ) {
            if ( bytes[offset] == b ) {
                return offset;
            }
        }
        return -1;
    }
    public static int findBytes ( final byte[] bytes, final int start, final int end, final byte[] b ) {
        final int blen = b.length;
        for ( int offset = start; offset < end; ++offset ) {
            for ( int i = 0; i < blen; ++i ) {
                if ( bytes[offset] == b[i] ) {
                    return offset;
                }
            }
        }
        return -1;
    }
    public static final byte[] convertToBytes ( final String value ) {
        final byte[] result = new byte[value.length()];
        for ( int i = 0; i < value.length(); ++i ) {
            result[i] = ( byte ) value.charAt ( i );
        }
        return result;
    }
    static {
        DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    }
    public interface ByteOutputChannel {
        void realWriteBytes ( byte[] p0, int p1, int p2 ) throws IOException;
        void realWriteBytes ( ByteBuffer p0 ) throws IOException;
    }
    public interface ByteInputChannel {
        int realReadBytes() throws IOException;
    }
}

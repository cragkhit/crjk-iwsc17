package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
public final class ByteChunk implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    public static interface ByteInputChannel {
        public int realReadBytes() throws IOException;
    }
    public static interface ByteOutputChannel {
        public void realWriteBytes ( byte cbuf[], int off, int len )
        throws IOException;
        public void realWriteBytes ( ByteBuffer from ) throws IOException;
    }
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private int hashCode = 0;
    private boolean hasHashCode = false;
    private byte[] buff;
    private int start = 0;
    private int end;
    private Charset charset;
    private boolean isSet = false;
    private int limit = -1;
    private ByteInputChannel in = null;
    private ByteOutputChannel out = null;
    public ByteChunk() {
    }
    public ByteChunk ( int initial ) {
        allocate ( initial, -1 );
    }
    public boolean isNull() {
        return ! isSet;
    }
    public void recycle() {
        charset = null;
        start = 0;
        end = 0;
        isSet = false;
        hasHashCode = false;
    }
    public void allocate ( int initial, int limit ) {
        if ( buff == null || buff.length < initial ) {
            buff = new byte[initial];
        }
        this.limit = limit;
        start = 0;
        end = 0;
        isSet = true;
        hasHashCode = false;
    }
    public void setBytes ( byte[] b, int off, int len ) {
        buff = b;
        start = off;
        end = start + len;
        isSet = true;
        hasHashCode = false;
    }
    public void setCharset ( Charset charset ) {
        this.charset = charset;
    }
    public Charset getCharset() {
        if ( charset == null ) {
            charset = DEFAULT_CHARSET;
        }
        return charset;
    }
    public byte[] getBytes() {
        return getBuffer();
    }
    public byte[] getBuffer() {
        return buff;
    }
    public int getStart() {
        return start;
    }
    public int getOffset() {
        return start;
    }
    public void setOffset ( int off ) {
        if ( end < off ) {
            end = off;
        }
        start = off;
    }
    public int getLength() {
        return end - start;
    }
    public void setLimit ( int limit ) {
        this.limit = limit;
    }
    public int getLimit() {
        return limit;
    }
    public void setByteInputChannel ( ByteInputChannel in ) {
        this.in = in;
    }
    public void setByteOutputChannel ( ByteOutputChannel out ) {
        this.out = out;
    }
    public int getEnd() {
        return end;
    }
    public void setEnd ( int i ) {
        end = i;
    }
    public void append ( byte b )
    throws IOException {
        makeSpace ( 1 );
        if ( limit > 0 && end >= limit ) {
            flushBuffer();
        }
        buff[end++] = b;
    }
    public void append ( ByteChunk src )
    throws IOException {
        append ( src.getBytes(), src.getStart(), src.getLength() );
    }
    public void append ( byte src[], int off, int len ) throws IOException {
        makeSpace ( len );
        if ( limit < 0 ) {
            System.arraycopy ( src, off, buff, end, len );
            end += len;
            return;
        }
        if ( len == limit && end == start && out != null ) {
            out.realWriteBytes ( src, off, len );
            return;
        }
        if ( len <= limit - end ) {
            System.arraycopy ( src, off, buff, end, len );
            end += len;
            return;
        }
        int avail = limit - end;
        System.arraycopy ( src, off, buff, end, avail );
        end += avail;
        flushBuffer();
        int remain = len - avail;
        while ( remain > ( limit - end ) ) {
            out.realWriteBytes ( src, ( off + len ) - remain, limit - end );
            remain = remain - ( limit - end );
        }
        System.arraycopy ( src, ( off + len ) - remain, buff, end, remain );
        end += remain;
    }
    public void append ( ByteBuffer from ) throws IOException {
        int len = from.remaining();
        makeSpace ( len );
        if ( limit < 0 ) {
            from.get ( buff, end, len );
            end += len;
            return;
        }
        if ( len == limit && end == start && out != null ) {
            out.realWriteBytes ( from );
            from.position ( from.limit() );
            return;
        }
        if ( len <= limit - end ) {
            from.get ( buff, end, len );
            end += len;
            return;
        }
        int avail = limit - end;
        from.get ( buff, end, avail );
        end += avail;
        flushBuffer();
        int fromLimit = from.limit();
        int remain = len - avail;
        avail = limit - end;
        while ( remain >= avail ) {
            from.limit ( from.position() + avail );
            out.realWriteBytes ( from );
            from.position ( from.limit() );
            remain = remain - avail;
        }
        from.limit ( fromLimit );
        from.get ( buff, end, remain );
        end += remain;
    }
    public int substract() throws IOException {
        if ( checkEof() ) {
            return -1;
        }
        return buff[start++] & 0xFF;
    }
    public byte substractB() throws IOException {
        if ( checkEof() ) {
            return -1;
        }
        return buff[start++];
    }
    public int substract ( byte dest[], int off, int len ) throws IOException {
        if ( checkEof() ) {
            return -1;
        }
        int n = len;
        if ( len > getLength() ) {
            n = getLength();
        }
        System.arraycopy ( buff, start, dest, off, n );
        start += n;
        return n;
    }
    public int substract ( ByteBuffer to ) throws IOException {
        if ( checkEof() ) {
            return -1;
        }
        int n = Math.min ( to.remaining(), getLength() );
        to.put ( buff, start, n );
        to.limit ( to.position() );
        to.position ( to.position() - n );
        start += n;
        return n;
    }
    private boolean checkEof() throws IOException {
        if ( ( end - start ) == 0 ) {
            if ( in == null ) {
                return true;
            }
            int n = in.realReadBytes();
            if ( n < 0 ) {
                return true;
            }
        }
        return false;
    }
    public void flushBuffer()
    throws IOException {
        if ( out == null ) {
            throw new IOException ( "Buffer overflow, no sink " + limit + " " +
                                    buff.length );
        }
        out.realWriteBytes ( buff, start, end - start );
        end = start;
    }
    public void makeSpace ( int count ) {
        byte[] tmp = null;
        int newSize;
        int desiredSize = end + count;
        if ( limit > 0 &&
                desiredSize > limit ) {
            desiredSize = limit;
        }
        if ( buff == null ) {
            if ( desiredSize < 256 ) {
                desiredSize = 256;
            }
            buff = new byte[desiredSize];
        }
        if ( desiredSize <= buff.length ) {
            return;
        }
        if ( desiredSize < 2 * buff.length ) {
            newSize = buff.length * 2;
        } else {
            newSize = buff.length * 2 + count ;
        }
        if ( limit > 0 && newSize > limit ) {
            newSize = limit;
        }
        tmp = new byte[newSize];
        System.arraycopy ( buff, start, tmp, 0, end - start );
        buff = tmp;
        tmp = null;
        end = end - start;
        start = 0;
    }
    @Override
    public String toString() {
        if ( null == buff ) {
            return null;
        } else if ( end - start == 0 ) {
            return "";
        }
        return StringCache.toString ( this );
    }
    public String toStringInternal() {
        if ( charset == null ) {
            charset = DEFAULT_CHARSET;
        }
        CharBuffer cb = charset.decode ( ByteBuffer.wrap ( buff, start, end - start ) );
        return new String ( cb.array(), cb.arrayOffset(), cb.length() );
    }
    public long getLong() {
        return Ascii.parseLong ( buff, start, end - start );
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof ByteChunk ) {
            return equals ( ( ByteChunk ) obj );
        }
        return false;
    }
    public boolean equals ( String s ) {
        byte[] b = buff;
        int blen = end - start;
        if ( b == null || blen != s.length() ) {
            return false;
        }
        int boff = start;
        for ( int i = 0; i < blen; i++ ) {
            if ( b[boff++] != s.charAt ( i ) ) {
                return false;
            }
        }
        return true;
    }
    public boolean equalsIgnoreCase ( String s ) {
        byte[] b = buff;
        int blen = end - start;
        if ( b == null || blen != s.length() ) {
            return false;
        }
        int boff = start;
        for ( int i = 0; i < blen; i++ ) {
            if ( Ascii.toLower ( b[boff++] ) != Ascii.toLower ( s.charAt ( i ) ) ) {
                return false;
            }
        }
        return true;
    }
    public boolean equals ( ByteChunk bb ) {
        return equals ( bb.getBytes(), bb.getStart(), bb.getLength() );
    }
    public boolean equals ( byte b2[], int off2, int len2 ) {
        byte b1[] = buff;
        if ( b1 == null && b2 == null ) {
            return true;
        }
        int len = end - start;
        if ( len2 != len || b1 == null || b2 == null ) {
            return false;
        }
        int off1 = start;
        while ( len-- > 0 ) {
            if ( b1[off1++] != b2[off2++] ) {
                return false;
            }
        }
        return true;
    }
    public boolean equals ( CharChunk cc ) {
        return equals ( cc.getChars(), cc.getStart(), cc.getLength() );
    }
    public boolean equals ( char c2[], int off2, int len2 ) {
        byte b1[] = buff;
        if ( c2 == null && b1 == null ) {
            return true;
        }
        if ( b1 == null || c2 == null || end - start != len2 ) {
            return false;
        }
        int off1 = start;
        int len = end - start;
        while ( len-- > 0 ) {
            if ( ( char ) b1[off1++] != c2[off2++] ) {
                return false;
            }
        }
        return true;
    }
    public boolean startsWithIgnoreCase ( String s, int pos ) {
        byte[] b = buff;
        int len = s.length();
        if ( b == null || len + pos > end - start ) {
            return false;
        }
        int off = start + pos;
        for ( int i = 0; i < len; i++ ) {
            if ( Ascii.toLower ( b[off++] ) != Ascii.toLower ( s.charAt ( i ) ) ) {
                return false;
            }
        }
        return true;
    }
    public int indexOf ( String src, int srcOff, int srcLen, int myOff ) {
        char first = src.charAt ( srcOff );
        int srcEnd = srcOff + srcLen;
        mainLoop:
        for ( int i = myOff + start; i <= ( end - srcLen ); i++ ) {
            if ( buff[i] != first ) {
                continue;
            }
            int myPos = i + 1;
            for ( int srcPos = srcOff + 1; srcPos < srcEnd; ) {
                if ( buff[myPos++] != src.charAt ( srcPos++ ) ) {
                    continue mainLoop;
                }
            }
            return i - start;
        }
        return -1;
    }
    @Override
    public int hashCode() {
        if ( hasHashCode ) {
            return hashCode;
        }
        int code = 0;
        code = hash();
        hashCode = code;
        hasHashCode = true;
        return code;
    }
    public int hash() {
        return hashBytes ( buff, start, end - start );
    }
    private static int hashBytes ( byte buff[], int start, int bytesLen ) {
        int max = start + bytesLen;
        byte bb[] = buff;
        int code = 0;
        for ( int i = start; i < max ; i++ ) {
            code = code * 37 + bb[i];
        }
        return code;
    }
    public int indexOf ( char c, int starting ) {
        int ret = indexOf ( buff, start + starting, end, c );
        return ( ret >= start ) ? ret - start : -1;
    }
    public static int indexOf ( byte bytes[], int start, int end, char c ) {
        int offset = start;
        while ( offset < end ) {
            byte b = bytes[offset];
            if ( b == c ) {
                return offset;
            }
            offset++;
        }
        return -1;
    }
    public static int findByte ( byte bytes[], int start, int end, byte b ) {
        int offset = start;
        while ( offset < end ) {
            if ( bytes[offset] == b ) {
                return offset;
            }
            offset++;
        }
        return -1;
    }
    public static int findBytes ( byte bytes[], int start, int end, byte b[] ) {
        int blen = b.length;
        int offset = start;
        while ( offset < end ) {
            for ( int i = 0;  i < blen; i++ ) {
                if ( bytes[offset] == b[i] ) {
                    return offset;
                }
            }
            offset++;
        }
        return -1;
    }
    public static final byte[] convertToBytes ( String value ) {
        byte[] result = new byte[value.length()];
        for ( int i = 0; i < value.length(); i++ ) {
            result[i] = ( byte ) value.charAt ( i );
        }
        return result;
    }
}

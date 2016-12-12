package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
public final class MessageBytes implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    private int type = T_NULL;
    public static final int T_NULL = 0;
    public static final int T_STR  = 1;
    public static final int T_BYTES = 2;
    public static final int T_CHARS = 3;
    private int hashCode = 0;
    private boolean hasHashCode = false;
    private final ByteChunk byteC = new ByteChunk();
    private final CharChunk charC = new CharChunk();
    private String strValue;
    private boolean hasStrValue = false;
    private MessageBytes() {
    }
    public static MessageBytes newInstance() {
        return factory.newInstance();
    }
    public boolean isNull() {
        return byteC.isNull() && charC.isNull() && ! hasStrValue;
    }
    public void recycle() {
        type = T_NULL;
        byteC.recycle();
        charC.recycle();
        strValue = null;
        hasStrValue = false;
        hasHashCode = false;
        hasLongValue = false;
    }
    public void setBytes ( byte[] b, int off, int len ) {
        byteC.setBytes ( b, off, len );
        type = T_BYTES;
        hasStrValue = false;
        hasHashCode = false;
        hasLongValue = false;
    }
    public void setChars ( char[] c, int off, int len ) {
        charC.setChars ( c, off, len );
        type = T_CHARS;
        hasStrValue = false;
        hasHashCode = false;
        hasLongValue = false;
    }
    public void setString ( String s ) {
        strValue = s;
        hasHashCode = false;
        hasLongValue = false;
        if ( s == null ) {
            hasStrValue = false;
            type = T_NULL;
        } else {
            hasStrValue = true;
            type = T_STR;
        }
    }
    @Override
    public String toString() {
        if ( hasStrValue ) {
            return strValue;
        }
        switch ( type ) {
        case T_CHARS:
            strValue = charC.toString();
            hasStrValue = true;
            return strValue;
        case T_BYTES:
            strValue = byteC.toString();
            hasStrValue = true;
            return strValue;
        }
        return null;
    }
    public int getType() {
        return type;
    }
    public ByteChunk getByteChunk() {
        return byteC;
    }
    public CharChunk getCharChunk() {
        return charC;
    }
    public String getString() {
        return strValue;
    }
    public Charset getCharset() {
        return byteC.getCharset();
    }
    public void setCharset ( Charset charset ) {
        byteC.setCharset ( charset );
    }
    public void toBytes() {
        if ( !byteC.isNull() ) {
            type = T_BYTES;
            return;
        }
        toString();
        type = T_BYTES;
        Charset charset = byteC.getCharset();
        ByteBuffer result = charset.encode ( strValue );
        byteC.setBytes ( result.array(), result.arrayOffset(), result.limit() );
    }
    public void toChars() {
        if ( ! charC.isNull() ) {
            type = T_CHARS;
            return;
        }
        toString();
        type = T_CHARS;
        char cc[] = strValue.toCharArray();
        charC.setChars ( cc, 0, cc.length );
    }
    public int getLength() {
        if ( type == T_BYTES ) {
            return byteC.getLength();
        }
        if ( type == T_CHARS ) {
            return charC.getLength();
        }
        if ( type == T_STR ) {
            return strValue.length();
        }
        toString();
        if ( strValue == null ) {
            return 0;
        }
        return strValue.length();
    }
    public boolean equals ( String s ) {
        switch ( type ) {
        case T_STR:
            if ( strValue == null ) {
                return s == null;
            }
            return strValue.equals ( s );
        case T_CHARS:
            return charC.equals ( s );
        case T_BYTES:
            return byteC.equals ( s );
        default:
            return false;
        }
    }
    public boolean equalsIgnoreCase ( String s ) {
        switch ( type ) {
        case T_STR:
            if ( strValue == null ) {
                return s == null;
            }
            return strValue.equalsIgnoreCase ( s );
        case T_CHARS:
            return charC.equalsIgnoreCase ( s );
        case T_BYTES:
            return byteC.equalsIgnoreCase ( s );
        default:
            return false;
        }
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof MessageBytes ) {
            return equals ( ( MessageBytes ) obj );
        }
        return false;
    }
    public boolean equals ( MessageBytes mb ) {
        switch ( type ) {
        case T_STR:
            return mb.equals ( strValue );
        }
        if ( mb.type != T_CHARS &&
                mb.type != T_BYTES ) {
            return equals ( mb.toString() );
        }
        if ( mb.type == T_CHARS && type == T_CHARS ) {
            return charC.equals ( mb.charC );
        }
        if ( mb.type == T_BYTES && type == T_BYTES ) {
            return byteC.equals ( mb.byteC );
        }
        if ( mb.type == T_CHARS && type == T_BYTES ) {
            return byteC.equals ( mb.charC );
        }
        if ( mb.type == T_BYTES && type == T_CHARS ) {
            return mb.byteC.equals ( charC );
        }
        return true;
    }
    public boolean startsWithIgnoreCase ( String s, int pos ) {
        switch ( type ) {
        case T_STR:
            if ( strValue == null ) {
                return false;
            }
            if ( strValue.length() < pos + s.length() ) {
                return false;
            }
            for ( int i = 0; i < s.length(); i++ ) {
                if ( Ascii.toLower ( s.charAt ( i ) ) !=
                        Ascii.toLower ( strValue.charAt ( pos + i ) ) ) {
                    return false;
                }
            }
            return true;
        case T_CHARS:
            return charC.startsWithIgnoreCase ( s, pos );
        case T_BYTES:
            return byteC.startsWithIgnoreCase ( s, pos );
        default:
            return false;
        }
    }
    @Override
    public  int hashCode() {
        if ( hasHashCode ) {
            return hashCode;
        }
        int code = 0;
        code = hash();
        hashCode = code;
        hasHashCode = true;
        return code;
    }
    private int hash() {
        int code = 0;
        switch ( type ) {
        case T_STR:
            for ( int i = 0; i < strValue.length(); i++ ) {
                code = code * 37 + strValue.charAt ( i );
            }
            return code;
        case T_CHARS:
            return charC.hash();
        case T_BYTES:
            return byteC.hash();
        default:
            return 0;
        }
    }
    public int indexOf ( String s, int starting ) {
        toString();
        return strValue.indexOf ( s, starting );
    }
    public int indexOf ( String s ) {
        return indexOf ( s, 0 );
    }
    public int indexOfIgnoreCase ( String s, int starting ) {
        toString();
        String upper = strValue.toUpperCase ( Locale.ENGLISH );
        String sU = s.toUpperCase ( Locale.ENGLISH );
        return upper.indexOf ( sU, starting );
    }
    public void duplicate ( MessageBytes src ) throws IOException {
        switch ( src.getType() ) {
        case MessageBytes.T_BYTES:
            type = T_BYTES;
            ByteChunk bc = src.getByteChunk();
            byteC.allocate ( 2 * bc.getLength(), -1 );
            byteC.append ( bc );
            break;
        case MessageBytes.T_CHARS:
            type = T_CHARS;
            CharChunk cc = src.getCharChunk();
            charC.allocate ( 2 * cc.getLength(), -1 );
            charC.append ( cc );
            break;
        case MessageBytes.T_STR:
            type = T_STR;
            String sc = src.getString();
            this.setString ( sc );
            break;
        }
        setCharset ( src.getCharset() );
    }
    private long longValue;
    private boolean hasLongValue = false;
    public void setLong ( long l ) {
        byteC.allocate ( 32, 64 );
        long current = l;
        byte[] buf = byteC.getBuffer();
        int start = 0;
        int end = 0;
        if ( l == 0 ) {
            buf[end++] = ( byte ) '0';
        }
        if ( l < 0 ) {
            current = -l;
            buf[end++] = ( byte ) '-';
        }
        while ( current > 0 ) {
            int digit = ( int ) ( current % 10 );
            current = current / 10;
            buf[end++] = HexUtils.getHex ( digit );
        }
        byteC.setOffset ( 0 );
        byteC.setEnd ( end );
        end--;
        if ( l < 0 ) {
            start++;
        }
        while ( end > start ) {
            byte temp = buf[start];
            buf[start] = buf[end];
            buf[end] = temp;
            start++;
            end--;
        }
        longValue = l;
        hasStrValue = false;
        hasHashCode = false;
        hasLongValue = true;
        type = T_BYTES;
    }
    public long getLong() {
        if ( hasLongValue ) {
            return longValue;
        }
        switch ( type ) {
        case T_BYTES:
            longValue = byteC.getLong();
            break;
        default:
            longValue = Long.parseLong ( toString() );
        }
        hasLongValue = true;
        return longValue;
    }
    private static final MessageBytesFactory factory = new MessageBytesFactory();
    private static class MessageBytesFactory {
        protected MessageBytesFactory() {
        }
        public MessageBytes newInstance() {
            return new MessageBytes();
        }
    }
}

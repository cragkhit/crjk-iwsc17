package org.apache.coyote.ajp;
import java.nio.ByteBuffer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.res.StringManager;
public class AjpMessage {
    private static final Log log = LogFactory.getLog ( AjpMessage.class );
    protected static final StringManager sm = StringManager.getManager ( AjpMessage.class );
    public AjpMessage ( int packetSize ) {
        buf = new byte[packetSize];
    }
    protected final byte buf[];
    protected int pos;
    protected int len;
    public void reset() {
        len = 4;
        pos = 4;
    }
    public void end() {
        len = pos;
        int dLen = len - 4;
        buf[0] = ( byte ) 0x41;
        buf[1] = ( byte ) 0x42;
        buf[2] = ( byte ) ( ( dLen >>> 8 ) & 0xFF );
        buf[3] = ( byte ) ( dLen & 0xFF );
    }
    public byte[] getBuffer() {
        return buf;
    }
    public int getLen() {
        return len;
    }
    public void appendInt ( int val ) {
        buf[pos++] = ( byte ) ( ( val >>> 8 ) & 0xFF );
        buf[pos++] = ( byte ) ( val & 0xFF );
    }
    public void appendByte ( int val ) {
        buf[pos++] = ( byte ) val;
    }
    public void appendBytes ( MessageBytes mb ) {
        if ( mb == null ) {
            log.error ( sm.getString ( "ajpmessage.null" ),
                        new NullPointerException() );
            appendInt ( 0 );
            appendByte ( 0 );
            return;
        }
        if ( mb.getType() != MessageBytes.T_BYTES ) {
            mb.toBytes();
            ByteChunk bc = mb.getByteChunk();
            byte[] buffer = bc.getBuffer();
            for ( int i = bc.getOffset(); i < bc.getLength(); i++ ) {
                if ( ( buffer[i] > -1 && buffer[i] <= 31 && buffer[i] != 9 ) ||
                        buffer[i] == 127 ) {
                    buffer[i] = ' ';
                }
            }
        }
        appendByteChunk ( mb.getByteChunk() );
    }
    public void appendByteChunk ( ByteChunk bc ) {
        if ( bc == null ) {
            log.error ( sm.getString ( "ajpmessage.null" ),
                        new NullPointerException() );
            appendInt ( 0 );
            appendByte ( 0 );
            return;
        }
        appendBytes ( bc.getBytes(), bc.getStart(), bc.getLength() );
    }
    public void appendBytes ( byte[] b, int off, int numBytes ) {
        if ( checkOverflow ( numBytes ) ) {
            return;
        }
        appendInt ( numBytes );
        System.arraycopy ( b, off, buf, pos, numBytes );
        pos += numBytes;
        appendByte ( 0 );
    }
    public void appendBytes ( ByteBuffer b ) {
        int numBytes = b.remaining();
        if ( checkOverflow ( numBytes ) ) {
            return;
        }
        appendInt ( numBytes );
        b.get ( buf, pos, numBytes );
        pos += numBytes;
        appendByte ( 0 );
    }
    private boolean checkOverflow ( int numBytes ) {
        if ( pos + numBytes + 3 > buf.length ) {
            log.error ( sm.getString ( "ajpmessage.overflow", "" + numBytes, "" + pos ),
                        new ArrayIndexOutOfBoundsException() );
            if ( log.isDebugEnabled() ) {
                dump ( "Overflow/coBytes" );
            }
            return true;
        }
        return false;
    }
    public int getInt() {
        int b1 = buf[pos++] & 0xFF;
        int b2 = buf[pos++] & 0xFF;
        validatePos ( pos );
        return ( b1 << 8 ) + b2;
    }
    public int peekInt() {
        validatePos ( pos + 2 );
        int b1 = buf[pos] & 0xFF;
        int b2 = buf[pos + 1] & 0xFF;
        return ( b1 << 8 ) + b2;
    }
    public byte getByte() {
        byte res = buf[pos++];
        validatePos ( pos );
        return res;
    }
    public void getBytes ( MessageBytes mb ) {
        doGetBytes ( mb, true );
    }
    public void getBodyBytes ( MessageBytes mb ) {
        doGetBytes ( mb, false );
    }
    private void doGetBytes ( MessageBytes mb, boolean terminated ) {
        int length = getInt();
        if ( ( length == 0xFFFF ) || ( length == -1 ) ) {
            mb.recycle();
            return;
        }
        if ( terminated ) {
            validatePos ( pos + length + 1 );
        } else {
            validatePos ( pos + length );
        }
        mb.setBytes ( buf, pos, length );
        mb.getCharChunk().recycle();
        pos += length;
        if ( terminated ) {
            pos++;
        }
    }
    public int getLongInt() {
        int b1 = buf[pos++] & 0xFF;
        b1 <<= 8;
        b1 |= ( buf[pos++] & 0xFF );
        b1 <<= 8;
        b1 |= ( buf[pos++] & 0xFF );
        b1 <<= 8;
        b1 |= ( buf[pos++] & 0xFF );
        validatePos ( pos );
        return  b1;
    }
    public int processHeader ( boolean toContainer ) {
        pos = 0;
        int mark = getInt();
        len = getInt();
        if ( ( toContainer && mark != 0x1234 ) ||
                ( !toContainer && mark != 0x4142 ) ) {
            log.error ( sm.getString ( "ajpmessage.invalid", "" + mark ) );
            if ( log.isDebugEnabled() ) {
                dump ( "In" );
            }
            return -1;
        }
        if ( log.isDebugEnabled() )  {
            log.debug ( "Received " + len + " " + buf[0] );
        }
        return len;
    }
    private void dump ( String prefix ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( prefix + ": " + HexUtils.toHexString ( buf ) + " " + pos + "/" + ( len + 4 ) );
        }
        int max = pos;
        if ( len + 4 > pos ) {
            max = len + 4;
        }
        if ( max > 1000 ) {
            max = 1000;
        }
        if ( log.isDebugEnabled() ) {
            for ( int j = 0; j < max; j += 16 ) {
                log.debug ( hexLine ( buf, j, len ) );
            }
        }
    }
    private void validatePos ( int posToTest ) {
        if ( posToTest > len + 4 ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString (
                        "ajpMessage.invalidPos", Integer.valueOf ( posToTest ) ) );
        }
    }
    protected static String hexLine ( byte buf[], int start, int len ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = start; i < start + 16 ; i++ ) {
            if ( i < len + 4 ) {
                sb.append ( hex ( buf[i] ) + " " );
            } else {
                sb.append ( "   " );
            }
        }
        sb.append ( " | " );
        for ( int i = start; i < start + 16 && i < len + 4; i++ ) {
            if ( !Character.isISOControl ( ( char ) buf[i] ) ) {
                sb.append ( Character.valueOf ( ( char ) buf[i] ) );
            } else {
                sb.append ( "." );
            }
        }
        return sb.toString();
    }
    protected static String hex ( int x ) {
        String h = Integer.toHexString ( x );
        if ( h.length() == 1 ) {
            h = "0" + h;
        }
        return h.substring ( h.length() - 2 );
    }
}
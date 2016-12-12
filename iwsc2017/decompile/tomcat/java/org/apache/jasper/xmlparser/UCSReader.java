package org.apache.jasper.xmlparser;
import java.io.IOException;
import org.apache.juli.logging.LogFactory;
import java.io.InputStream;
import org.apache.juli.logging.Log;
import java.io.Reader;
public class UCSReader extends Reader {
    private final Log log;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final short UCS2LE = 1;
    public static final short UCS2BE = 2;
    public static final short UCS4LE = 4;
    public static final short UCS4BE = 8;
    private final InputStream fInputStream;
    private final byte[] fBuffer;
    private final short fEncoding;
    public UCSReader ( final InputStream inputStream, final short encoding ) {
        this ( inputStream, 8192, encoding );
    }
    public UCSReader ( final InputStream inputStream, final int size, final short encoding ) {
        this.log = LogFactory.getLog ( UCSReader.class );
        this.fInputStream = inputStream;
        this.fBuffer = new byte[size];
        this.fEncoding = encoding;
    }
    @Override
    public int read() throws IOException {
        final int b0 = this.fInputStream.read() & 0xFF;
        if ( b0 == 255 ) {
            return -1;
        }
        final int b = this.fInputStream.read() & 0xFF;
        if ( b == 255 ) {
            return -1;
        }
        if ( this.fEncoding >= 4 ) {
            final int b2 = this.fInputStream.read() & 0xFF;
            if ( b2 == 255 ) {
                return -1;
            }
            final int b3 = this.fInputStream.read() & 0xFF;
            if ( b3 == 255 ) {
                return -1;
            }
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "b0 is " + ( b0 & 0xFF ) + " b1 " + ( b & 0xFF ) + " b2 " + ( b2 & 0xFF ) + " b3 " + ( b3 & 0xFF ) );
            }
            if ( this.fEncoding == 8 ) {
                return ( b0 << 24 ) + ( b << 16 ) + ( b2 << 8 ) + b3;
            }
            return ( b3 << 24 ) + ( b2 << 16 ) + ( b << 8 ) + b0;
        } else {
            if ( this.fEncoding == 2 ) {
                return ( b0 << 8 ) + b;
            }
            return ( b << 8 ) + b0;
        }
    }
    @Override
    public int read ( final char[] ch, final int offset, final int length ) throws IOException {
        int byteLength = length << ( ( this.fEncoding >= 4 ) ? 2 : 1 );
        if ( byteLength > this.fBuffer.length ) {
            byteLength = this.fBuffer.length;
        }
        int count = this.fInputStream.read ( this.fBuffer, 0, byteLength );
        if ( count == -1 ) {
            return -1;
        }
        if ( this.fEncoding >= 4 ) {
            final int numToRead = 4 - ( count & 0x3 ) & 0x3;
            for ( int i = 0; i < numToRead; ++i ) {
                final int charRead = this.fInputStream.read();
                if ( charRead == -1 ) {
                    for ( int j = i; j < numToRead; ++j ) {
                        this.fBuffer[count + j] = 0;
                    }
                    break;
                }
                this.fBuffer[count + i] = ( byte ) charRead;
            }
            count += numToRead;
        } else {
            final int numToRead = count & 0x1;
            if ( numToRead != 0 ) {
                ++count;
                final int charRead2 = this.fInputStream.read();
                if ( charRead2 == -1 ) {
                    this.fBuffer[count] = 0;
                } else {
                    this.fBuffer[count] = ( byte ) charRead2;
                }
            }
        }
        final int numChars = count >> ( ( this.fEncoding >= 4 ) ? 2 : 1 );
        int curPos = 0;
        for ( int k = 0; k < numChars; ++k ) {
            final int b0 = this.fBuffer[curPos++] & 0xFF;
            final int b = this.fBuffer[curPos++] & 0xFF;
            if ( this.fEncoding >= 4 ) {
                final int b2 = this.fBuffer[curPos++] & 0xFF;
                final int b3 = this.fBuffer[curPos++] & 0xFF;
                if ( this.fEncoding == 8 ) {
                    ch[offset + k] = ( char ) ( ( b0 << 24 ) + ( b << 16 ) + ( b2 << 8 ) + b3 );
                } else {
                    ch[offset + k] = ( char ) ( ( b3 << 24 ) + ( b2 << 16 ) + ( b << 8 ) + b0 );
                }
            } else if ( this.fEncoding == 2 ) {
                ch[offset + k] = ( char ) ( ( b0 << 8 ) + b );
            } else {
                ch[offset + k] = ( char ) ( ( b << 8 ) + b0 );
            }
        }
        return numChars;
    }
    @Override
    public long skip ( final long n ) throws IOException {
        final int charWidth = ( this.fEncoding >= 4 ) ? 2 : 1;
        final long bytesSkipped = this.fInputStream.skip ( n << charWidth );
        if ( ( bytesSkipped & ( charWidth | 0x1 ) ) == 0x0L ) {
            return bytesSkipped >> charWidth;
        }
        return ( bytesSkipped >> charWidth ) + 1L;
    }
    @Override
    public boolean ready() throws IOException {
        return false;
    }
    @Override
    public boolean markSupported() {
        return this.fInputStream.markSupported();
    }
    @Override
    public void mark ( final int readAheadLimit ) throws IOException {
        this.fInputStream.mark ( readAheadLimit );
    }
    @Override
    public void reset() throws IOException {
        this.fInputStream.reset();
    }
    @Override
    public void close() throws IOException {
        this.fInputStream.close();
    }
}

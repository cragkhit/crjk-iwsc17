package org.apache.jasper.xmlparser;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class UCSReader extends Reader {
    private final Log log = LogFactory.getLog ( UCSReader.class );
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final short UCS2LE = 1;
    public static final short UCS2BE = 2;
    public static final short UCS4LE = 4;
    public static final short UCS4BE = 8;
    private final InputStream fInputStream;
    private final byte[] fBuffer;
    private final short fEncoding;
    public UCSReader ( InputStream inputStream, short encoding ) {
        this ( inputStream, DEFAULT_BUFFER_SIZE, encoding );
    }
    public UCSReader ( InputStream inputStream, int size, short encoding ) {
        fInputStream = inputStream;
        fBuffer = new byte[size];
        fEncoding = encoding;
    }
    @Override
    public int read() throws IOException {
        int b0 = fInputStream.read() & 0xff;
        if ( b0 == 0xff ) {
            return -1;
        }
        int b1 = fInputStream.read() & 0xff;
        if ( b1 == 0xff ) {
            return -1;
        }
        if ( fEncoding >= 4 ) {
            int b2 = fInputStream.read() & 0xff;
            if ( b2 == 0xff ) {
                return -1;
            }
            int b3 = fInputStream.read() & 0xff;
            if ( b3 == 0xff ) {
                return -1;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "b0 is " + ( b0 & 0xff ) + " b1 " + ( b1 & 0xff ) + " b2 " + ( b2 & 0xff ) + " b3 " + ( b3 & 0xff ) );
            }
            if ( fEncoding == UCS4BE ) {
                return ( b0 << 24 ) + ( b1 << 16 ) + ( b2 << 8 ) + b3;
            } else {
                return ( b3 << 24 ) + ( b2 << 16 ) + ( b1 << 8 ) + b0;
            }
        } else {
            if ( fEncoding == UCS2BE ) {
                return ( b0 << 8 ) + b1;
            } else {
                return ( b1 << 8 ) + b0;
            }
        }
    }
    @Override
    public int read ( char ch[], int offset, int length ) throws IOException {
        int byteLength = length << ( ( fEncoding >= 4 ) ? 2 : 1 );
        if ( byteLength > fBuffer.length ) {
            byteLength = fBuffer.length;
        }
        int count = fInputStream.read ( fBuffer, 0, byteLength );
        if ( count == -1 ) {
            return -1;
        }
        if ( fEncoding >= 4 ) {
            int numToRead = ( 4 - ( count & 3 ) & 3 );
            for ( int i = 0; i < numToRead; i++ ) {
                int charRead = fInputStream.read();
                if ( charRead == -1 ) {
                    for ( int j = i; j < numToRead; j++ ) {
                        fBuffer[count + j] = 0;
                    }
                    break;
                } else {
                    fBuffer[count + i] = ( byte ) charRead;
                }
            }
            count += numToRead;
        } else {
            int numToRead = count & 1;
            if ( numToRead != 0 ) {
                count++;
                int charRead = fInputStream.read();
                if ( charRead == -1 ) {
                    fBuffer[count] = 0;
                } else {
                    fBuffer[count] = ( byte ) charRead;
                }
            }
        }
        int numChars = count >> ( ( fEncoding >= 4 ) ? 2 : 1 );
        int curPos = 0;
        for ( int i = 0; i < numChars; i++ ) {
            int b0 = fBuffer[curPos++] & 0xff;
            int b1 = fBuffer[curPos++] & 0xff;
            if ( fEncoding >= 4 ) {
                int b2 = fBuffer[curPos++] & 0xff;
                int b3 = fBuffer[curPos++] & 0xff;
                if ( fEncoding == UCS4BE ) {
                    ch[offset + i] = ( char ) ( ( b0 << 24 ) + ( b1 << 16 ) + ( b2 << 8 ) + b3 );
                } else {
                    ch[offset + i] = ( char ) ( ( b3 << 24 ) + ( b2 << 16 ) + ( b1 << 8 ) + b0 );
                }
            } else {
                if ( fEncoding == UCS2BE ) {
                    ch[offset + i] = ( char ) ( ( b0 << 8 ) + b1 );
                } else {
                    ch[offset + i] = ( char ) ( ( b1 << 8 ) + b0 );
                }
            }
        }
        return numChars;
    }
    @Override
    public long skip ( long n ) throws IOException {
        int charWidth = ( fEncoding >= 4 ) ? 2 : 1;
        long bytesSkipped = fInputStream.skip ( n << charWidth );
        if ( ( bytesSkipped & ( charWidth | 1 ) ) == 0 ) {
            return bytesSkipped >> charWidth;
        }
        return ( bytesSkipped >> charWidth ) + 1;
    }
    @Override
    public boolean ready() throws IOException {
        return false;
    }
    @Override
    public boolean markSupported() {
        return fInputStream.markSupported();
    }
    @Override
    public void mark ( int readAheadLimit ) throws IOException {
        fInputStream.mark ( readAheadLimit );
    }
    @Override
    public void reset() throws IOException {
        fInputStream.reset();
    }
    @Override
    public void close() throws IOException {
        fInputStream.close();
    }
}

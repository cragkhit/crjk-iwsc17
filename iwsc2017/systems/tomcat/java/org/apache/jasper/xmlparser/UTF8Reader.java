package org.apache.jasper.xmlparser;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UTFDataFormatException;
import org.apache.jasper.compiler.Localizer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class UTF8Reader
    extends Reader {
    private final Log log = LogFactory.getLog ( UTF8Reader.class );
    private static final boolean DEBUG_READ = false;
    private final InputStream fInputStream;
    private final byte[] fBuffer;
    private int fOffset;
    private int fSurrogate = -1;
    public UTF8Reader ( InputStream inputStream, int size ) {
        fInputStream = inputStream;
        fBuffer = new byte[size];
    }
    @Override
    public int read() throws IOException {
        int c = fSurrogate;
        if ( fSurrogate == -1 ) {
            int index = 0;
            int b0 = index == fOffset
                     ? fInputStream.read() : fBuffer[index++] & 0x00FF;
            if ( b0 == -1 ) {
                return -1;
            }
            if ( b0 < 0x80 ) {
                c = ( char ) b0;
            } else if ( ( b0 & 0xE0 ) == 0xC0 ) {
                int b1 = index == fOffset
                         ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if ( b1 == -1 ) {
                    expectedByte ( 2, 2 );
                }
                if ( ( b1 & 0xC0 ) != 0x80 ) {
                    invalidByte ( 2, 2 );
                }
                c = ( ( b0 << 6 ) & 0x07C0 ) | ( b1 & 0x003F );
            } else if ( ( b0 & 0xF0 ) == 0xE0 ) {
                int b1 = index == fOffset
                         ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if ( b1 == -1 ) {
                    expectedByte ( 2, 3 );
                }
                if ( ( b1 & 0xC0 ) != 0x80 ) {
                    invalidByte ( 2, 3 );
                }
                int b2 = index == fOffset
                         ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if ( b2 == -1 ) {
                    expectedByte ( 3, 3 );
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    invalidByte ( 3, 3 );
                }
                c = ( ( b0 << 12 ) & 0xF000 ) | ( ( b1 << 6 ) & 0x0FC0 ) |
                    ( b2 & 0x003F );
            } else if ( ( b0 & 0xF8 ) == 0xF0 ) {
                int b1 = index == fOffset
                         ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if ( b1 == -1 ) {
                    expectedByte ( 2, 4 );
                }
                if ( ( b1 & 0xC0 ) != 0x80 ) {
                    invalidByte ( 2, 3 );
                }
                int b2 = index == fOffset
                         ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if ( b2 == -1 ) {
                    expectedByte ( 3, 4 );
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    invalidByte ( 3, 3 );
                }
                int b3 = index == fOffset
                         ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if ( b3 == -1 ) {
                    expectedByte ( 4, 4 );
                }
                if ( ( b3 & 0xC0 ) != 0x80 ) {
                    invalidByte ( 4, 4 );
                }
                int uuuuu = ( ( b0 << 2 ) & 0x001C ) | ( ( b1 >> 4 ) & 0x0003 );
                if ( uuuuu > 0x10 ) {
                    invalidSurrogate ( uuuuu );
                }
                int wwww = uuuuu - 1;
                int hs = 0xD800 |
                         ( ( wwww << 6 ) & 0x03C0 ) | ( ( b1 << 2 ) & 0x003C ) |
                         ( ( b2 >> 4 ) & 0x0003 );
                int ls = 0xDC00 | ( ( b2 << 6 ) & 0x03C0 ) | ( b3 & 0x003F );
                c = hs;
                fSurrogate = ls;
            } else {
                invalidByte ( 1, 1 );
            }
        } else {
            fSurrogate = -1;
        }
        if ( DEBUG_READ ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "read(): 0x" + Integer.toHexString ( c ) );
            }
        }
        return c;
    }
    @Override
    public int read ( char ch[], int offset, int length ) throws IOException {
        int out = offset;
        if ( fSurrogate != -1 ) {
            ch[offset + 1] = ( char ) fSurrogate;
            fSurrogate = -1;
            length--;
            out++;
        }
        int count = 0;
        if ( fOffset == 0 ) {
            if ( length > fBuffer.length ) {
                length = fBuffer.length;
            }
            count = fInputStream.read ( fBuffer, 0, length );
            if ( count == -1 ) {
                return -1;
            }
            count += out - offset;
        } else {
            count = fOffset;
            fOffset = 0;
        }
        final int total = count;
        for ( int in = 0; in < total; in++ ) {
            int b0 = fBuffer[in] & 0x00FF;
            if ( b0 < 0x80 ) {
                ch[out++] = ( char ) b0;
                continue;
            }
            if ( ( b0 & 0xE0 ) == 0xC0 ) {
                int b1 = -1;
                if ( ++in < total ) {
                    b1 = fBuffer[in] & 0x00FF;
                } else {
                    b1 = fInputStream.read();
                    if ( b1 == -1 ) {
                        if ( out > offset ) {
                            fBuffer[0] = ( byte ) b0;
                            fOffset = 1;
                            return out - offset;
                        }
                        expectedByte ( 2, 2 );
                    }
                    count++;
                }
                if ( ( b1 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        fBuffer[0] = ( byte ) b0;
                        fBuffer[1] = ( byte ) b1;
                        fOffset = 2;
                        return out - offset;
                    }
                    invalidByte ( 2, 2 );
                }
                int c = ( ( b0 << 6 ) & 0x07C0 ) | ( b1 & 0x003F );
                ch[out++] = ( char ) c;
                count -= 1;
                continue;
            }
            if ( ( b0 & 0xF0 ) == 0xE0 ) {
                int b1 = -1;
                if ( ++in < total ) {
                    b1 = fBuffer[in] & 0x00FF;
                } else {
                    b1 = fInputStream.read();
                    if ( b1 == -1 ) {
                        if ( out > offset ) {
                            fBuffer[0] = ( byte ) b0;
                            fOffset = 1;
                            return out - offset;
                        }
                        expectedByte ( 2, 3 );
                    }
                    count++;
                }
                if ( ( b1 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        fBuffer[0] = ( byte ) b0;
                        fBuffer[1] = ( byte ) b1;
                        fOffset = 2;
                        return out - offset;
                    }
                    invalidByte ( 2, 3 );
                }
                int b2 = -1;
                if ( ++in < total ) {
                    b2 = fBuffer[in] & 0x00FF;
                } else {
                    b2 = fInputStream.read();
                    if ( b2 == -1 ) {
                        if ( out > offset ) {
                            fBuffer[0] = ( byte ) b0;
                            fBuffer[1] = ( byte ) b1;
                            fOffset = 2;
                            return out - offset;
                        }
                        expectedByte ( 3, 3 );
                    }
                    count++;
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        fBuffer[0] = ( byte ) b0;
                        fBuffer[1] = ( byte ) b1;
                        fBuffer[2] = ( byte ) b2;
                        fOffset = 3;
                        return out - offset;
                    }
                    invalidByte ( 3, 3 );
                }
                int c = ( ( b0 << 12 ) & 0xF000 ) | ( ( b1 << 6 ) & 0x0FC0 ) |
                        ( b2 & 0x003F );
                ch[out++] = ( char ) c;
                count -= 2;
                continue;
            }
            if ( ( b0 & 0xF8 ) == 0xF0 ) {
                int b1 = -1;
                if ( ++in < total ) {
                    b1 = fBuffer[in] & 0x00FF;
                } else {
                    b1 = fInputStream.read();
                    if ( b1 == -1 ) {
                        if ( out > offset ) {
                            fBuffer[0] = ( byte ) b0;
                            fOffset = 1;
                            return out - offset;
                        }
                        expectedByte ( 2, 4 );
                    }
                    count++;
                }
                if ( ( b1 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        fBuffer[0] = ( byte ) b0;
                        fBuffer[1] = ( byte ) b1;
                        fOffset = 2;
                        return out - offset;
                    }
                    invalidByte ( 2, 4 );
                }
                int b2 = -1;
                if ( ++in < total ) {
                    b2 = fBuffer[in] & 0x00FF;
                } else {
                    b2 = fInputStream.read();
                    if ( b2 == -1 ) {
                        if ( out > offset ) {
                            fBuffer[0] = ( byte ) b0;
                            fBuffer[1] = ( byte ) b1;
                            fOffset = 2;
                            return out - offset;
                        }
                        expectedByte ( 3, 4 );
                    }
                    count++;
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        fBuffer[0] = ( byte ) b0;
                        fBuffer[1] = ( byte ) b1;
                        fBuffer[2] = ( byte ) b2;
                        fOffset = 3;
                        return out - offset;
                    }
                    invalidByte ( 3, 4 );
                }
                int b3 = -1;
                if ( ++in < total ) {
                    b3 = fBuffer[in] & 0x00FF;
                } else {
                    b3 = fInputStream.read();
                    if ( b3 == -1 ) {
                        if ( out > offset ) {
                            fBuffer[0] = ( byte ) b0;
                            fBuffer[1] = ( byte ) b1;
                            fBuffer[2] = ( byte ) b2;
                            fOffset = 3;
                            return out - offset;
                        }
                        expectedByte ( 4, 4 );
                    }
                    count++;
                }
                if ( ( b3 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        fBuffer[0] = ( byte ) b0;
                        fBuffer[1] = ( byte ) b1;
                        fBuffer[2] = ( byte ) b2;
                        fBuffer[3] = ( byte ) b3;
                        fOffset = 4;
                        return out - offset;
                    }
                    invalidByte ( 4, 4 );
                }
                int uuuuu = ( ( b0 << 2 ) & 0x001C ) | ( ( b1 >> 4 ) & 0x0003 );
                if ( uuuuu > 0x10 ) {
                    invalidSurrogate ( uuuuu );
                }
                int wwww = uuuuu - 1;
                int zzzz = b1 & 0x000F;
                int yyyyyy = b2 & 0x003F;
                int xxxxxx = b3 & 0x003F;
                int hs = 0xD800 | ( ( wwww << 6 ) & 0x03C0 ) | ( zzzz << 2 ) | ( yyyyyy >> 4 );
                int ls = 0xDC00 | ( ( yyyyyy << 6 ) & 0x03C0 ) | xxxxxx;
                ch[out++] = ( char ) hs;
                ch[out++] = ( char ) ls;
                count -= 2;
                continue;
            }
            if ( out > offset ) {
                fBuffer[0] = ( byte ) b0;
                fOffset = 1;
                return out - offset;
            }
            invalidByte ( 1, 1 );
        }
        if ( DEBUG_READ ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "read(char[]," + offset + ',' + length + "): count=" + count );
            }
        }
        return count;
    }
    @Override
    public long skip ( long n ) throws IOException {
        long remaining = n;
        final char[] ch = new char[fBuffer.length];
        do {
            int length = ch.length < remaining ? ch.length : ( int ) remaining;
            int count = read ( ch, 0, length );
            if ( count > 0 ) {
                remaining -= count;
            } else {
                break;
            }
        } while ( remaining > 0 );
        long skipped = n - remaining;
        return skipped;
    }
    @Override
    public boolean ready() throws IOException {
        return false;
    }
    @Override
    public boolean markSupported() {
        return false;
    }
    @Override
    public void mark ( int readAheadLimit ) throws IOException {
        throw new IOException (
            Localizer.getMessage ( "jsp.error.xml.operationNotSupported",
                                   "mark()", "UTF-8" ) );
    }
    @Override
    public void reset() throws IOException {
        fOffset = 0;
        fSurrogate = -1;
    }
    @Override
    public void close() throws IOException {
        fInputStream.close();
    }
    private void expectedByte ( int position, int count )
    throws UTFDataFormatException {
        throw new UTFDataFormatException (
            Localizer.getMessage ( "jsp.error.xml.expectedByte",
                                   Integer.toString ( position ),
                                   Integer.toString ( count ) ) );
    }
    private void invalidByte ( int position, int count )
    throws UTFDataFormatException {
        throw new UTFDataFormatException (
            Localizer.getMessage ( "jsp.error.xml.invalidByte",
                                   Integer.toString ( position ),
                                   Integer.toString ( count ) ) );
    }
    private void invalidSurrogate ( int uuuuu ) throws UTFDataFormatException {
        throw new UTFDataFormatException (
            Localizer.getMessage ( "jsp.error.xml.invalidHighSurrogate",
                                   Integer.toHexString ( uuuuu ) ) );
    }
}

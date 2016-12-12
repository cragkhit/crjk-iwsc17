package org.apache.jasper.xmlparser;
import java.io.UTFDataFormatException;
import org.apache.jasper.compiler.Localizer;
import java.io.IOException;
import org.apache.juli.logging.LogFactory;
import java.io.InputStream;
import org.apache.juli.logging.Log;
import java.io.Reader;
public class UTF8Reader extends Reader {
    private final Log log;
    private static final boolean DEBUG_READ = false;
    private final InputStream fInputStream;
    private final byte[] fBuffer;
    private int fOffset;
    private int fSurrogate;
    public UTF8Reader ( final InputStream inputStream, final int size ) {
        this.log = LogFactory.getLog ( UTF8Reader.class );
        this.fSurrogate = -1;
        this.fInputStream = inputStream;
        this.fBuffer = new byte[size];
    }
    @Override
    public int read() throws IOException {
        int c = this.fSurrogate;
        if ( this.fSurrogate == -1 ) {
            int index = 0;
            final int b0 = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
            if ( b0 == -1 ) {
                return -1;
            }
            if ( b0 < 128 ) {
                c = ( char ) b0;
            } else if ( ( b0 & 0xE0 ) == 0xC0 ) {
                final int b = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
                if ( b == -1 ) {
                    this.expectedByte ( 2, 2 );
                }
                if ( ( b & 0xC0 ) != 0x80 ) {
                    this.invalidByte ( 2, 2 );
                }
                c = ( ( b0 << 6 & 0x7C0 ) | ( b & 0x3F ) );
            } else if ( ( b0 & 0xF0 ) == 0xE0 ) {
                final int b = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
                if ( b == -1 ) {
                    this.expectedByte ( 2, 3 );
                }
                if ( ( b & 0xC0 ) != 0x80 ) {
                    this.invalidByte ( 2, 3 );
                }
                final int b2 = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
                if ( b2 == -1 ) {
                    this.expectedByte ( 3, 3 );
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    this.invalidByte ( 3, 3 );
                }
                c = ( ( b0 << 12 & 0xF000 ) | ( b << 6 & 0xFC0 ) | ( b2 & 0x3F ) );
            } else if ( ( b0 & 0xF8 ) == 0xF0 ) {
                final int b = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
                if ( b == -1 ) {
                    this.expectedByte ( 2, 4 );
                }
                if ( ( b & 0xC0 ) != 0x80 ) {
                    this.invalidByte ( 2, 3 );
                }
                final int b2 = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
                if ( b2 == -1 ) {
                    this.expectedByte ( 3, 4 );
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    this.invalidByte ( 3, 3 );
                }
                final int b3 = ( index == this.fOffset ) ? this.fInputStream.read() : ( this.fBuffer[index++] & 0xFF );
                if ( b3 == -1 ) {
                    this.expectedByte ( 4, 4 );
                }
                if ( ( b3 & 0xC0 ) != 0x80 ) {
                    this.invalidByte ( 4, 4 );
                }
                final int uuuuu = ( b0 << 2 & 0x1C ) | ( b >> 4 & 0x3 );
                if ( uuuuu > 16 ) {
                    this.invalidSurrogate ( uuuuu );
                }
                final int wwww = uuuuu - 1;
                final int hs = 0xD800 | ( wwww << 6 & 0x3C0 ) | ( b << 2 & 0x3C ) | ( b2 >> 4 & 0x3 );
                final int ls = 0xDC00 | ( b2 << 6 & 0x3C0 ) | ( b3 & 0x3F );
                c = hs;
                this.fSurrogate = ls;
            } else {
                this.invalidByte ( 1, 1 );
            }
        } else {
            this.fSurrogate = -1;
        }
        return c;
    }
    @Override
    public int read ( final char[] ch, final int offset, int length ) throws IOException {
        int out = offset;
        if ( this.fSurrogate != -1 ) {
            ch[offset + 1] = ( char ) this.fSurrogate;
            this.fSurrogate = -1;
            --length;
            ++out;
        }
        int count = 0;
        if ( this.fOffset == 0 ) {
            if ( length > this.fBuffer.length ) {
                length = this.fBuffer.length;
            }
            count = this.fInputStream.read ( this.fBuffer, 0, length );
            if ( count == -1 ) {
                return -1;
            }
            count += out - offset;
        } else {
            count = this.fOffset;
            this.fOffset = 0;
        }
        for ( int total = count, in = 0; in < total; ++in ) {
            final int b0 = this.fBuffer[in] & 0xFF;
            if ( b0 < 128 ) {
                ch[out++] = ( char ) b0;
            } else if ( ( b0 & 0xE0 ) == 0xC0 ) {
                int b = -1;
                if ( ++in < total ) {
                    b = ( this.fBuffer[in] & 0xFF );
                } else {
                    b = this.fInputStream.read();
                    if ( b == -1 ) {
                        if ( out > offset ) {
                            this.fBuffer[0] = ( byte ) b0;
                            this.fOffset = 1;
                            return out - offset;
                        }
                        this.expectedByte ( 2, 2 );
                    }
                    ++count;
                }
                if ( ( b & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        this.fBuffer[0] = ( byte ) b0;
                        this.fBuffer[1] = ( byte ) b;
                        this.fOffset = 2;
                        return out - offset;
                    }
                    this.invalidByte ( 2, 2 );
                }
                final int c = ( b0 << 6 & 0x7C0 ) | ( b & 0x3F );
                ch[out++] = ( char ) c;
                --count;
            } else if ( ( b0 & 0xF0 ) == 0xE0 ) {
                int b = -1;
                if ( ++in < total ) {
                    b = ( this.fBuffer[in] & 0xFF );
                } else {
                    b = this.fInputStream.read();
                    if ( b == -1 ) {
                        if ( out > offset ) {
                            this.fBuffer[0] = ( byte ) b0;
                            this.fOffset = 1;
                            return out - offset;
                        }
                        this.expectedByte ( 2, 3 );
                    }
                    ++count;
                }
                if ( ( b & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        this.fBuffer[0] = ( byte ) b0;
                        this.fBuffer[1] = ( byte ) b;
                        this.fOffset = 2;
                        return out - offset;
                    }
                    this.invalidByte ( 2, 3 );
                }
                int b2 = -1;
                if ( ++in < total ) {
                    b2 = ( this.fBuffer[in] & 0xFF );
                } else {
                    b2 = this.fInputStream.read();
                    if ( b2 == -1 ) {
                        if ( out > offset ) {
                            this.fBuffer[0] = ( byte ) b0;
                            this.fBuffer[1] = ( byte ) b;
                            this.fOffset = 2;
                            return out - offset;
                        }
                        this.expectedByte ( 3, 3 );
                    }
                    ++count;
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        this.fBuffer[0] = ( byte ) b0;
                        this.fBuffer[1] = ( byte ) b;
                        this.fBuffer[2] = ( byte ) b2;
                        this.fOffset = 3;
                        return out - offset;
                    }
                    this.invalidByte ( 3, 3 );
                }
                final int c2 = ( b0 << 12 & 0xF000 ) | ( b << 6 & 0xFC0 ) | ( b2 & 0x3F );
                ch[out++] = ( char ) c2;
                count -= 2;
            } else if ( ( b0 & 0xF8 ) == 0xF0 ) {
                int b = -1;
                if ( ++in < total ) {
                    b = ( this.fBuffer[in] & 0xFF );
                } else {
                    b = this.fInputStream.read();
                    if ( b == -1 ) {
                        if ( out > offset ) {
                            this.fBuffer[0] = ( byte ) b0;
                            this.fOffset = 1;
                            return out - offset;
                        }
                        this.expectedByte ( 2, 4 );
                    }
                    ++count;
                }
                if ( ( b & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        this.fBuffer[0] = ( byte ) b0;
                        this.fBuffer[1] = ( byte ) b;
                        this.fOffset = 2;
                        return out - offset;
                    }
                    this.invalidByte ( 2, 4 );
                }
                int b2 = -1;
                if ( ++in < total ) {
                    b2 = ( this.fBuffer[in] & 0xFF );
                } else {
                    b2 = this.fInputStream.read();
                    if ( b2 == -1 ) {
                        if ( out > offset ) {
                            this.fBuffer[0] = ( byte ) b0;
                            this.fBuffer[1] = ( byte ) b;
                            this.fOffset = 2;
                            return out - offset;
                        }
                        this.expectedByte ( 3, 4 );
                    }
                    ++count;
                }
                if ( ( b2 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        this.fBuffer[0] = ( byte ) b0;
                        this.fBuffer[1] = ( byte ) b;
                        this.fBuffer[2] = ( byte ) b2;
                        this.fOffset = 3;
                        return out - offset;
                    }
                    this.invalidByte ( 3, 4 );
                }
                int b3 = -1;
                if ( ++in < total ) {
                    b3 = ( this.fBuffer[in] & 0xFF );
                } else {
                    b3 = this.fInputStream.read();
                    if ( b3 == -1 ) {
                        if ( out > offset ) {
                            this.fBuffer[0] = ( byte ) b0;
                            this.fBuffer[1] = ( byte ) b;
                            this.fBuffer[2] = ( byte ) b2;
                            this.fOffset = 3;
                            return out - offset;
                        }
                        this.expectedByte ( 4, 4 );
                    }
                    ++count;
                }
                if ( ( b3 & 0xC0 ) != 0x80 ) {
                    if ( out > offset ) {
                        this.fBuffer[0] = ( byte ) b0;
                        this.fBuffer[1] = ( byte ) b;
                        this.fBuffer[2] = ( byte ) b2;
                        this.fBuffer[3] = ( byte ) b3;
                        this.fOffset = 4;
                        return out - offset;
                    }
                    this.invalidByte ( 4, 4 );
                }
                final int uuuuu = ( b0 << 2 & 0x1C ) | ( b >> 4 & 0x3 );
                if ( uuuuu > 16 ) {
                    this.invalidSurrogate ( uuuuu );
                }
                final int wwww = uuuuu - 1;
                final int zzzz = b & 0xF;
                final int yyyyyy = b2 & 0x3F;
                final int xxxxxx = b3 & 0x3F;
                final int hs = 0xD800 | ( wwww << 6 & 0x3C0 ) | zzzz << 2 | yyyyyy >> 4;
                final int ls = 0xDC00 | ( yyyyyy << 6 & 0x3C0 ) | xxxxxx;
                ch[out++] = ( char ) hs;
                ch[out++] = ( char ) ls;
                count -= 2;
            } else {
                if ( out > offset ) {
                    this.fBuffer[0] = ( byte ) b0;
                    this.fOffset = 1;
                    return out - offset;
                }
                this.invalidByte ( 1, 1 );
            }
        }
        return count;
    }
    @Override
    public long skip ( final long n ) throws IOException {
        long remaining = n;
        final char[] ch = new char[this.fBuffer.length];
        do {
            final int length = ( ch.length < remaining ) ? ch.length : ( ( int ) remaining );
            final int count = this.read ( ch, 0, length );
            if ( count <= 0 ) {
                break;
            }
            remaining -= count;
        } while ( remaining > 0L );
        final long skipped = n - remaining;
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
    public void mark ( final int readAheadLimit ) throws IOException {
        throw new IOException ( Localizer.getMessage ( "jsp.error.xml.operationNotSupported", "mark()", "UTF-8" ) );
    }
    @Override
    public void reset() throws IOException {
        this.fOffset = 0;
        this.fSurrogate = -1;
    }
    @Override
    public void close() throws IOException {
        this.fInputStream.close();
    }
    private void expectedByte ( final int position, final int count ) throws UTFDataFormatException {
        throw new UTFDataFormatException ( Localizer.getMessage ( "jsp.error.xml.expectedByte", Integer.toString ( position ), Integer.toString ( count ) ) );
    }
    private void invalidByte ( final int position, final int count ) throws UTFDataFormatException {
        throw new UTFDataFormatException ( Localizer.getMessage ( "jsp.error.xml.invalidByte", Integer.toString ( position ), Integer.toString ( count ) ) );
    }
    private void invalidSurrogate ( final int uuuuu ) throws UTFDataFormatException {
        throw new UTFDataFormatException ( Localizer.getMessage ( "jsp.error.xml.invalidHighSurrogate", Integer.toHexString ( uuuuu ) ) );
    }
}

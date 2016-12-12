package org.apache.jasper.xmlparser;
import java.io.IOException;
import java.io.InputStream;
private static final class RewindableInputStream extends InputStream {
    private InputStream fInputStream;
    private byte[] fData;
    private int fEndOffset;
    private int fOffset;
    private int fLength;
    private int fMark;
    public RewindableInputStream ( final InputStream is ) {
        this.fData = new byte[64];
        this.fInputStream = is;
        this.fEndOffset = -1;
        this.fOffset = 0;
        this.fLength = 0;
        this.fMark = 0;
    }
    @Override
    public int read() throws IOException {
        int b = 0;
        if ( this.fOffset < this.fLength ) {
            return this.fData[this.fOffset++] & 0xFF;
        }
        if ( this.fOffset == this.fEndOffset ) {
            return -1;
        }
        if ( this.fOffset == this.fData.length ) {
            final byte[] newData = new byte[this.fOffset << 1];
            System.arraycopy ( this.fData, 0, newData, 0, this.fOffset );
            this.fData = newData;
        }
        b = this.fInputStream.read();
        if ( b == -1 ) {
            this.fEndOffset = this.fOffset;
            return -1;
        }
        this.fData[this.fLength++] = ( byte ) b;
        ++this.fOffset;
        return b & 0xFF;
    }
    @Override
    public int read ( final byte[] b, final int off, int len ) throws IOException {
        final int bytesLeft = this.fLength - this.fOffset;
        if ( bytesLeft != 0 ) {
            if ( len < bytesLeft ) {
                if ( len <= 0 ) {
                    return 0;
                }
            } else {
                len = bytesLeft;
            }
            if ( b != null ) {
                System.arraycopy ( this.fData, this.fOffset, b, off, len );
            }
            this.fOffset += len;
            return len;
        }
        if ( this.fOffset == this.fEndOffset ) {
            return -1;
        }
        final int returnedVal = this.read();
        if ( returnedVal == -1 ) {
            this.fEndOffset = this.fOffset;
            return -1;
        }
        b[off] = ( byte ) returnedVal;
        return 1;
    }
    @Override
    public long skip ( long n ) throws IOException {
        if ( n <= 0L ) {
            return 0L;
        }
        final int bytesLeft = this.fLength - this.fOffset;
        if ( bytesLeft == 0 ) {
            if ( this.fOffset == this.fEndOffset ) {
                return 0L;
            }
            return this.fInputStream.skip ( n );
        } else {
            if ( n <= bytesLeft ) {
                this.fOffset += ( int ) n;
                return n;
            }
            this.fOffset += bytesLeft;
            if ( this.fOffset == this.fEndOffset ) {
                return bytesLeft;
            }
            n -= bytesLeft;
            return this.fInputStream.skip ( n ) + bytesLeft;
        }
    }
    @Override
    public int available() throws IOException {
        final int bytesLeft = this.fLength - this.fOffset;
        if ( bytesLeft != 0 ) {
            return bytesLeft;
        }
        if ( this.fOffset == this.fEndOffset ) {
            return -1;
        }
        return 0;
    }
    @Override
    public synchronized void mark ( final int howMuch ) {
        this.fMark = this.fOffset;
    }
    @Override
    public synchronized void reset() {
        this.fOffset = this.fMark;
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public void close() throws IOException {
        if ( this.fInputStream != null ) {
            this.fInputStream.close();
            this.fInputStream = null;
        }
    }
}

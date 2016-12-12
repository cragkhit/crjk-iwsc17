package org.apache.jasper.xmlparser;
import java.io.IOException;
import org.apache.jasper.compiler.Localizer;
import java.io.InputStream;
import java.io.Reader;
public class ASCIIReader extends Reader {
    private final InputStream fInputStream;
    private final byte[] fBuffer;
    public ASCIIReader ( final InputStream inputStream, final int size ) {
        this.fInputStream = inputStream;
        this.fBuffer = new byte[size];
    }
    @Override
    public int read() throws IOException {
        final int b0 = this.fInputStream.read();
        if ( b0 > 128 ) {
            throw new IOException ( Localizer.getMessage ( "jsp.error.xml.invalidASCII", Integer.toString ( b0 ) ) );
        }
        return b0;
    }
    @Override
    public int read ( final char[] ch, final int offset, int length ) throws IOException {
        if ( length > this.fBuffer.length ) {
            length = this.fBuffer.length;
        }
        final int count = this.fInputStream.read ( this.fBuffer, 0, length );
        for ( int i = 0; i < count; ++i ) {
            final int b0 = 0xFF & this.fBuffer[i];
            if ( b0 > 128 ) {
                throw new IOException ( Localizer.getMessage ( "jsp.error.xml.invalidASCII", Integer.toString ( b0 ) ) );
            }
            ch[offset + i] = ( char ) b0;
        }
        return count;
    }
    @Override
    public long skip ( final long n ) throws IOException {
        return this.fInputStream.skip ( n );
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

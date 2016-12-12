package org.apache.jasper.xmlparser;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.jasper.compiler.Localizer;
public class ASCIIReader extends Reader {
    private final InputStream fInputStream;
    private final byte[] fBuffer;
    public ASCIIReader ( InputStream inputStream, int size ) {
        fInputStream = inputStream;
        fBuffer = new byte[size];
    }
    @Override
    public int read() throws IOException {
        int b0 = fInputStream.read();
        if ( b0 > 0x80 ) {
            throw new IOException ( Localizer.getMessage ( "jsp.error.xml.invalidASCII",
                                    Integer.toString ( b0 ) ) );
        }
        return b0;
    }
    @Override
    public int read ( char ch[], int offset, int length ) throws IOException {
        if ( length > fBuffer.length ) {
            length = fBuffer.length;
        }
        int count = fInputStream.read ( fBuffer, 0, length );
        for ( int i = 0; i < count; i++ ) {
            int b0 = ( 0xff & fBuffer[i] );
            if ( b0 > 0x80 ) {
                throw new IOException ( Localizer.getMessage ( "jsp.error.xml.invalidASCII",
                                        Integer.toString ( b0 ) ) );
            }
            ch[offset + i] = ( char ) b0;
        }
        return count;
    }
    @Override
    public long skip ( long n ) throws IOException {
        return fInputStream.skip ( n );
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

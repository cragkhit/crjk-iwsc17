package org.apache.catalina.connector;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
public class CoyoteReader extends BufferedReader {
    private static final char[] LINE_SEP;
    private static final int MAX_LINE_LENGTH = 4096;
    protected InputBuffer ib;
    protected char[] lineBuffer;
    public CoyoteReader ( final InputBuffer ib ) {
        super ( ib, 1 );
        this.lineBuffer = null;
        this.ib = ib;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    void clear() {
        this.ib = null;
    }
    @Override
    public void close() throws IOException {
        this.ib.close();
    }
    @Override
    public int read() throws IOException {
        return this.ib.read();
    }
    @Override
    public int read ( final char[] cbuf ) throws IOException {
        return this.ib.read ( cbuf, 0, cbuf.length );
    }
    @Override
    public int read ( final char[] cbuf, final int off, final int len ) throws IOException {
        return this.ib.read ( cbuf, off, len );
    }
    @Override
    public long skip ( final long n ) throws IOException {
        return this.ib.skip ( n );
    }
    @Override
    public boolean ready() throws IOException {
        return this.ib.ready();
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public void mark ( final int readAheadLimit ) throws IOException {
        this.ib.mark ( readAheadLimit );
    }
    @Override
    public void reset() throws IOException {
        this.ib.reset();
    }
    @Override
    public String readLine() throws IOException {
        if ( this.lineBuffer == null ) {
            this.lineBuffer = new char[4096];
        }
        String result = null;
        int pos = 0;
        int end = -1;
        int skip = -1;
        StringBuilder aggregator = null;
        while ( end < 0 ) {
            this.mark ( 4096 );
            while ( pos < 4096 && end < 0 ) {
                final int nRead = this.read ( this.lineBuffer, pos, 4096 - pos );
                if ( nRead < 0 ) {
                    if ( pos == 0 && aggregator == null ) {
                        return null;
                    }
                    end = pos;
                    skip = pos;
                }
                for ( int i = pos; i < pos + nRead && end < 0; ++i ) {
                    if ( this.lineBuffer[i] == CoyoteReader.LINE_SEP[0] ) {
                        end = i;
                        skip = i + 1;
                        char nextchar;
                        if ( i == pos + nRead - 1 ) {
                            nextchar = ( char ) this.read();
                        } else {
                            nextchar = this.lineBuffer[i + 1];
                        }
                        if ( nextchar == CoyoteReader.LINE_SEP[1] ) {
                            ++skip;
                        }
                    } else if ( this.lineBuffer[i] == CoyoteReader.LINE_SEP[1] ) {
                        end = i;
                        skip = i + 1;
                    }
                }
                if ( nRead > 0 ) {
                    pos += nRead;
                }
            }
            if ( end < 0 ) {
                if ( aggregator == null ) {
                    aggregator = new StringBuilder();
                }
                aggregator.append ( this.lineBuffer );
                pos = 0;
            } else {
                this.reset();
                this.skip ( skip );
            }
        }
        if ( aggregator == null ) {
            result = new String ( this.lineBuffer, 0, end );
        } else {
            aggregator.append ( this.lineBuffer, 0, end );
            result = aggregator.toString();
        }
        return result;
    }
    static {
        LINE_SEP = new char[] { '\r', '\n' };
    }
}

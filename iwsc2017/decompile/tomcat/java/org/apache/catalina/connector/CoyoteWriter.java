package org.apache.catalina.connector;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
public class CoyoteWriter extends PrintWriter {
    private static final char[] LINE_SEP;
    protected OutputBuffer ob;
    protected boolean error;
    public CoyoteWriter ( final OutputBuffer ob ) {
        super ( ob );
        this.error = false;
        this.ob = ob;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    void clear() {
        this.ob = null;
    }
    void recycle() {
        this.error = false;
    }
    @Override
    public void flush() {
        if ( this.error ) {
            return;
        }
        try {
            this.ob.flush();
        } catch ( IOException e ) {
            this.error = true;
        }
    }
    @Override
    public void close() {
        try {
            this.ob.close();
        } catch ( IOException ex ) {}
        this.error = false;
    }
    @Override
    public boolean checkError() {
        this.flush();
        return this.error;
    }
    @Override
    public void write ( final int c ) {
        if ( this.error ) {
            return;
        }
        try {
            this.ob.write ( c );
        } catch ( IOException e ) {
            this.error = true;
        }
    }
    @Override
    public void write ( final char[] buf, final int off, final int len ) {
        if ( this.error ) {
            return;
        }
        try {
            this.ob.write ( buf, off, len );
        } catch ( IOException e ) {
            this.error = true;
        }
    }
    @Override
    public void write ( final char[] buf ) {
        this.write ( buf, 0, buf.length );
    }
    @Override
    public void write ( final String s, final int off, final int len ) {
        if ( this.error ) {
            return;
        }
        try {
            this.ob.write ( s, off, len );
        } catch ( IOException e ) {
            this.error = true;
        }
    }
    @Override
    public void write ( final String s ) {
        this.write ( s, 0, s.length() );
    }
    @Override
    public void print ( final boolean b ) {
        if ( b ) {
            this.write ( "true" );
        } else {
            this.write ( "false" );
        }
    }
    @Override
    public void print ( final char c ) {
        this.write ( c );
    }
    @Override
    public void print ( final int i ) {
        this.write ( String.valueOf ( i ) );
    }
    @Override
    public void print ( final long l ) {
        this.write ( String.valueOf ( l ) );
    }
    @Override
    public void print ( final float f ) {
        this.write ( String.valueOf ( f ) );
    }
    @Override
    public void print ( final double d ) {
        this.write ( String.valueOf ( d ) );
    }
    @Override
    public void print ( final char[] s ) {
        this.write ( s );
    }
    @Override
    public void print ( String s ) {
        if ( s == null ) {
            s = "null";
        }
        this.write ( s );
    }
    @Override
    public void print ( final Object obj ) {
        this.write ( String.valueOf ( obj ) );
    }
    @Override
    public void println() {
        this.write ( CoyoteWriter.LINE_SEP );
    }
    @Override
    public void println ( final boolean b ) {
        this.print ( b );
        this.println();
    }
    @Override
    public void println ( final char c ) {
        this.print ( c );
        this.println();
    }
    @Override
    public void println ( final int i ) {
        this.print ( i );
        this.println();
    }
    @Override
    public void println ( final long l ) {
        this.print ( l );
        this.println();
    }
    @Override
    public void println ( final float f ) {
        this.print ( f );
        this.println();
    }
    @Override
    public void println ( final double d ) {
        this.print ( d );
        this.println();
    }
    @Override
    public void println ( final char[] c ) {
        this.print ( c );
        this.println();
    }
    @Override
    public void println ( final String s ) {
        this.print ( s );
        this.println();
    }
    @Override
    public void println ( final Object o ) {
        this.print ( o );
        this.println();
    }
    static {
        LINE_SEP = System.lineSeparator().toCharArray();
    }
}

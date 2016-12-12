package org.apache.jasper.compiler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
protected static class SystemLogHandler extends PrintStream {
    protected final PrintStream wrapped;
    protected static final ThreadLocal<PrintStream> streams;
    protected static final ThreadLocal<ByteArrayOutputStream> data;
    public SystemLogHandler ( final PrintStream wrapped ) {
        super ( wrapped );
        this.wrapped = wrapped;
    }
    public static void setThread() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SystemLogHandler.data.set ( baos );
        SystemLogHandler.streams.set ( new PrintStream ( baos ) );
    }
    public static String unsetThread() {
        final ByteArrayOutputStream baos = SystemLogHandler.data.get();
        if ( baos == null ) {
            return null;
        }
        SystemLogHandler.streams.set ( null );
        SystemLogHandler.data.set ( null );
        return baos.toString();
    }
    protected PrintStream findStream() {
        PrintStream ps = SystemLogHandler.streams.get();
        if ( ps == null ) {
            ps = this.wrapped;
        }
        return ps;
    }
    @Override
    public void flush() {
        this.findStream().flush();
    }
    @Override
    public void close() {
        this.findStream().close();
    }
    @Override
    public boolean checkError() {
        return this.findStream().checkError();
    }
    @Override
    protected void setError() {
    }
    @Override
    public void write ( final int b ) {
        this.findStream().write ( b );
    }
    @Override
    public void write ( final byte[] b ) throws IOException {
        this.findStream().write ( b );
    }
    @Override
    public void write ( final byte[] buf, final int off, final int len ) {
        this.findStream().write ( buf, off, len );
    }
    @Override
    public void print ( final boolean b ) {
        this.findStream().print ( b );
    }
    @Override
    public void print ( final char c ) {
        this.findStream().print ( c );
    }
    @Override
    public void print ( final int i ) {
        this.findStream().print ( i );
    }
    @Override
    public void print ( final long l ) {
        this.findStream().print ( l );
    }
    @Override
    public void print ( final float f ) {
        this.findStream().print ( f );
    }
    @Override
    public void print ( final double d ) {
        this.findStream().print ( d );
    }
    @Override
    public void print ( final char[] s ) {
        this.findStream().print ( s );
    }
    @Override
    public void print ( final String s ) {
        this.findStream().print ( s );
    }
    @Override
    public void print ( final Object obj ) {
        this.findStream().print ( obj );
    }
    @Override
    public void println() {
        this.findStream().println();
    }
    @Override
    public void println ( final boolean x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final char x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final int x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final long x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final float x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final double x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final char[] x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final String x ) {
        this.findStream().println ( x );
    }
    @Override
    public void println ( final Object x ) {
        this.findStream().println ( x );
    }
    static {
        streams = new ThreadLocal<PrintStream>();
        data = new ThreadLocal<ByteArrayOutputStream>();
    }
}

package org.apache.tomcat.util.log;
import java.io.IOException;
import java.io.PrintStream;
import java.util.EmptyStackException;
import java.util.Stack;
public class SystemLogHandler extends PrintStream {
    public SystemLogHandler ( PrintStream wrapped ) {
        super ( wrapped );
        out = wrapped;
    }
    private final PrintStream out;
    private static final ThreadLocal<Stack<CaptureLog>> logs = new ThreadLocal<>();
    private static final Stack<CaptureLog> reuse = new Stack<>();
    public static void startCapture() {
        CaptureLog log = null;
        if ( !reuse.isEmpty() ) {
            try {
                log = reuse.pop();
            } catch ( EmptyStackException e ) {
                log = new CaptureLog();
            }
        } else {
            log = new CaptureLog();
        }
        Stack<CaptureLog> stack = logs.get();
        if ( stack == null ) {
            stack = new Stack<>();
            logs.set ( stack );
        }
        stack.push ( log );
    }
    public static String stopCapture() {
        Stack<CaptureLog> stack = logs.get();
        if ( stack == null || stack.isEmpty() ) {
            return null;
        }
        CaptureLog log = stack.pop();
        if ( log == null ) {
            return null;
        }
        String capture = log.getCapture();
        log.reset();
        reuse.push ( log );
        return capture;
    }
    protected PrintStream findStream() {
        Stack<CaptureLog> stack = logs.get();
        if ( stack != null && !stack.isEmpty() ) {
            CaptureLog log = stack.peek();
            if ( log != null ) {
                PrintStream ps = log.getStream();
                if ( ps != null ) {
                    return ps;
                }
            }
        }
        return out;
    }
    @Override
    public void flush() {
        findStream().flush();
    }
    @Override
    public void close() {
        findStream().close();
    }
    @Override
    public boolean checkError() {
        return findStream().checkError();
    }
    @Override
    protected void setError() {
    }
    @Override
    public void write ( int b ) {
        findStream().write ( b );
    }
    @Override
    public void write ( byte[] b )
    throws IOException {
        findStream().write ( b );
    }
    @Override
    public void write ( byte[] buf, int off, int len ) {
        findStream().write ( buf, off, len );
    }
    @Override
    public void print ( boolean b ) {
        findStream().print ( b );
    }
    @Override
    public void print ( char c ) {
        findStream().print ( c );
    }
    @Override
    public void print ( int i ) {
        findStream().print ( i );
    }
    @Override
    public void print ( long l ) {
        findStream().print ( l );
    }
    @Override
    public void print ( float f ) {
        findStream().print ( f );
    }
    @Override
    public void print ( double d ) {
        findStream().print ( d );
    }
    @Override
    public void print ( char[] s ) {
        findStream().print ( s );
    }
    @Override
    public void print ( String s ) {
        findStream().print ( s );
    }
    @Override
    public void print ( Object obj ) {
        findStream().print ( obj );
    }
    @Override
    public void println() {
        findStream().println();
    }
    @Override
    public void println ( boolean x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( char x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( int x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( long x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( float x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( double x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( char[] x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( String x ) {
        findStream().println ( x );
    }
    @Override
    public void println ( Object x ) {
        findStream().println ( x );
    }
}

package javax.servlet.jsp;
import java.io.IOException;
public abstract class JspWriter extends java.io.Writer {
    public static final int NO_BUFFER = 0;
    public static final int DEFAULT_BUFFER = -1;
    public static final int UNBOUNDED_BUFFER = -2;
    protected JspWriter ( int bufferSize, boolean autoFlush ) {
        this.bufferSize = bufferSize;
        this.autoFlush = autoFlush;
    }
    public abstract void newLine() throws IOException;
    public abstract void print ( boolean b ) throws IOException;
    public abstract void print ( char c ) throws IOException;
    public abstract void print ( int i ) throws IOException;
    public abstract void print ( long l ) throws IOException;
    public abstract void print ( float f ) throws IOException;
    public abstract void print ( double d ) throws IOException;
    public abstract void print ( char s[] ) throws IOException;
    public abstract void print ( String s ) throws IOException;
    public abstract void print ( Object obj ) throws IOException;
    public abstract void println() throws IOException;
    public abstract void println ( boolean x ) throws IOException;
    public abstract void println ( char x ) throws IOException;
    public abstract void println ( int x ) throws IOException;
    public abstract void println ( long x ) throws IOException;
    public abstract void println ( float x ) throws IOException;
    public abstract void println ( double x ) throws IOException;
    public abstract void println ( char x[] ) throws IOException;
    public abstract void println ( String x ) throws IOException;
    public abstract void println ( Object x ) throws IOException;
    public abstract void clear() throws IOException;
    public abstract void clearBuffer() throws IOException;
    @Override
    public abstract void flush() throws IOException;
    @Override
    public abstract void close() throws IOException;
    public int getBufferSize() {
        return bufferSize;
    }
    public abstract int getRemaining();
    public boolean isAutoFlush() {
        return autoFlush;
    }
    protected int bufferSize;
    protected boolean autoFlush;
}

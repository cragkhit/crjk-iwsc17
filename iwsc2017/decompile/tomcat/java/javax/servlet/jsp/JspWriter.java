package javax.servlet.jsp;
import java.io.IOException;
import java.io.Writer;
public abstract class JspWriter extends Writer {
    public static final int NO_BUFFER = 0;
    public static final int DEFAULT_BUFFER = -1;
    public static final int UNBOUNDED_BUFFER = -2;
    protected int bufferSize;
    protected boolean autoFlush;
    protected JspWriter ( final int bufferSize, final boolean autoFlush ) {
        this.bufferSize = bufferSize;
        this.autoFlush = autoFlush;
    }
    public abstract void newLine() throws IOException;
    public abstract void print ( final boolean p0 ) throws IOException;
    public abstract void print ( final char p0 ) throws IOException;
    public abstract void print ( final int p0 ) throws IOException;
    public abstract void print ( final long p0 ) throws IOException;
    public abstract void print ( final float p0 ) throws IOException;
    public abstract void print ( final double p0 ) throws IOException;
    public abstract void print ( final char[] p0 ) throws IOException;
    public abstract void print ( final String p0 ) throws IOException;
    public abstract void print ( final Object p0 ) throws IOException;
    public abstract void println() throws IOException;
    public abstract void println ( final boolean p0 ) throws IOException;
    public abstract void println ( final char p0 ) throws IOException;
    public abstract void println ( final int p0 ) throws IOException;
    public abstract void println ( final long p0 ) throws IOException;
    public abstract void println ( final float p0 ) throws IOException;
    public abstract void println ( final double p0 ) throws IOException;
    public abstract void println ( final char[] p0 ) throws IOException;
    public abstract void println ( final String p0 ) throws IOException;
    public abstract void println ( final Object p0 ) throws IOException;
    public abstract void clear() throws IOException;
    public abstract void clearBuffer() throws IOException;
    @Override
    public abstract void flush() throws IOException;
    @Override
    public abstract void close() throws IOException;
    public int getBufferSize() {
        return this.bufferSize;
    }
    public abstract int getRemaining();
    public boolean isAutoFlush() {
        return this.autoFlush;
    }
}

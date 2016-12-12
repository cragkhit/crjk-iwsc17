package org.apache.jasper.compiler;
import java.io.PrintWriter;
public class ServletWriter implements AutoCloseable {
    private static final int TAB_WIDTH = 2;
    private static final String SPACES = "                              ";
    private int indent;
    private int virtual_indent;
    private final PrintWriter writer;
    private int javaLine;
    public ServletWriter ( final PrintWriter writer ) {
        this.indent = 0;
        this.virtual_indent = 0;
        this.javaLine = 1;
        this.writer = writer;
    }
    @Override
    public void close() {
        this.writer.close();
    }
    public int getJavaLine() {
        return this.javaLine;
    }
    public void pushIndent() {
        this.virtual_indent += 2;
        if ( this.virtual_indent >= 0 && this.virtual_indent <= "                              ".length() ) {
            this.indent = this.virtual_indent;
        }
    }
    public void popIndent() {
        this.virtual_indent -= 2;
        if ( this.virtual_indent >= 0 && this.virtual_indent <= "                              ".length() ) {
            this.indent = this.virtual_indent;
        }
    }
    public void println ( final String s ) {
        ++this.javaLine;
        this.writer.println ( s );
    }
    public void println() {
        ++this.javaLine;
        this.writer.println ( "" );
    }
    public void printin() {
        this.writer.print ( "                              ".substring ( 0, this.indent ) );
    }
    public void printin ( final String s ) {
        this.writer.print ( "                              ".substring ( 0, this.indent ) );
        this.writer.print ( s );
    }
    public void printil ( final String s ) {
        ++this.javaLine;
        this.writer.print ( "                              ".substring ( 0, this.indent ) );
        this.writer.println ( s );
    }
    public void print ( final char c ) {
        this.writer.print ( c );
    }
    public void print ( final int i ) {
        this.writer.print ( i );
    }
    public void print ( final String s ) {
        this.writer.print ( s );
    }
    public void printMultiLn ( final String s ) {
        for ( int index = 0; ( index = s.indexOf ( 10, index ) ) > -1; ++index ) {
            ++this.javaLine;
        }
        this.writer.print ( s );
    }
}

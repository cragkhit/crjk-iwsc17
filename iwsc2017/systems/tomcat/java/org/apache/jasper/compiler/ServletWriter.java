package org.apache.jasper.compiler;
import java.io.PrintWriter;
public class ServletWriter implements AutoCloseable {
    private static final int TAB_WIDTH = 2;
    private static final String SPACES = "                              ";
    private int indent = 0;
    private int virtual_indent = 0;
    private final PrintWriter writer;
    private int javaLine = 1;
    public ServletWriter ( PrintWriter writer ) {
        this.writer = writer;
    }
    @Override
    public void close() {
        writer.close();
    }
    public int getJavaLine() {
        return javaLine;
    }
    public void pushIndent() {
        virtual_indent += TAB_WIDTH;
        if ( virtual_indent >= 0 && virtual_indent <= SPACES.length() ) {
            indent = virtual_indent;
        }
    }
    public void popIndent() {
        virtual_indent -= TAB_WIDTH;
        if ( virtual_indent >= 0 && virtual_indent <= SPACES.length() ) {
            indent = virtual_indent;
        }
    }
    public void println ( String s ) {
        javaLine++;
        writer.println ( s );
    }
    public void println() {
        javaLine++;
        writer.println ( "" );
    }
    public void printin() {
        writer.print ( SPACES.substring ( 0, indent ) );
    }
    public void printin ( String s ) {
        writer.print ( SPACES.substring ( 0, indent ) );
        writer.print ( s );
    }
    public void printil ( String s ) {
        javaLine++;
        writer.print ( SPACES.substring ( 0, indent ) );
        writer.println ( s );
    }
    public void print ( char c ) {
        writer.print ( c );
    }
    public void print ( int i ) {
        writer.print ( i );
    }
    public void print ( String s ) {
        writer.print ( s );
    }
    public void printMultiLn ( String s ) {
        int index = 0;
        while ( ( index = s.indexOf ( '\n', index ) ) > -1 ) {
            javaLine++;
            index++;
        }
        writer.print ( s );
    }
}

package org.apache.jasper.compiler;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.jasper.JspCompilationContext;
final class Mark {
    int cursor;
    int line;
    int col;
    char[] stream;
    private String fileName;
    private JspCompilationContext ctxt;
    Mark ( final JspReader reader, final char[] inStream, final String name ) {
        this.stream = null;
        this.ctxt = reader.getJspCompilationContext();
        this.stream = inStream;
        this.cursor = 0;
        this.line = 1;
        this.col = 1;
        this.fileName = name;
    }
    Mark ( final Mark other ) {
        this.stream = null;
        this.init ( other, false );
    }
    void update ( final int cursor, final int line, final int col ) {
        this.cursor = cursor;
        this.line = line;
        this.col = col;
    }
    void init ( final Mark other, final boolean singleFile ) {
        this.cursor = other.cursor;
        this.line = other.line;
        this.col = other.col;
        if ( !singleFile ) {
            this.ctxt = other.ctxt;
            this.stream = other.stream;
            this.fileName = other.fileName;
        }
    }
    Mark ( final JspCompilationContext ctxt, final String filename, final int line, final int col ) {
        this.stream = null;
        this.ctxt = ctxt;
        this.stream = null;
        this.cursor = 0;
        this.line = line;
        this.col = col;
        this.fileName = filename;
    }
    public int getLineNumber() {
        return this.line;
    }
    public int getColumnNumber() {
        return this.col;
    }
    @Override
    public String toString() {
        return this.getFile() + "(" + this.line + "," + this.col + ")";
    }
    public String getFile() {
        return this.fileName;
    }
    public URL getURL() throws MalformedURLException {
        return this.ctxt.getResource ( this.getFile() );
    }
    @Override
    public boolean equals ( final Object other ) {
        if ( other instanceof Mark ) {
            final Mark m = ( Mark ) other;
            return this.cursor == m.cursor && this.line == m.line && this.col == m.col;
        }
        return false;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + this.col;
        result = 31 * result + this.cursor;
        result = 31 * result + this.line;
        return result;
    }
}

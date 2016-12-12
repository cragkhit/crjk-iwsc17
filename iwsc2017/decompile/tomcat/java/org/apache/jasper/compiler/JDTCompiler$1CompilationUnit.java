package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
class CompilationUnit implements ICompilationUnit {
    private final String className;
    private final String sourceFile;
    CompilationUnit ( final String sourceFile, final String className ) {
        this.className = className;
        this.sourceFile = sourceFile;
    }
    public char[] getFileName() {
        return this.sourceFile.toCharArray();
    }
    public char[] getContents() {
        char[] result = null;
        try ( final FileInputStream is = new FileInputStream ( this.sourceFile );
                    final InputStreamReader isr = new InputStreamReader ( is, JDTCompiler.this.ctxt.getOptions().getJavaEncoding() );
                    final Reader reader = new BufferedReader ( isr ) ) {
            final char[] chars = new char[8192];
            final StringBuilder buf = new StringBuilder();
            int count;
            while ( ( count = reader.read ( chars, 0, chars.length ) ) > 0 ) {
                buf.append ( chars, 0, count );
            }
            result = new char[buf.length()];
            buf.getChars ( 0, result.length, result, 0 );
        } catch ( IOException e ) {
            JDTCompiler.access$000 ( JDTCompiler.this ).error ( "Compilation error", e );
        }
        return result;
    }
    public char[] getMainTypeName() {
        final int dot = this.className.lastIndexOf ( 46 );
        if ( dot > 0 ) {
            return this.className.substring ( dot + 1 ).toCharArray();
        }
        return this.className.toCharArray();
    }
    public char[][] getPackageName() {
        final StringTokenizer izer = new StringTokenizer ( this.className, "." );
        final char[][] result = new char[izer.countTokens() - 1][];
        for ( int i = 0; i < result.length; ++i ) {
            final String tok = izer.nextToken();
            result[i] = tok.toCharArray();
        }
        return result;
    }
    public boolean ignoreOptionalProblems() {
        return false;
    }
}

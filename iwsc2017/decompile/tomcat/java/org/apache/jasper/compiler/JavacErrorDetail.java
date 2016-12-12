package org.apache.jasper.compiler;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.tomcat.Jar;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import org.apache.jasper.JspCompilationContext;
public class JavacErrorDetail {
    private final String javaFileName;
    private final int javaLineNum;
    private String jspFileName;
    private int jspBeginLineNum;
    private final StringBuilder errMsg;
    private String jspExtract;
    public JavacErrorDetail ( final String javaFileName, final int javaLineNum, final StringBuilder errMsg ) {
        this ( javaFileName, javaLineNum, null, -1, errMsg, null );
    }
    public JavacErrorDetail ( final String javaFileName, final int javaLineNum, final String jspFileName, int jspBeginLineNum, final StringBuilder errMsg, final JspCompilationContext ctxt ) {
        this.jspExtract = null;
        this.javaFileName = javaFileName;
        this.javaLineNum = javaLineNum;
        this.errMsg = errMsg;
        this.jspFileName = jspFileName;
        if ( jspBeginLineNum > 0 && ctxt != null ) {
            InputStream is = null;
            try {
                final Jar tagJar = ctxt.getTagFileJar();
                if ( tagJar != null ) {
                    final String entryName = jspFileName.substring ( 1 );
                    is = tagJar.getInputStream ( entryName );
                    this.jspFileName = tagJar.getURL ( entryName );
                } else {
                    is = ctxt.getResourceAsStream ( jspFileName );
                }
                final String[] jspLines = this.readFile ( is );
                try ( final FileInputStream fis = new FileInputStream ( ctxt.getServletJavaFileName() ) ) {
                    final String[] javaLines = this.readFile ( fis );
                    if ( jspLines.length < jspBeginLineNum ) {
                        this.jspExtract = Localizer.getMessage ( "jsp.error.bug48498" );
                        return;
                    }
                    if ( jspLines[jspBeginLineNum - 1].lastIndexOf ( "<%" ) > jspLines[jspBeginLineNum - 1].lastIndexOf ( "%>" ) ) {
                        final String javaLine = javaLines[javaLineNum - 1].trim();
                        for ( int i = jspBeginLineNum - 1; i < jspLines.length; ++i ) {
                            if ( jspLines[i].indexOf ( javaLine ) != -1 ) {
                                jspBeginLineNum = i + 1;
                                break;
                            }
                        }
                    }
                    final StringBuilder fragment = new StringBuilder ( 1024 );
                    final int startIndex = Math.max ( 0, jspBeginLineNum - 1 - 3 );
                    for ( int endIndex = Math.min ( jspLines.length - 1, jspBeginLineNum - 1 + 3 ), j = startIndex; j <= endIndex; ++j ) {
                        fragment.append ( j + 1 );
                        fragment.append ( ": " );
                        fragment.append ( jspLines[j] );
                        fragment.append ( System.lineSeparator() );
                    }
                    this.jspExtract = fragment.toString();
                }
            } catch ( IOException ex ) {}
            finally {
                if ( is != null ) {
                    try {
                        is.close();
                    } catch ( IOException ex2 ) {}
                }
            }
        }
        this.jspBeginLineNum = jspBeginLineNum;
    }
    public String getJavaFileName() {
        return this.javaFileName;
    }
    public int getJavaLineNumber() {
        return this.javaLineNum;
    }
    public String getJspFileName() {
        return this.jspFileName;
    }
    public int getJspBeginLineNumber() {
        return this.jspBeginLineNum;
    }
    public String getErrorMessage() {
        return this.errMsg.toString();
    }
    public String getJspExtract() {
        return this.jspExtract;
    }
    private String[] readFile ( final InputStream s ) throws IOException {
        final BufferedReader reader = new BufferedReader ( new InputStreamReader ( s ) );
        final List<String> lines = new ArrayList<String>();
        String line;
        while ( ( line = reader.readLine() ) != null ) {
            lines.add ( line );
        }
        return lines.toArray ( new String[lines.size()] );
    }
}

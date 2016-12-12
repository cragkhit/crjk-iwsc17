package org.apache.jasper.compiler;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.Jar;
public class JavacErrorDetail {
    private final String javaFileName;
    private final int javaLineNum;
    private String jspFileName;
    private int jspBeginLineNum;
    private final StringBuilder errMsg;
    private String jspExtract = null;
    public JavacErrorDetail ( String javaFileName,
                              int javaLineNum,
                              StringBuilder errMsg ) {
        this ( javaFileName, javaLineNum, null, -1, errMsg, null );
    }
    public JavacErrorDetail ( String javaFileName,
                              int javaLineNum,
                              String jspFileName,
                              int jspBeginLineNum,
                              StringBuilder errMsg,
                              JspCompilationContext ctxt ) {
        this.javaFileName = javaFileName;
        this.javaLineNum = javaLineNum;
        this.errMsg = errMsg;
        this.jspFileName = jspFileName;
        if ( jspBeginLineNum > 0 && ctxt != null ) {
            InputStream is = null;
            try {
                Jar tagJar = ctxt.getTagFileJar();
                if ( tagJar != null ) {
                    String entryName = jspFileName.substring ( 1 );
                    is = tagJar.getInputStream ( entryName );
                    this.jspFileName = tagJar.getURL ( entryName );
                } else {
                    is = ctxt.getResourceAsStream ( jspFileName );
                }
                String[] jspLines = readFile ( is );
                try ( FileInputStream fis = new FileInputStream ( ctxt.getServletJavaFileName() ) ) {
                    String[] javaLines = readFile ( fis );
                    if ( jspLines.length < jspBeginLineNum ) {
                        jspExtract = Localizer.getMessage ( "jsp.error.bug48498" );
                        return;
                    }
                    if ( jspLines[jspBeginLineNum - 1].lastIndexOf ( "<%" ) >
                            jspLines[jspBeginLineNum - 1].lastIndexOf ( "%>" ) ) {
                        String javaLine = javaLines[javaLineNum - 1].trim();
                        for ( int i = jspBeginLineNum - 1; i < jspLines.length; i++ ) {
                            if ( jspLines[i].indexOf ( javaLine ) != -1 ) {
                                jspBeginLineNum = i + 1;
                                break;
                            }
                        }
                    }
                    StringBuilder fragment = new StringBuilder ( 1024 );
                    int startIndex = Math.max ( 0, jspBeginLineNum - 1 - 3 );
                    int endIndex = Math.min (
                                       jspLines.length - 1, jspBeginLineNum - 1 + 3 );
                    for ( int i = startIndex; i <= endIndex; ++i ) {
                        fragment.append ( i + 1 );
                        fragment.append ( ": " );
                        fragment.append ( jspLines[i] );
                        fragment.append ( System.lineSeparator() );
                    }
                    jspExtract = fragment.toString();
                }
            } catch ( IOException ioe ) {
            } finally {
                if ( is != null ) {
                    try {
                        is.close();
                    } catch ( IOException ignore ) {
                    }
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
    private String[] readFile ( InputStream s ) throws IOException {
        BufferedReader reader = new BufferedReader ( new InputStreamReader ( s ) );
        List<String> lines = new ArrayList<>();
        String line;
        while ( ( line = reader.readLine() ) != null ) {
            lines.add ( line );
        }
        return lines.toArray ( new String[lines.size()] );
    }
}

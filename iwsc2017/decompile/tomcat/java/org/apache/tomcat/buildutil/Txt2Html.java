package org.apache.tomcat.buildutil;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.apache.tools.ant.DirectoryScanner;
import java.util.Iterator;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import java.util.LinkedList;
import org.apache.tools.ant.types.FileSet;
import java.util.List;
import java.io.File;
import org.apache.tools.ant.Task;
public class Txt2Html extends Task {
    private File todir;
    private final List<FileSet> filesets;
    private static final String SOURCE_ENCODING = "ISO-8859-1";
    private static final String LINE_SEPARATOR = "\r\n";
    public Txt2Html() {
        this.filesets = new LinkedList<FileSet>();
    }
    public void setTodir ( final File todir ) {
        this.todir = todir;
    }
    public void addFileset ( final FileSet fs ) {
        this.filesets.add ( fs );
    }
    public void execute() throws BuildException {
        int count = 0;
        for ( final FileSet fs : this.filesets ) {
            final DirectoryScanner ds = fs.getDirectoryScanner ( this.getProject() );
            final File basedir = ds.getBasedir();
            final String[] files = ds.getIncludedFiles();
            for ( int i = 0; i < files.length; ++i ) {
                final File from = new File ( basedir, files[i] );
                final File to = new File ( this.todir, files[i] + ".html" );
                if ( !to.exists() || from.lastModified() > to.lastModified() ) {
                    this.log ( "Converting file '" + from.getAbsolutePath() + "' to '" + to.getAbsolutePath(), 3 );
                    try {
                        this.convert ( from, to );
                    } catch ( IOException e ) {
                        throw new BuildException ( "Could not convert '" + from.getAbsolutePath() + "' to '" + to.getAbsolutePath() + "'", ( Throwable ) e );
                    }
                    ++count;
                }
            }
            if ( count > 0 ) {
                this.log ( "Converted " + count + " file" + ( ( count > 1 ) ? "s" : "" ) + " to " + this.todir.getAbsolutePath() );
            }
        }
    }
    private void convert ( final File from, final File to ) throws IOException {
        try ( final BufferedReader in = new BufferedReader ( new InputStreamReader ( new FileInputStream ( from ), "ISO-8859-1" ) );
                    final PrintWriter out = new PrintWriter ( new OutputStreamWriter ( new FileOutputStream ( to ), "UTF-8" ) ) ) {
            out.print ( "<!DOCTYPE html><html><head><meta charset=\"UTF-8\" /><title>Source Code</title></head><body><pre>" );
            String line;
            while ( ( line = in.readLine() ) != null ) {
                final StringBuilder result = new StringBuilder();
                for ( int len = line.length(), i = 0; i < len; ++i ) {
                    final char c = line.charAt ( i );
                    switch ( c ) {
                    case '&': {
                        result.append ( "&amp;" );
                        break;
                    }
                    case '<': {
                        result.append ( "&lt;" );
                        break;
                    }
                    default: {
                        result.append ( c );
                        break;
                    }
                    }
                }
                out.print ( result.toString() + "\r\n" );
            }
            out.print ( "</pre></body></html>" );
        }
    }
}

package org.apache.tomcat.buildutil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
public class Txt2Html
    extends Task {
    private File todir;
    private final List<FileSet> filesets = new LinkedList<>();
    private static final String SOURCE_ENCODING = "ISO-8859-1";
    private static final String LINE_SEPARATOR = "\r\n";
    public void setTodir ( File todir ) {
        this.todir = todir;
    }
    public void addFileset ( FileSet fs ) {
        filesets.add ( fs );
    }
    @Override
    public void execute()
    throws BuildException {
        int count = 0;
        Iterator<FileSet> iter = filesets.iterator();
        while ( iter.hasNext() ) {
            FileSet fs = iter.next();
            DirectoryScanner ds = fs.getDirectoryScanner ( getProject() );
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for ( int i = 0; i < files.length; i++ ) {
                File from = new File ( basedir, files[i] );
                File to = new File ( todir, files[i] + ".html" );
                if ( !to.exists() ||
                        ( from.lastModified() > to.lastModified() ) ) {
                    log ( "Converting file '" + from.getAbsolutePath() +
                          "' to '" + to.getAbsolutePath(), Project.MSG_VERBOSE );
                    try {
                        convert ( from, to );
                    } catch ( IOException e ) {
                        throw new BuildException ( "Could not convert '" +
                                                   from.getAbsolutePath() + "' to '" +
                                                   to.getAbsolutePath() + "'", e );
                    }
                    count++;
                }
            }
            if ( count > 0 ) {
                log ( "Converted " + count + " file" + ( count > 1 ? "s" : "" ) +
                      " to " + todir.getAbsolutePath() );
            }
        }
    }
    private void convert ( File from, File to )
    throws IOException {
        try ( BufferedReader in = new BufferedReader ( new InputStreamReader (
                        new FileInputStream ( from ), SOURCE_ENCODING ) ) ) {
            try ( PrintWriter out = new PrintWriter ( new OutputStreamWriter (
                            new FileOutputStream ( to ), "UTF-8" ) ) ) {
                out.print ( "<!DOCTYPE html><html><head><meta charset=\"UTF-8\" />"
                            + "<title>Source Code</title></head><body><pre>" );
                String line;
                while ( ( line = in.readLine() ) != null ) {
                    StringBuilder result = new StringBuilder();
                    int len = line.length();
                    for ( int i = 0; i < len; i++ ) {
                        char c = line.charAt ( i );
                        switch ( c ) {
                        case '&':
                            result.append ( "&amp;" );
                            break;
                        case '<':
                            result.append ( "&lt;" );
                            break;
                        default:
                            result.append ( c );
                        }
                    }
                    out.print ( result.toString() + LINE_SEPARATOR );
                }
                out.print ( "</pre></body></html>" );
            }
        }
    }
}

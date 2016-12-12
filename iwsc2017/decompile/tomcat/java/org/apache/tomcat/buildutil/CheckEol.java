package org.apache.tomcat.buildutil;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import org.apache.tools.ant.DirectoryScanner;
import java.util.Iterator;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.tools.ant.types.FileSet;
import java.util.List;
import org.apache.tools.ant.Task;
public class CheckEol extends Task {
    private final List<FileSet> filesets;
    public CheckEol() {
        this.filesets = new LinkedList<FileSet>();
    }
    public void addFileset ( final FileSet fs ) {
        this.filesets.add ( fs );
    }
    public void execute() throws BuildException {
        Mode mode = null;
        if ( "\n".equals ( System.lineSeparator() ) ) {
            mode = Mode.LF;
        } else {
            if ( !"\r\n".equals ( System.lineSeparator() ) ) {
                this.log ( "Line ends check skipped, because OS line ends setting is neither LF nor CRLF.", 3 );
                return;
            }
            mode = Mode.CRLF;
        }
        int count = 0;
        final List<CheckFailure> errors = new ArrayList<CheckFailure>();
        for ( final FileSet fs : this.filesets ) {
            final DirectoryScanner ds = fs.getDirectoryScanner ( this.getProject() );
            final File basedir = ds.getBasedir();
            final String[] files = ds.getIncludedFiles();
            if ( files.length > 0 ) {
                this.log ( "Checking line ends in " + files.length + " file(s)" );
                for ( int i = 0; i < files.length; ++i ) {
                    final File file = new File ( basedir, files[i] );
                    this.log ( "Checking file '" + file + "' for correct line ends", 4 );
                    try {
                        this.check ( file, errors, mode );
                    } catch ( IOException e ) {
                        throw new BuildException ( "Could not check file '" + file.getAbsolutePath() + "'", ( Throwable ) e );
                    }
                    ++count;
                }
            }
        }
        if ( count > 0 ) {
            this.log ( "Done line ends check in " + count + " file(s), " + errors.size() + " error(s) found." );
        }
        if ( errors.size() > 0 ) {
            final String message = "The following files have wrong line ends: " + errors;
            this.log ( message, 0 );
            throw new BuildException ( message );
        }
    }
    private void check ( final File file, final List<CheckFailure> errors, final Mode mode ) throws IOException {
        try ( final FileInputStream fis = new FileInputStream ( file );
                    final BufferedInputStream is = new BufferedInputStream ( fis ) ) {
            int line = 1;
            int prev = -1;
            int ch;
            while ( ( ch = is.read() ) != -1 ) {
                if ( ch == 10 ) {
                    if ( mode == Mode.LF && prev == 13 ) {
                        errors.add ( new CheckFailure ( file, line, "CRLF" ) );
                        return;
                    }
                    if ( mode == Mode.CRLF && prev != 13 ) {
                        errors.add ( new CheckFailure ( file, line, "LF" ) );
                        return;
                    }
                    ++line;
                } else if ( prev == 13 ) {
                    errors.add ( new CheckFailure ( file, line, "CR" ) );
                    return;
                }
                prev = ch;
            }
        }
    }
    private enum Mode {
        LF,
        CRLF;
    }
    private static class CheckFailure {
        private final File file;
        private final int line;
        private final String value;
        public CheckFailure ( final File file, final int line, final String value ) {
            this.file = file;
            this.line = line;
            this.value = value;
        }
        @Override
        public String toString() {
            return System.lineSeparator() + this.file + ": uses " + this.value + " on line " + this.line;
        }
    }
}

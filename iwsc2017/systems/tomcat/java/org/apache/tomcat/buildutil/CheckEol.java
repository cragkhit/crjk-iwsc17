package org.apache.tomcat.buildutil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
public class CheckEol extends Task {
    private final List<FileSet> filesets = new LinkedList<>();
    public void addFileset ( FileSet fs ) {
        filesets.add ( fs );
    }
    @Override
    public void execute() throws BuildException {
        Mode mode = null;
        if ( "\n".equals ( System.lineSeparator() ) ) {
            mode = Mode.LF;
        } else if ( "\r\n".equals ( System.lineSeparator() ) ) {
            mode = Mode.CRLF;
        } else {
            log ( "Line ends check skipped, because OS line ends setting is neither LF nor CRLF.",
                  Project.MSG_VERBOSE );
            return;
        }
        int count = 0;
        List<CheckFailure> errors = new ArrayList<>();
        for ( FileSet fs : filesets ) {
            DirectoryScanner ds = fs.getDirectoryScanner ( getProject() );
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            if ( files.length > 0 ) {
                log ( "Checking line ends in " + files.length + " file(s)" );
                for ( int i = 0; i < files.length; i++ ) {
                    File file = new File ( basedir, files[i] );
                    log ( "Checking file '" + file + "' for correct line ends",
                          Project.MSG_DEBUG );
                    try {
                        check ( file, errors, mode );
                    } catch ( IOException e ) {
                        throw new BuildException ( "Could not check file '"
                                                   + file.getAbsolutePath() + "'", e );
                    }
                    count++;
                }
            }
        }
        if ( count > 0 ) {
            log ( "Done line ends check in " + count + " file(s), "
                  + errors.size() + " error(s) found." );
        }
        if ( errors.size() > 0 ) {
            String message = "The following files have wrong line ends: "
                             + errors;
            log ( message, Project.MSG_ERR );
            throw new BuildException ( message );
        }
    }
    private static enum Mode {
        LF, CRLF
    }
    private static class CheckFailure {
        private final File file;
        private final int line;
        private final String value;
        public CheckFailure ( File file, int line, String value ) {
            this.file = file;
            this.line = line;
            this.value = value;
        }
        @Override
        public String toString() {
            return System.lineSeparator() + file + ": uses " + value + " on line " + line;
        }
    }
    private void check ( File file, List<CheckFailure> errors, Mode mode ) throws IOException {
        try ( FileInputStream fis = new FileInputStream ( file );
                    BufferedInputStream is = new BufferedInputStream ( fis ) ) {
            int line = 1;
            int prev = -1;
            int ch;
            while ( ( ch = is.read() ) != -1 ) {
                if ( ch == '\n' ) {
                    if ( mode == Mode.LF && prev == '\r' ) {
                        errors.add ( new CheckFailure ( file, line, "CRLF" ) );
                        return;
                    } else if ( mode == Mode.CRLF && prev != '\r' ) {
                        errors.add ( new CheckFailure ( file, line, "LF" ) );
                        return;
                    }
                    line++;
                } else if ( prev == '\r' ) {
                    errors.add ( new CheckFailure ( file, line, "CR" ) );
                    return;
                }
                prev = ch;
            }
        }
    }
}

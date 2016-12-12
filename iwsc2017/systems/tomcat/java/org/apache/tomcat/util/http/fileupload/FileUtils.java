package org.apache.tomcat.util.http.fileupload;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
public class FileUtils {
    public FileUtils() {
        super();
    }
    public static void deleteDirectory ( File directory ) throws IOException {
        if ( !directory.exists() ) {
            return;
        }
        if ( !isSymlink ( directory ) ) {
            cleanDirectory ( directory );
        }
        if ( !directory.delete() ) {
            String message =
                "Unable to delete directory " + directory + ".";
            throw new IOException ( message );
        }
    }
    public static void cleanDirectory ( File directory ) throws IOException {
        if ( !directory.exists() ) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException ( message );
        }
        if ( !directory.isDirectory() ) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException ( message );
        }
        File[] files = directory.listFiles();
        if ( files == null ) {
            throw new IOException ( "Failed to list contents of " + directory );
        }
        IOException exception = null;
        for ( File file : files ) {
            try {
                forceDelete ( file );
            } catch ( IOException ioe ) {
                exception = ioe;
            }
        }
        if ( null != exception ) {
            throw exception;
        }
    }
    public static void forceDelete ( File file ) throws IOException {
        if ( file.isDirectory() ) {
            deleteDirectory ( file );
        } else {
            boolean filePresent = file.exists();
            if ( !file.delete() ) {
                if ( !filePresent ) {
                    throw new FileNotFoundException ( "File does not exist: " + file );
                }
                String message =
                    "Unable to delete file: " + file;
                throw new IOException ( message );
            }
        }
    }
    public static void forceDeleteOnExit ( File file ) throws IOException {
        if ( file.isDirectory() ) {
            deleteDirectoryOnExit ( file );
        } else {
            file.deleteOnExit();
        }
    }
    private static void deleteDirectoryOnExit ( File directory ) throws IOException {
        if ( !directory.exists() ) {
            return;
        }
        directory.deleteOnExit();
        if ( !isSymlink ( directory ) ) {
            cleanDirectoryOnExit ( directory );
        }
    }
    private static void cleanDirectoryOnExit ( File directory ) throws IOException {
        if ( !directory.exists() ) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException ( message );
        }
        if ( !directory.isDirectory() ) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException ( message );
        }
        File[] files = directory.listFiles();
        if ( files == null ) {
            throw new IOException ( "Failed to list contents of " + directory );
        }
        IOException exception = null;
        for ( File file : files ) {
            try {
                forceDeleteOnExit ( file );
            } catch ( IOException ioe ) {
                exception = ioe;
            }
        }
        if ( null != exception ) {
            throw exception;
        }
    }
    public static boolean isSymlink ( File file ) throws IOException {
        if ( file == null ) {
            throw new NullPointerException ( "File must not be null" );
        }
        if ( File.separatorChar == '\\' ) {
            return false;
        }
        File fileInCanonicalDir = null;
        if ( file.getParent() == null ) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File ( canonicalDir, file.getName() );
        }
        if ( fileInCanonicalDir.getCanonicalFile().equals ( fileInCanonicalDir.getAbsoluteFile() ) ) {
            return false;
        } else {
            return true;
        }
    }
}

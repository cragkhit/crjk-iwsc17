package org.junit.rules;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
public class TemporaryFolder extends ExternalResource {
    private final File parentFolder;
    private final boolean assureDeletion;
    private File folder;
    private static final int TEMP_DIR_ATTEMPTS = 10000;
    private static final String TMP_PREFIX = "junit";
    public TemporaryFolder() {
        this ( ( File ) null );
    }
    public TemporaryFolder ( File parentFolder ) {
        this.parentFolder = parentFolder;
        this.assureDeletion = false;
    }
    protected TemporaryFolder ( Builder builder ) {
        this.parentFolder = builder.parentFolder;
        this.assureDeletion = builder.assureDeletion;
    }
    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private File parentFolder;
        private boolean assureDeletion;
        protected Builder() {}
        public Builder parentFolder ( File parentFolder ) {
            this.parentFolder = parentFolder;
            return this;
        }
        public Builder assureDeletion() {
            this.assureDeletion = true;
            return this;
        }
        public TemporaryFolder build() {
            return new TemporaryFolder ( this );
        }
    }
    @Override
    protected void before() throws Throwable {
        create();
    }
    @Override
    protected void after() {
        delete();
    }
    public void create() throws IOException {
        folder = createTemporaryFolderIn ( parentFolder );
    }
    public File newFile ( String fileName ) throws IOException {
        File file = new File ( getRoot(), fileName );
        if ( !file.createNewFile() ) {
            throw new IOException (
                "a file with the name \'" + fileName + "\' already exists in the test folder" );
        }
        return file;
    }
    public File newFile() throws IOException {
        return File.createTempFile ( TMP_PREFIX, null, getRoot() );
    }
    public File newFolder ( String folder ) throws IOException {
        return newFolder ( new String[] {folder} );
    }
    public File newFolder ( String... folderNames ) throws IOException {
        File file = getRoot();
        for ( int i = 0; i < folderNames.length; i++ ) {
            String folderName = folderNames[i];
            validateFolderName ( folderName );
            file = new File ( file, folderName );
            if ( !file.mkdir() && isLastElementInArray ( i, folderNames ) ) {
                throw new IOException (
                    "a folder with the name \'" + folderName + "\' already exists" );
            }
        }
        return file;
    }
    private void validateFolderName ( String folderName ) throws IOException {
        File tempFile = new File ( folderName );
        if ( tempFile.getParent() != null ) {
            String errorMsg = "Folder name cannot consist of multiple path components separated by a file separator."
                              + " Please use newFolder('MyParentFolder','MyFolder') to create hierarchies of folders";
            throw new IOException ( errorMsg );
        }
    }
    private boolean isLastElementInArray ( int index, String[] array ) {
        return index == array.length - 1;
    }
    public File newFolder() throws IOException {
        return createTemporaryFolderIn ( getRoot() );
    }
    private File createTemporaryFolderIn ( File parentFolder ) throws IOException {
        File createdFolder = null;
        for ( int i = 0; i < TEMP_DIR_ATTEMPTS; ++i ) {
            String suffix = ".tmp";
            File tmpFile = File.createTempFile ( TMP_PREFIX, suffix, parentFolder );
            String tmpName = tmpFile.toString();
            String folderName = tmpName.substring ( 0, tmpName.length() - suffix.length() );
            createdFolder = new File ( folderName );
            if ( createdFolder.mkdir() ) {
                tmpFile.delete();
                return createdFolder;
            }
            tmpFile.delete();
        }
        throw new IOException ( "Unable to create temporary directory in: "
                                + parentFolder.toString() + ". Tried " + TEMP_DIR_ATTEMPTS + " times. "
                                + "Last attempted to create: " + createdFolder.toString() );
    }
    public File getRoot() {
        if ( folder == null ) {
            throw new IllegalStateException (
                "the temporary folder has not yet been created" );
        }
        return folder;
    }
    public void delete() {
        if ( !tryDelete() ) {
            if ( assureDeletion ) {
                fail ( "Unable to clean up temporary folder " + folder );
            }
        }
    }
    protected boolean tryDelete() {
        if ( folder == null ) {
            return true;
        }
        return recursiveDelete ( folder );
    }
    private boolean recursiveDelete ( File file ) {
        if ( file.delete() ) {
            return true;
        }
        boolean result = true;
        File[] files = file.listFiles();
        if ( files != null ) {
            for ( File each : files ) {
                result = result && recursiveDelete ( each );
            }
        }
        return result && file.delete();
    }
}

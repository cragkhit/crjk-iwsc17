package org.junit.rules;
import org.junit.Assert;
import java.io.IOException;
import java.io.File;
public class TemporaryFolder extends ExternalResource {
    private final File parentFolder;
    private final boolean assureDeletion;
    private File folder;
    private static final int TEMP_DIR_ATTEMPTS = 10000;
    private static final String TMP_PREFIX = "junit";
    public TemporaryFolder() {
        this ( ( File ) null );
    }
    public TemporaryFolder ( final File parentFolder ) {
        this.parentFolder = parentFolder;
        this.assureDeletion = false;
    }
    protected TemporaryFolder ( final Builder builder ) {
        this.parentFolder = builder.parentFolder;
        this.assureDeletion = builder.assureDeletion;
    }
    public static Builder builder() {
        return new Builder();
    }
    protected void before() throws Throwable {
        this.create();
    }
    protected void after() {
        this.delete();
    }
    public void create() throws IOException {
        this.folder = this.createTemporaryFolderIn ( this.parentFolder );
    }
    public File newFile ( final String fileName ) throws IOException {
        final File file = new File ( this.getRoot(), fileName );
        if ( !file.createNewFile() ) {
            throw new IOException ( "a file with the name '" + fileName + "' already exists in the test folder" );
        }
        return file;
    }
    public File newFile() throws IOException {
        return File.createTempFile ( "junit", null, this.getRoot() );
    }
    public File newFolder ( final String folder ) throws IOException {
        return this.newFolder ( new String[] { folder } );
    }
    public File newFolder ( final String... folderNames ) throws IOException {
        File file = this.getRoot();
        for ( int i = 0; i < folderNames.length; ++i ) {
            final String folderName = folderNames[i];
            this.validateFolderName ( folderName );
            file = new File ( file, folderName );
            if ( !file.mkdir() && this.isLastElementInArray ( i, folderNames ) ) {
                throw new IOException ( "a folder with the name '" + folderName + "' already exists" );
            }
        }
        return file;
    }
    private void validateFolderName ( final String folderName ) throws IOException {
        final File tempFile = new File ( folderName );
        if ( tempFile.getParent() != null ) {
            final String errorMsg = "Folder name cannot consist of multiple path components separated by a file separator. Please use newFolder('MyParentFolder','MyFolder') to create hierarchies of folders";
            throw new IOException ( errorMsg );
        }
    }
    private boolean isLastElementInArray ( final int index, final String[] array ) {
        return index == array.length - 1;
    }
    public File newFolder() throws IOException {
        return this.createTemporaryFolderIn ( this.getRoot() );
    }
    private File createTemporaryFolderIn ( final File parentFolder ) throws IOException {
        File createdFolder = null;
        for ( int i = 0; i < 10000; ++i ) {
            final String suffix = ".tmp";
            final File tmpFile = File.createTempFile ( "junit", suffix, parentFolder );
            final String tmpName = tmpFile.toString();
            final String folderName = tmpName.substring ( 0, tmpName.length() - suffix.length() );
            createdFolder = new File ( folderName );
            if ( createdFolder.mkdir() ) {
                tmpFile.delete();
                return createdFolder;
            }
            tmpFile.delete();
        }
        throw new IOException ( "Unable to create temporary directory in: " + parentFolder.toString() + ". Tried " + 10000 + " times. Last attempted to create: " + createdFolder.toString() );
    }
    public File getRoot() {
        if ( this.folder == null ) {
            throw new IllegalStateException ( "the temporary folder has not yet been created" );
        }
        return this.folder;
    }
    public void delete() {
        if ( !this.tryDelete() && this.assureDeletion ) {
            Assert.fail ( "Unable to clean up temporary folder " + this.folder );
        }
    }
    protected boolean tryDelete() {
        return this.folder == null || this.recursiveDelete ( this.folder );
    }
    private boolean recursiveDelete ( final File file ) {
        if ( file.delete() ) {
            return true;
        }
        boolean result = true;
        final File[] files = file.listFiles();
        if ( files != null ) {
            for ( final File each : files ) {
                result = ( result && this.recursiveDelete ( each ) );
            }
        }
        return result && file.delete();
    }
    public static class Builder {
        private File parentFolder;
        private boolean assureDeletion;
        public Builder parentFolder ( final File parentFolder ) {
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
}

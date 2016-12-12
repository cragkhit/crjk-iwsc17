package org.apache.tomcat.util.http.fileupload;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public class DeferredFileOutputStream
    extends ThresholdingOutputStream {
    private ByteArrayOutputStream memoryOutputStream;
    private OutputStream currentOutputStream;
    private File outputFile;
    private final String prefix;
    private final String suffix;
    private final File directory;
    public DeferredFileOutputStream ( int threshold, File outputFile ) {
        this ( threshold,  outputFile, null, null, null );
    }
    private DeferredFileOutputStream ( int threshold, File outputFile, String prefix, String suffix, File directory ) {
        super ( threshold );
        this.outputFile = outputFile;
        memoryOutputStream = new ByteArrayOutputStream();
        currentOutputStream = memoryOutputStream;
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
    }
    @Override
    protected OutputStream getStream() throws IOException {
        return currentOutputStream;
    }
    @Override
    protected void thresholdReached() throws IOException {
        if ( prefix != null ) {
            outputFile = File.createTempFile ( prefix, suffix, directory );
        }
        FileOutputStream fos = new FileOutputStream ( outputFile );
        memoryOutputStream.writeTo ( fos );
        currentOutputStream = fos;
        memoryOutputStream = null;
    }
    public boolean isInMemory() {
        return !isThresholdExceeded();
    }
    public byte[] getData() {
        if ( memoryOutputStream != null ) {
            return memoryOutputStream.toByteArray();
        }
        return null;
    }
    public File getFile() {
        return outputFile;
    }
    @Override
    public void close() throws IOException {
        super.close();
    }
}

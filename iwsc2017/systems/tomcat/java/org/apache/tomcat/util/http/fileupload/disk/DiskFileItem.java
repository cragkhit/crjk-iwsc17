package org.apache.tomcat.util.http.fileupload.disk;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.http.fileupload.DeferredFileOutputStream;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemHeaders;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.ParameterParser;
import org.apache.tomcat.util.http.fileupload.util.Streams;
public class DiskFileItem
    implements FileItem {
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    private static final String UID =
        UUID.randomUUID().toString().replace ( '-', '_' );
    private static final AtomicInteger COUNTER = new AtomicInteger ( 0 );
    private String fieldName;
    private final String contentType;
    private boolean isFormField;
    private final String fileName;
    private long size = -1;
    private final int sizeThreshold;
    private final File repository;
    private byte[] cachedContent;
    private transient DeferredFileOutputStream dfos;
    private transient File tempFile;
    private FileItemHeaders headers;
    public DiskFileItem ( String fieldName,
                          String contentType, boolean isFormField, String fileName,
                          int sizeThreshold, File repository ) {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
    }
    @Override
    public InputStream getInputStream()
    throws IOException {
        if ( !isInMemory() ) {
            return new FileInputStream ( dfos.getFile() );
        }
        if ( cachedContent == null ) {
            cachedContent = dfos.getData();
        }
        return new ByteArrayInputStream ( cachedContent );
    }
    @Override
    public String getContentType() {
        return contentType;
    }
    public String getCharSet() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames ( true );
        Map<String, String> params = parser.parse ( getContentType(), ';' );
        return params.get ( "charset" );
    }
    @Override
    public String getName() {
        return Streams.checkFileName ( fileName );
    }
    @Override
    public boolean isInMemory() {
        if ( cachedContent != null ) {
            return true;
        }
        return dfos.isInMemory();
    }
    @Override
    public long getSize() {
        if ( size >= 0 ) {
            return size;
        } else if ( cachedContent != null ) {
            return cachedContent.length;
        } else if ( dfos.isInMemory() ) {
            return dfos.getData().length;
        } else {
            return dfos.getFile().length();
        }
    }
    @Override
    public byte[] get() {
        if ( isInMemory() ) {
            if ( cachedContent == null && dfos != null ) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }
        byte[] fileData = new byte[ ( int ) getSize()];
        InputStream fis = null;
        try {
            fis = new FileInputStream ( dfos.getFile() );
            IOUtils.readFully ( fis, fileData );
        } catch ( IOException e ) {
            fileData = null;
        } finally {
            IOUtils.closeQuietly ( fis );
        }
        return fileData;
    }
    @Override
    public String getString ( final String charset )
    throws UnsupportedEncodingException {
        return new String ( get(), charset );
    }
    @Override
    public String getString() {
        byte[] rawdata = get();
        String charset = getCharSet();
        if ( charset == null ) {
            charset = DEFAULT_CHARSET;
        }
        try {
            return new String ( rawdata, charset );
        } catch ( UnsupportedEncodingException e ) {
            return new String ( rawdata );
        }
    }
    @Override
    public void write ( File file ) throws Exception {
        if ( isInMemory() ) {
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream ( file );
                fout.write ( get() );
                fout.close();
            } finally {
                IOUtils.closeQuietly ( fout );
            }
        } else {
            File outputFile = getStoreLocation();
            if ( outputFile != null ) {
                size = outputFile.length();
                if ( !outputFile.renameTo ( file ) ) {
                    BufferedInputStream in = null;
                    BufferedOutputStream out = null;
                    try {
                        in = new BufferedInputStream (
                            new FileInputStream ( outputFile ) );
                        out = new BufferedOutputStream (
                            new FileOutputStream ( file ) );
                        IOUtils.copy ( in, out );
                        out.close();
                    } finally {
                        IOUtils.closeQuietly ( in );
                        IOUtils.closeQuietly ( out );
                    }
                }
            } else {
                throw new FileUploadException (
                    "Cannot write uploaded file to disk!" );
            }
        }
    }
    @Override
    public void delete() {
        cachedContent = null;
        File outputFile = getStoreLocation();
        if ( outputFile != null && outputFile.exists() ) {
            outputFile.delete();
        }
    }
    @Override
    public String getFieldName() {
        return fieldName;
    }
    @Override
    public void setFieldName ( String fieldName ) {
        this.fieldName = fieldName;
    }
    @Override
    public boolean isFormField() {
        return isFormField;
    }
    @Override
    public void setFormField ( boolean state ) {
        isFormField = state;
    }
    @Override
    public OutputStream getOutputStream()
    throws IOException {
        if ( dfos == null ) {
            File outputFile = getTempFile();
            dfos = new DeferredFileOutputStream ( sizeThreshold, outputFile );
        }
        return dfos;
    }
    public File getStoreLocation() {
        if ( dfos == null ) {
            return null;
        }
        if ( isInMemory() ) {
            return null;
        }
        return dfos.getFile();
    }
    @Override
    protected void finalize() {
        if ( dfos == null ) {
            return;
        }
        File outputFile = dfos.getFile();
        if ( outputFile != null && outputFile.exists() ) {
            outputFile.delete();
        }
    }
    protected File getTempFile() {
        if ( tempFile == null ) {
            File tempDir = repository;
            if ( tempDir == null ) {
                tempDir = new File ( System.getProperty ( "java.io.tmpdir" ) );
            }
            String tempFileName =
                String.format ( "upload_%s_%s.tmp", UID, getUniqueId() );
            tempFile = new File ( tempDir, tempFileName );
        }
        return tempFile;
    }
    private static String getUniqueId() {
        final int limit = 100000000;
        int current = COUNTER.getAndIncrement();
        String id = Integer.toString ( current );
        if ( current < limit ) {
            id = ( "00000000" + id ).substring ( id.length() );
        }
        return id;
    }
    @Override
    public String toString() {
        return String.format ( "name=%s, StoreLocation=%s, size=%s bytes, isFormField=%s, FieldName=%s",
                               getName(), getStoreLocation(), Long.valueOf ( getSize() ),
                               Boolean.valueOf ( isFormField() ), getFieldName() );
    }
    @Override
    public FileItemHeaders getHeaders() {
        return headers;
    }
    @Override
    public void setHeaders ( FileItemHeaders pHeaders ) {
        headers = pHeaders;
    }
}

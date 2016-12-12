package org.apache.tomcat.util.http.fileupload;
import org.apache.tomcat.util.http.fileupload.util.Closeable;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import java.io.IOException;
import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream;
import java.io.InputStream;
class FileItemStreamImpl implements FileItemStream {
    private final String contentType;
    private final String fieldName;
    private final String name;
    private final boolean formField;
    private final InputStream stream;
    private boolean opened;
    private FileItemHeaders headers;
    FileItemStreamImpl ( final String pName, final String pFieldName, final String pContentType, final boolean pFormField, final long pContentLength ) throws IOException {
        this.name = pName;
        this.fieldName = pFieldName;
        this.contentType = pContentType;
        this.formField = pFormField;
        InputStream istream;
        final MultipartStream.ItemInputStream itemStream = ( MultipartStream.ItemInputStream ) ( istream = FileItemIteratorImpl.access$100 ( FileItemIteratorImpl.this ).newInputStream() );
        if ( FileUploadBase.access$200 ( FileItemIteratorImpl.this.this$0 ) != -1L ) {
            if ( pContentLength != -1L && pContentLength > FileUploadBase.access$200 ( FileItemIteratorImpl.this.this$0 ) ) {
                final FileSizeLimitExceededException e = new FileSizeLimitExceededException ( String.format ( "The field %s exceeds its maximum permitted size of %s bytes.", this.fieldName, FileUploadBase.access$200 ( FileItemIteratorImpl.this.this$0 ) ), pContentLength, FileUploadBase.access$200 ( FileItemIteratorImpl.this.this$0 ) );
                e.setFileName ( pName );
                e.setFieldName ( pFieldName );
                throw new FileUploadIOException ( e );
            }
            istream = new LimitedInputStream ( istream, FileUploadBase.access$200 ( FileItemIteratorImpl.this.this$0 ) ) {
                @Override
                protected void raiseError ( final long pSizeMax, final long pCount ) throws IOException {
                    itemStream.close ( true );
                    final FileSizeLimitExceededException e = new FileSizeLimitExceededException ( String.format ( "The field %s exceeds its maximum permitted size of %s bytes.", FileItemStreamImpl.this.fieldName, pSizeMax ), pCount, pSizeMax );
                    e.setFieldName ( FileItemStreamImpl.this.fieldName );
                    e.setFileName ( FileItemStreamImpl.this.name );
                    throw new FileUploadIOException ( e );
                }
            };
        }
        this.stream = istream;
    }
    @Override
    public String getContentType() {
        return this.contentType;
    }
    @Override
    public String getFieldName() {
        return this.fieldName;
    }
    @Override
    public String getName() {
        return Streams.checkFileName ( this.name );
    }
    @Override
    public boolean isFormField() {
        return this.formField;
    }
    @Override
    public InputStream openStream() throws IOException {
        if ( this.opened ) {
            throw new IllegalStateException ( "The stream was already opened." );
        }
        if ( ( ( Closeable ) this.stream ).isClosed() ) {
            throw new ItemSkippedException();
        }
        return this.stream;
    }
    void close() throws IOException {
        this.stream.close();
    }
    @Override
    public FileItemHeaders getHeaders() {
        return this.headers;
    }
    @Override
    public void setHeaders ( final FileItemHeaders pHeaders ) {
        this.headers = pHeaders;
    }
}

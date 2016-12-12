package org.apache.tomcat.util.http.fileupload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import java.util.NoSuchElementException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream;
import java.util.Locale;
private class FileItemIteratorImpl implements FileItemIterator {
    private final MultipartStream multi;
    private final MultipartStream.ProgressNotifier notifier;
    private final byte[] boundary;
    private FileItemStreamImpl currentItem;
    private String currentFieldName;
    private boolean skipPreamble;
    private boolean itemValid;
    private boolean eof;
    final   FileUploadBase this$0;
    FileItemIteratorImpl ( final RequestContext ctx ) throws FileUploadException, IOException {
        if ( ctx == null ) {
            throw new NullPointerException ( "ctx parameter" );
        }
        final String contentType = ctx.getContentType();
        if ( null == contentType || !contentType.toLowerCase ( Locale.ENGLISH ).startsWith ( "multipart/" ) ) {
            throw new InvalidContentTypeException ( String.format ( "the request doesn't contain a %s or %s stream, content type header is %s", "multipart/form-data", "multipart/mixed", contentType ) );
        }
        final long requestSize = ( ( UploadContext ) ctx ).contentLength();
        InputStream input;
        if ( FileUploadBase.access$400 ( FileUploadBase.this ) >= 0L ) {
            if ( requestSize != -1L && requestSize > FileUploadBase.access$400 ( FileUploadBase.this ) ) {
                throw new SizeLimitExceededException ( String.format ( "the request was rejected because its size (%s) exceeds the configured maximum (%s)", requestSize, FileUploadBase.access$400 ( FileUploadBase.this ) ), requestSize, FileUploadBase.access$400 ( FileUploadBase.this ) );
            }
            input = new LimitedInputStream ( ctx.getInputStream(), FileUploadBase.access$400 ( FileUploadBase.this ) ) {
                @Override
                protected void raiseError ( final long pSizeMax, final long pCount ) throws IOException {
                    final FileUploadException ex = new SizeLimitExceededException ( String.format ( "the request was rejected because its size (%s) exceeds the configured maximum (%s)", pCount, pSizeMax ), pCount, pSizeMax );
                    throw new FileUploadIOException ( ex );
                }
            };
        } else {
            input = ctx.getInputStream();
        }
        String charEncoding = FileUploadBase.access$500 ( FileUploadBase.this );
        if ( charEncoding == null ) {
            charEncoding = ctx.getCharacterEncoding();
        }
        this.boundary = FileUploadBase.this.getBoundary ( contentType );
        if ( this.boundary == null ) {
            IOUtils.closeQuietly ( input );
            throw new FileUploadException ( "the request was rejected because no multipart boundary was found" );
        }
        this.notifier = new MultipartStream.ProgressNotifier ( FileUploadBase.access$600 ( FileUploadBase.this ), requestSize );
        try {
            this.multi = new MultipartStream ( input, this.boundary, this.notifier );
        } catch ( IllegalArgumentException iae ) {
            IOUtils.closeQuietly ( input );
            throw new InvalidContentTypeException ( String.format ( "The boundary specified in the %s header is too long", "Content-type" ), iae );
        }
        this.multi.setHeaderEncoding ( charEncoding );
        this.skipPreamble = true;
        this.findNextItem();
    }
    private boolean findNextItem() throws IOException {
        if ( this.eof ) {
            return false;
        }
        if ( this.currentItem != null ) {
            this.currentItem.close();
            this.currentItem = null;
        }
        while ( true ) {
            boolean nextPart;
            if ( this.skipPreamble ) {
                nextPart = this.multi.skipPreamble();
            } else {
                nextPart = this.multi.readBoundary();
            }
            if ( !nextPart ) {
                if ( this.currentFieldName == null ) {
                    this.eof = true;
                    return false;
                }
                this.multi.setBoundary ( this.boundary );
                this.currentFieldName = null;
            } else {
                final FileItemHeaders headers = FileUploadBase.this.getParsedHeaders ( this.multi.readHeaders() );
                if ( this.currentFieldName == null ) {
                    final String fieldName = FileUploadBase.this.getFieldName ( headers );
                    if ( fieldName != null ) {
                        final String subContentType = headers.getHeader ( "Content-type" );
                        if ( subContentType != null && subContentType.toLowerCase ( Locale.ENGLISH ).startsWith ( "multipart/mixed" ) ) {
                            this.currentFieldName = fieldName;
                            final byte[] subBoundary = FileUploadBase.this.getBoundary ( subContentType );
                            this.multi.setBoundary ( subBoundary );
                            this.skipPreamble = true;
                            continue;
                        }
                        final String fileName = FileUploadBase.this.getFileName ( headers );
                        ( this.currentItem = new FileItemStreamImpl ( fileName, fieldName, headers.getHeader ( "Content-type" ), fileName == null, this.getContentLength ( headers ) ) ).setHeaders ( headers );
                        this.notifier.noteItem();
                        return this.itemValid = true;
                    }
                } else {
                    final String fileName2 = FileUploadBase.this.getFileName ( headers );
                    if ( fileName2 != null ) {
                        ( this.currentItem = new FileItemStreamImpl ( fileName2, this.currentFieldName, headers.getHeader ( "Content-type" ), false, this.getContentLength ( headers ) ) ).setHeaders ( headers );
                        this.notifier.noteItem();
                        return this.itemValid = true;
                    }
                }
                this.multi.discardBodyData();
            }
        }
    }
    private long getContentLength ( final FileItemHeaders pHeaders ) {
        try {
            return Long.parseLong ( pHeaders.getHeader ( "Content-length" ) );
        } catch ( Exception e ) {
            return -1L;
        }
    }
    @Override
    public boolean hasNext() throws FileUploadException, IOException {
        if ( this.eof ) {
            return false;
        }
        if ( this.itemValid ) {
            return true;
        }
        try {
            return this.findNextItem();
        } catch ( FileUploadIOException e ) {
            throw ( FileUploadException ) e.getCause();
        }
    }
    @Override
    public FileItemStream next() throws FileUploadException, IOException {
        if ( this.eof || ( !this.itemValid && !this.hasNext() ) ) {
            throw new NoSuchElementException();
        }
        this.itemValid = false;
        return this.currentItem;
    }
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
            final MultipartStream.ItemInputStream itemStream = ( MultipartStream.ItemInputStream ) ( istream = FileItemIteratorImpl.this.multi.newInputStream() );
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
            if ( ( ( org.apache.tomcat.util.http.fileupload.util.Closeable ) this.stream ).isClosed() ) {
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
}

package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.tomcat.util.http.fileupload.MultipartStream.ItemInputStream;
import org.apache.tomcat.util.http.fileupload.util.Closeable;
import org.apache.tomcat.util.http.fileupload.util.FileItemHeadersImpl;
import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream;
import org.apache.tomcat.util.http.fileupload.util.Streams;
public abstract class FileUploadBase {
    public static final boolean isMultipartContent ( RequestContext ctx ) {
        String contentType = ctx.getContentType();
        if ( contentType == null ) {
            return false;
        }
        if ( contentType.toLowerCase ( Locale.ENGLISH ).startsWith ( MULTIPART ) ) {
            return true;
        }
        return false;
    }
    public static final String CONTENT_TYPE = "Content-type";
    public static final String CONTENT_DISPOSITION = "Content-disposition";
    public static final String CONTENT_LENGTH = "Content-length";
    public static final String FORM_DATA = "form-data";
    public static final String ATTACHMENT = "attachment";
    public static final String MULTIPART = "multipart/";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MULTIPART_MIXED = "multipart/mixed";
    private long sizeMax = -1;
    private long fileSizeMax = -1;
    private String headerEncoding;
    private ProgressListener listener;
    public abstract FileItemFactory getFileItemFactory();
    public abstract void setFileItemFactory ( FileItemFactory factory );
    public long getSizeMax() {
        return sizeMax;
    }
    public void setSizeMax ( long sizeMax ) {
        this.sizeMax = sizeMax;
    }
    public long getFileSizeMax() {
        return fileSizeMax;
    }
    public void setFileSizeMax ( long fileSizeMax ) {
        this.fileSizeMax = fileSizeMax;
    }
    public String getHeaderEncoding() {
        return headerEncoding;
    }
    public void setHeaderEncoding ( String encoding ) {
        headerEncoding = encoding;
    }
    public FileItemIterator getItemIterator ( RequestContext ctx )
    throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl ( ctx );
        } catch ( FileUploadIOException e ) {
            throw ( FileUploadException ) e.getCause();
        }
    }
    public List<FileItem> parseRequest ( RequestContext ctx )
    throws FileUploadException {
        List<FileItem> items = new ArrayList<>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator ( ctx );
            FileItemFactory fac = getFileItemFactory();
            if ( fac == null ) {
                throw new NullPointerException ( "No FileItemFactory has been set." );
            }
            while ( iter.hasNext() ) {
                final FileItemStream item = iter.next();
                final String fileName = ( ( FileItemIteratorImpl.FileItemStreamImpl ) item ).name;
                FileItem fileItem = fac.createItem ( item.getFieldName(), item.getContentType(),
                                                     item.isFormField(), fileName );
                items.add ( fileItem );
                try {
                    Streams.copy ( item.openStream(), fileItem.getOutputStream(), true );
                } catch ( FileUploadIOException e ) {
                    throw ( FileUploadException ) e.getCause();
                } catch ( IOException e ) {
                    throw new IOFileUploadException ( String.format ( "Processing of %s request failed. %s",
                                                      MULTIPART_FORM_DATA, e.getMessage() ), e );
                }
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders ( fih );
            }
            successful = true;
            return items;
        } catch ( FileUploadIOException e ) {
            throw ( FileUploadException ) e.getCause();
        } catch ( IOException e ) {
            throw new FileUploadException ( e.getMessage(), e );
        } finally {
            if ( !successful ) {
                for ( FileItem fileItem : items ) {
                    try {
                        fileItem.delete();
                    } catch ( Exception ignored ) {
                    }
                }
            }
        }
    }
    public Map<String, List<FileItem>> parseParameterMap ( RequestContext ctx )
    throws FileUploadException {
        final List<FileItem> items = parseRequest ( ctx );
        final Map<String, List<FileItem>> itemsMap = new HashMap<> ( items.size() );
        for ( FileItem fileItem : items ) {
            String fieldName = fileItem.getFieldName();
            List<FileItem> mappedItems = itemsMap.get ( fieldName );
            if ( mappedItems == null ) {
                mappedItems = new ArrayList<>();
                itemsMap.put ( fieldName, mappedItems );
            }
            mappedItems.add ( fileItem );
        }
        return itemsMap;
    }
    protected byte[] getBoundary ( String contentType ) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames ( true );
        Map<String, String> params =
            parser.parse ( contentType, new char[] {';', ','} );
        String boundaryStr = params.get ( "boundary" );
        if ( boundaryStr == null ) {
            return null;
        }
        byte[] boundary;
        boundary = boundaryStr.getBytes ( StandardCharsets.ISO_8859_1 );
        return boundary;
    }
    protected String getFileName ( FileItemHeaders headers ) {
        return getFileName ( headers.getHeader ( CONTENT_DISPOSITION ) );
    }
    private String getFileName ( String pContentDisposition ) {
        String fileName = null;
        if ( pContentDisposition != null ) {
            String cdl = pContentDisposition.toLowerCase ( Locale.ENGLISH );
            if ( cdl.startsWith ( FORM_DATA ) || cdl.startsWith ( ATTACHMENT ) ) {
                ParameterParser parser = new ParameterParser();
                parser.setLowerCaseNames ( true );
                Map<String, String> params =
                    parser.parse ( pContentDisposition, ';' );
                if ( params.containsKey ( "filename" ) ) {
                    fileName = params.get ( "filename" );
                    if ( fileName != null ) {
                        fileName = fileName.trim();
                    } else {
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }
    protected String getFieldName ( FileItemHeaders headers ) {
        return getFieldName ( headers.getHeader ( CONTENT_DISPOSITION ) );
    }
    private String getFieldName ( String pContentDisposition ) {
        String fieldName = null;
        if ( pContentDisposition != null
                && pContentDisposition.toLowerCase ( Locale.ENGLISH ).startsWith ( FORM_DATA ) ) {
            ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames ( true );
            Map<String, String> params = parser.parse ( pContentDisposition, ';' );
            fieldName = params.get ( "name" );
            if ( fieldName != null ) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
    }
    protected FileItemHeaders getParsedHeaders ( String headerPart ) {
        final int len = headerPart.length();
        FileItemHeadersImpl headers = newFileItemHeaders();
        int start = 0;
        for ( ;; ) {
            int end = parseEndOfLine ( headerPart, start );
            if ( start == end ) {
                break;
            }
            StringBuilder header = new StringBuilder ( headerPart.substring ( start, end ) );
            start = end + 2;
            while ( start < len ) {
                int nonWs = start;
                while ( nonWs < len ) {
                    char c = headerPart.charAt ( nonWs );
                    if ( c != ' '  &&  c != '\t' ) {
                        break;
                    }
                    ++nonWs;
                }
                if ( nonWs == start ) {
                    break;
                }
                end = parseEndOfLine ( headerPart, nonWs );
                header.append ( " " ).append ( headerPart.substring ( nonWs, end ) );
                start = end + 2;
            }
            parseHeaderLine ( headers, header.toString() );
        }
        return headers;
    }
    protected FileItemHeadersImpl newFileItemHeaders() {
        return new FileItemHeadersImpl();
    }
    private int parseEndOfLine ( String headerPart, int end ) {
        int index = end;
        for ( ;; ) {
            int offset = headerPart.indexOf ( '\r', index );
            if ( offset == -1  ||  offset + 1 >= headerPart.length() ) {
                throw new IllegalStateException (
                    "Expected headers to be terminated by an empty line." );
            }
            if ( headerPart.charAt ( offset + 1 ) == '\n' ) {
                return offset;
            }
            index = offset + 1;
        }
    }
    private void parseHeaderLine ( FileItemHeadersImpl headers, String header ) {
        final int colonOffset = header.indexOf ( ':' );
        if ( colonOffset == -1 ) {
            return;
        }
        String headerName = header.substring ( 0, colonOffset ).trim();
        String headerValue =
            header.substring ( header.indexOf ( ':' ) + 1 ).trim();
        headers.addHeader ( headerName, headerValue );
    }
    private class FileItemIteratorImpl implements FileItemIterator {
        class FileItemStreamImpl implements FileItemStream {
            private final String contentType;
            private final String fieldName;
            private final String name;
            private final boolean formField;
            private final InputStream stream;
            private boolean opened;
            private FileItemHeaders headers;
            FileItemStreamImpl ( String pName, String pFieldName,
                                 String pContentType, boolean pFormField,
                                 long pContentLength ) throws IOException {
                name = pName;
                fieldName = pFieldName;
                contentType = pContentType;
                formField = pFormField;
                final ItemInputStream itemStream = multi.newInputStream();
                InputStream istream = itemStream;
                if ( fileSizeMax != -1 ) {
                    if ( pContentLength != -1
                            &&  pContentLength > fileSizeMax ) {
                        FileSizeLimitExceededException e =
                            new FileSizeLimitExceededException (
                            String.format ( "The field %s exceeds its maximum permitted size of %s bytes.",
                                            fieldName, Long.valueOf ( fileSizeMax ) ),
                            pContentLength, fileSizeMax );
                        e.setFileName ( pName );
                        e.setFieldName ( pFieldName );
                        throw new FileUploadIOException ( e );
                    }
                    istream = new LimitedInputStream ( istream, fileSizeMax ) {
                        @Override
                        protected void raiseError ( long pSizeMax, long pCount )
                        throws IOException {
                            itemStream.close ( true );
                            FileSizeLimitExceededException e =
                                new FileSizeLimitExceededException (
                                String.format ( "The field %s exceeds its maximum permitted size of %s bytes.",
                                                fieldName, Long.valueOf ( pSizeMax ) ),
                                pCount, pSizeMax );
                            e.setFieldName ( fieldName );
                            e.setFileName ( name );
                            throw new FileUploadIOException ( e );
                        }
                    };
                }
                stream = istream;
            }
            @Override
            public String getContentType() {
                return contentType;
            }
            @Override
            public String getFieldName() {
                return fieldName;
            }
            @Override
            public String getName() {
                return Streams.checkFileName ( name );
            }
            @Override
            public boolean isFormField() {
                return formField;
            }
            @Override
            public InputStream openStream() throws IOException {
                if ( opened ) {
                    throw new IllegalStateException (
                        "The stream was already opened." );
                }
                if ( ( ( Closeable ) stream ).isClosed() ) {
                    throw new FileItemStream.ItemSkippedException();
                }
                return stream;
            }
            void close() throws IOException {
                stream.close();
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
        private final MultipartStream multi;
        private final MultipartStream.ProgressNotifier notifier;
        private final byte[] boundary;
        private FileItemStreamImpl currentItem;
        private String currentFieldName;
        private boolean skipPreamble;
        private boolean itemValid;
        private boolean eof;
        FileItemIteratorImpl ( RequestContext ctx )
        throws FileUploadException, IOException {
            if ( ctx == null ) {
                throw new NullPointerException ( "ctx parameter" );
            }
            String contentType = ctx.getContentType();
            if ( ( null == contentType )
                    || ( !contentType.toLowerCase ( Locale.ENGLISH ).startsWith ( MULTIPART ) ) ) {
                throw new InvalidContentTypeException ( String.format (
                        "the request doesn't contain a %s or %s stream, content type header is %s",
                        MULTIPART_FORM_DATA, MULTIPART_MIXED, contentType ) );
            }
            final long requestSize = ( ( UploadContext ) ctx ).contentLength();
            InputStream input;
            if ( sizeMax >= 0 ) {
                if ( requestSize != -1 && requestSize > sizeMax ) {
                    throw new SizeLimitExceededException ( String.format (
                            "the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                            Long.valueOf ( requestSize ), Long.valueOf ( sizeMax ) ),
                                                           requestSize, sizeMax );
                }
                input = new LimitedInputStream ( ctx.getInputStream(), sizeMax ) {
                    @Override
                    protected void raiseError ( long pSizeMax, long pCount )
                    throws IOException {
                        FileUploadException ex = new SizeLimitExceededException (
                            String.format ( "the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                                            Long.valueOf ( pCount ), Long.valueOf ( pSizeMax ) ),
                            pCount, pSizeMax );
                        throw new FileUploadIOException ( ex );
                    }
                };
            } else {
                input = ctx.getInputStream();
            }
            String charEncoding = headerEncoding;
            if ( charEncoding == null ) {
                charEncoding = ctx.getCharacterEncoding();
            }
            boundary = getBoundary ( contentType );
            if ( boundary == null ) {
                IOUtils.closeQuietly ( input );
                throw new FileUploadException ( "the request was rejected because no multipart boundary was found" );
            }
            notifier = new MultipartStream.ProgressNotifier ( listener, requestSize );
            try {
                multi = new MultipartStream ( input, boundary, notifier );
            } catch ( IllegalArgumentException iae ) {
                IOUtils.closeQuietly ( input );
                throw new InvalidContentTypeException (
                    String.format ( "The boundary specified in the %s header is too long", CONTENT_TYPE ), iae );
            }
            multi.setHeaderEncoding ( charEncoding );
            skipPreamble = true;
            findNextItem();
        }
        private boolean findNextItem() throws IOException {
            if ( eof ) {
                return false;
            }
            if ( currentItem != null ) {
                currentItem.close();
                currentItem = null;
            }
            for ( ;; ) {
                boolean nextPart;
                if ( skipPreamble ) {
                    nextPart = multi.skipPreamble();
                } else {
                    nextPart = multi.readBoundary();
                }
                if ( !nextPart ) {
                    if ( currentFieldName == null ) {
                        eof = true;
                        return false;
                    }
                    multi.setBoundary ( boundary );
                    currentFieldName = null;
                    continue;
                }
                FileItemHeaders headers = getParsedHeaders ( multi.readHeaders() );
                if ( currentFieldName == null ) {
                    String fieldName = getFieldName ( headers );
                    if ( fieldName != null ) {
                        String subContentType = headers.getHeader ( CONTENT_TYPE );
                        if ( subContentType != null
                                &&  subContentType.toLowerCase ( Locale.ENGLISH )
                                .startsWith ( MULTIPART_MIXED ) ) {
                            currentFieldName = fieldName;
                            byte[] subBoundary = getBoundary ( subContentType );
                            multi.setBoundary ( subBoundary );
                            skipPreamble = true;
                            continue;
                        }
                        String fileName = getFileName ( headers );
                        currentItem = new FileItemStreamImpl ( fileName,
                                                               fieldName, headers.getHeader ( CONTENT_TYPE ),
                                                               fileName == null, getContentLength ( headers ) );
                        currentItem.setHeaders ( headers );
                        notifier.noteItem();
                        itemValid = true;
                        return true;
                    }
                } else {
                    String fileName = getFileName ( headers );
                    if ( fileName != null ) {
                        currentItem = new FileItemStreamImpl ( fileName,
                                                               currentFieldName,
                                                               headers.getHeader ( CONTENT_TYPE ),
                                                               false, getContentLength ( headers ) );
                        currentItem.setHeaders ( headers );
                        notifier.noteItem();
                        itemValid = true;
                        return true;
                    }
                }
                multi.discardBodyData();
            }
        }
        private long getContentLength ( FileItemHeaders pHeaders ) {
            try {
                return Long.parseLong ( pHeaders.getHeader ( CONTENT_LENGTH ) );
            } catch ( Exception e ) {
                return -1;
            }
        }
        @Override
        public boolean hasNext() throws FileUploadException, IOException {
            if ( eof ) {
                return false;
            }
            if ( itemValid ) {
                return true;
            }
            try {
                return findNextItem();
            } catch ( FileUploadIOException e ) {
                throw ( FileUploadException ) e.getCause();
            }
        }
        @Override
        public FileItemStream next() throws FileUploadException, IOException {
            if ( eof  || ( !itemValid && !hasNext() ) ) {
                throw new NoSuchElementException();
            }
            itemValid = false;
            return currentItem;
        }
    }
    public static class FileUploadIOException extends IOException {
        private static final long serialVersionUID = -3082868232248803474L;
        public FileUploadIOException() {
            super();
        }
        public FileUploadIOException ( String message, Throwable cause ) {
            super ( message, cause );
        }
        public FileUploadIOException ( String message ) {
            super ( message );
        }
        public FileUploadIOException ( Throwable cause ) {
            super ( cause );
        }
    }
    public static class InvalidContentTypeException
        extends FileUploadException {
        private static final long serialVersionUID = -9073026332015646668L;
        public InvalidContentTypeException() {
            super();
        }
        public InvalidContentTypeException ( String message ) {
            super ( message );
        }
        public InvalidContentTypeException ( String msg, Throwable cause ) {
            super ( msg, cause );
        }
    }
    public static class IOFileUploadException extends FileUploadException {
        private static final long serialVersionUID = -5858565745868986701L;
        public IOFileUploadException() {
            super();
        }
        public IOFileUploadException ( String message, Throwable cause ) {
            super ( message, cause );
        }
        public IOFileUploadException ( String message ) {
            super ( message );
        }
        public IOFileUploadException ( Throwable cause ) {
            super ( cause );
        }
    }
    public abstract static class SizeException extends FileUploadException {
        private static final long serialVersionUID = -8776225574705254126L;
        private final long actual;
        private final long permitted;
        protected SizeException ( String message, long actual, long permitted ) {
            super ( message );
            this.actual = actual;
            this.permitted = permitted;
        }
        public long getActualSize() {
            return actual;
        }
        public long getPermittedSize() {
            return permitted;
        }
    }
    public static class SizeLimitExceededException
        extends SizeException {
        private static final long serialVersionUID = -2474893167098052828L;
        public SizeLimitExceededException ( String message, long actual,
                                            long permitted ) {
            super ( message, actual, permitted );
        }
    }
    public static class FileSizeLimitExceededException
        extends SizeException {
        private static final long serialVersionUID = 8150776562029630058L;
        private String fileName;
        private String fieldName;
        public FileSizeLimitExceededException ( String message, long actual,
                                                long permitted ) {
            super ( message, actual, permitted );
        }
        public String getFileName() {
            return fileName;
        }
        public void setFileName ( String pFileName ) {
            fileName = pFileName;
        }
        public String getFieldName() {
            return fieldName;
        }
        public void setFieldName ( String pFieldName ) {
            fieldName = pFieldName;
        }
    }
    public ProgressListener getProgressListener() {
        return listener;
    }
    public void setProgressListener ( ProgressListener pListener ) {
        listener = pListener;
    }
}

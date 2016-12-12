package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream;
class FileUploadBase$FileItemIteratorImpl$FileItemStreamImpl$1 extends LimitedInputStream {
    final   MultipartStream.ItemInputStream val$itemStream;
    @Override
    protected void raiseError ( final long pSizeMax, final long pCount ) throws IOException {
        this.val$itemStream.close ( true );
        final FileSizeLimitExceededException e = new FileSizeLimitExceededException ( String.format ( "The field %s exceeds its maximum permitted size of %s bytes.", FileItemStreamImpl.access$300 ( FileItemStreamImpl.this ), pSizeMax ), pCount, pSizeMax );
        e.setFieldName ( FileItemStreamImpl.access$300 ( FileItemStreamImpl.this ) );
        e.setFileName ( FileItemStreamImpl.access$000 ( FileItemStreamImpl.this ) );
        throw new FileUploadIOException ( e );
    }
}

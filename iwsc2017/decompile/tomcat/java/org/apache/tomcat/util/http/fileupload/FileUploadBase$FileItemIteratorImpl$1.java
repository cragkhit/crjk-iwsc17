package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream;
class FileUploadBase$FileItemIteratorImpl$1 extends LimitedInputStream {
    @Override
    protected void raiseError ( final long pSizeMax, final long pCount ) throws IOException {
        final FileUploadException ex = new SizeLimitExceededException ( String.format ( "the request was rejected because its size (%s) exceeds the configured maximum (%s)", pCount, pSizeMax ), pCount, pSizeMax );
        throw new FileUploadIOException ( ex );
    }
}

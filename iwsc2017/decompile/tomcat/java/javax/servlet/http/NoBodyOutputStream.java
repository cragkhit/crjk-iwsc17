package javax.servlet.http;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.servlet.ServletOutputStream;
class NoBodyOutputStream extends ServletOutputStream {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings;
    private int contentLength;
    NoBodyOutputStream() {
        this.contentLength = 0;
    }
    int getContentLength() {
        return this.contentLength;
    }
    @Override
    public void write ( final int b ) {
        ++this.contentLength;
    }
    @Override
    public void write ( final byte[] buf, final int offset, final int len ) throws IOException {
        if ( buf == null ) {
            throw new NullPointerException ( NoBodyOutputStream.lStrings.getString ( "err.io.nullArray" ) );
        }
        if ( offset < 0 || len < 0 || offset + len > buf.length ) {
            String msg = NoBodyOutputStream.lStrings.getString ( "err.io.indexOutOfBounds" );
            final Object[] msgArgs = { offset, len, buf.length };
            msg = MessageFormat.format ( msg, msgArgs );
            throw new IndexOutOfBoundsException ( msg );
        }
        this.contentLength += len;
    }
    @Override
    public boolean isReady() {
        return false;
    }
    @Override
    public void setWriteListener ( final WriteListener listener ) {
    }
    static {
        lStrings = ResourceBundle.getBundle ( "javax.servlet.http.LocalStrings" );
    }
}

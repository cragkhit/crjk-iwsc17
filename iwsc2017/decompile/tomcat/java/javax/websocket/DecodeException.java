package javax.websocket;
import java.nio.ByteBuffer;
public class DecodeException extends Exception {
    private static final long serialVersionUID = 1L;
    private ByteBuffer bb;
    private String encodedString;
    public DecodeException ( final ByteBuffer bb, final String message, final Throwable cause ) {
        super ( message, cause );
        this.bb = bb;
    }
    public DecodeException ( final String encodedString, final String message, final Throwable cause ) {
        super ( message, cause );
        this.encodedString = encodedString;
    }
    public DecodeException ( final ByteBuffer bb, final String message ) {
        super ( message );
        this.bb = bb;
    }
    public DecodeException ( final String encodedString, final String message ) {
        super ( message );
        this.encodedString = encodedString;
    }
    public ByteBuffer getBytes() {
        return this.bb;
    }
    public String getText() {
        return this.encodedString;
    }
}

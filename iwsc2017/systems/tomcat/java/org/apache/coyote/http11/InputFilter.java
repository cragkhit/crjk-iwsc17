package org.apache.coyote.http11;
import java.io.IOException;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
public interface InputFilter extends InputBuffer {
    public void setRequest ( Request request );
    public void recycle();
    public ByteChunk getEncodingName();
    public void setBuffer ( InputBuffer buffer );
    public long end() throws IOException;
    public int available();
    public boolean isFinished();
}

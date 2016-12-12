package org.apache.coyote.http11;
import java.io.IOException;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.Request;
import org.apache.coyote.InputBuffer;
public interface InputFilter extends InputBuffer {
    void setRequest ( Request p0 );
    void recycle();
    ByteChunk getEncodingName();
    void setBuffer ( InputBuffer p0 );
    long end() throws IOException;
    int available();
    boolean isFinished();
}

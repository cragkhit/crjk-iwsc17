package org.apache.coyote.http11;
import java.io.IOException;
import org.apache.coyote.Response;
import org.apache.coyote.OutputBuffer;
public interface OutputFilter extends OutputBuffer {
    void setResponse ( Response p0 );
    void recycle();
    void setBuffer ( OutputBuffer p0 );
    long end() throws IOException;
}
